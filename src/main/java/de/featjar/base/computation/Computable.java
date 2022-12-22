/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.computation;

import de.featjar.base.Feat;
import de.featjar.base.cli.Option;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.extension.Extension;
import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.Monitor;
import de.featjar.base.tree.structure.Traversable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.featjar.base.computation.Computations.async;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * A {@link Computable} does not contain the computation result itself, which is only computed on demand.
 * If computed with {@link #get()} or {@link #compute()}, the result is returned as an
 * asynchronous {@link FutureResult}, which can be shared, cached, and waited for.
 * When computed with {@link #get()}, results are possibly cached in a {@link Cache}; {@link #compute()} does not cache.
 * Computation progress can optionally be reported with a {@link Monitor}.
 * Computations can depend on other computations by assigning them to fields and calling {@link #allOf(Computable[])}
 * or {@link FutureResult#thenCompute(BiFunction)} in {@link #compute()}.
 * To ensure the determinism required by caching, all parameters of a computation must be stored in fields.
 * Whether a parameter of type T should be stored as T or Computation&lt;T&gt; depends on whether the parameter
 * is expected to depend on other computation's results.
 * Implementors should pass mandatory parameters in the constructor and optional parameters using dedicated setters.
 * Dedicated setters should return the computation itself to allow for fluent configuration.
 * TODO: A validation scheme (e.g., against a simple feature model) and serialization scheme
 *  (e.g., to sensibly compare and cache computations based on their parameters and hash code) are missing for now.
 * TODO: Monitor and store should be injected once (see notes in {@link Monitor}), and then not worried about any further.
 * TODO: A hash code computation is completely missing, so caching does not work well at all right now.
 *
 * asynchronous supplier
 *
 * @param <T> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Computable<T> extends Supplier<FutureResult<T>>, Extension, Traversable<Computable<?>> {
    /**
     * {@return the (newly computed) asynchronous result of this computation}
     * The result is returned asynchronously; that is, as a {@link FutureResult}.
     * Calling this function directly is discouraged, as the result is forced to be re-computed.
     * Usually, you should call {@link #get()} instead to leverage cached results.
     * Do not rename this method, as the {@link Cache.CachingPolicy} performs
     * reflection that depends on its name to detect nested computations.
     */
    FutureResult<T> compute();

    /**
     * {@return the (possibly cached) asynchronous result of this computation}
     * Behaves just like {@link #compute()}, but tries to hit the {@link Cache} first.
     */
    @Override
    default FutureResult<T> get() {
        return Feat.cache().compute(this);
    }

    /**
     * {@return the (possibly cached) synchronous result of this computation}
     * The result is returned synchronously; that is, as a {@link Result}.
     * Like {@link #get()}, tries to hit the {@link Cache} before calling {@link #compute()}.
     */
    default Result<T> getResult() {
        return get().get();
    }

    /**
     * {@return a trivial computation that computes nothing}
     *
     * @param <T> the type of the object
     */
    static <T> Computable<T> empty() {
        return of(null, new CancelableMonitor());
    }

    /**
     * {@return a trivial computation that computes a given object}
     *
     * @param object the object
     * @param <T>    the type of the object
     */
    static <T> Computable<T> of(T object) {
        return of(object, new CancelableMonitor());
    }

    /**
     * {@return a trivial computation that computes a given object}
     *
     * @param object  the object
     * @param monitor the monitor
     * @param <T>     the type of the object
     */
    static <T> Computable<T> of(T object, Monitor monitor) {
        return new Computation.Constant<>(object, monitor);
    }

    /**
     * {@return a computation that computes both given computations, summarizing their results in a pair}
     *
     * @param computable1 the first computation
     * @param computable2 the second computation
     */
    @SuppressWarnings("unchecked")
    static <T, U> Computable<Pair<T, U>> of(Computable<T> computable1, Computable<U> computable2) {
        Computable<?>[] list = new Computable[]{computable1, computable2};
        return Computable.allOf(list).mapResult(
                Computable.class, "of", _list -> new Pair<>((T) _list.get(0), (U) _list.get(1)));
    }

    /**
     * {@return a computation that computes all of the given computations, summarizing their results in a list}
     *
     * @param dependencies the computations
     */
    static Computable<List<?>> allOf(List<? extends Computable<?>> dependencies) {
        return allOf(dependencies.toArray(Computable[]::new));
    }

    /**
     * {@return a computation that computes all of the given computations, summarizing their results in a list}
     *
     * @param dependencies the computations
     */
    static Computable<List<?>> allOf(Computable<?>... dependencies) {
        return new Computation.AllOf(dependencies);
    }

    /**
     * {@return the value returned by a given function applied to this computation}
     * Typically, this returns a new computation composed with this computation.
     *
     * @param fn the function
     */
    default <U> U map(Function<Computable<T>, U> fn) {
        return fn.apply(this);
    }

    @SuppressWarnings("unchecked")
    default <U extends Computable<T>> Computable<T> peek(Consumer<U> fn) {
        fn.accept((U) this);
        return this;
    }

    /**
     * {@return a computation that maps the result of this computation to another value}
     *
     * @param fn the function
     */
    default <U> Computable<U> mapResult(Class<?> klass, String scope, Function<T, U> fn) {
        return flatMapResult(klass, scope, t -> Result.of(fn.apply(t)));
    }

    /**
     * {@return a computation that maps the result of this computation to another value}
     *
     * @param fn the function
     */
    default <U> Computable<U> flatMapResult(Class<?> klass, String scope, Function<T, Result<U>> fn) {
        return new Computation.Mapper<>(this, klass.getCanonicalName() + "." + scope, fn);
    }

    /**
     * {@return a computation that runs a given function on the result}
     *
     * @param fn the function
     */
    default Computable<T> peekResult(Class<?> klass, String scope, Consumer<T> fn) {
        return mapResult(klass, scope, t -> {
            fn.accept(t);
            return t;
        });
    }

    // future ideas

    // TODO: the hashcode should depend on all inputs. can we create a default hashcode implementation?
    //  serialize should be used in equals + hashcode.
    //  requires that c1.serialize() == c2.serialize yield the same computation result.
    //  could abstract away complex identities to improve caching.
    // ... serialize();

    // TODO: validate whether a computation is sensible.
    //  maybe by encoding valid computations in a feature model, or some other way.
    // boolean validate();

    // TODO: besides using feature modeling to "magically" complete computations (in a separate module),
    //  it may be nice to denote THE canonical best input for a computation.
    //  maybe this can also be done with alternative constructors or something?
    //  maybe this is also something to be implemented in its own module?
//    <S> Optional<Computation<S, T>> getPreferredInputComputation();

    default <U> Computable<U> getDependency(Dependency<U> dependency) {
        return dependency.get(this);
    }

    default <U> void setDependency(Dependency<U> dependency, Computable<U> computable) {
        dependency.set(this, computable);
    }

    // assumes that this can be cast to Computable
    interface WithInput<T> {
        Dependency<T> getInputDependency();

        /**
         * {@return the input of this analysis}
         * This analysis uses the result of this computation as its primary input (e.g., the formula to analyze).
         */
        default Computable<T> getInput() {
            return getInputDependency().get((Computable<?>) this);
        }

        /**
         * Sets the input of this analysis.
         *
         * @param input the input computation
         */
        default void setInput(Computable<T> input) {
            getInputDependency().set((Computable<?>) this, input);
        }
    }

    /**
     * A potentially long-running analysis that can be canceled if a given time has passed.
     */
    interface WithTimeout { // todo: how to handle partial results?
        Dependency<Long> getTimeoutDependency();

        /**
         * {@return the timeout of this analysis in milliseconds, if any}
         * This analysis terminates with an empty {@link Result} when it has
         * not terminated until the timeout passes.
         */
        default Computable<Long> getTimeout() {
            return getTimeoutDependency().get((Computable<?>) this);
        }

        /**
         * Sets the timeout of this analysis in milliseconds.
         *
         * @param timeout the timeout in milliseconds, if any
         */
        default void setTimeout(Computable<Long> timeout) {
            getTimeoutDependency().set((Computable<?>) this, timeout);
        }
    }

    /**
     * An analysis that may need to generate pseudorandom numbers.
     */
    interface WithRandom {
        /**
         * The default seed for the pseudorandom number generator returned by {@link #getRandom()}, if not specified otherwise.
         */
        long DEFAULT_RANDOM_SEED = 0;// todo: needed?

        Dependency<Random> getRandomDependency();

        /**
         * {@return the pseudorandom number generator of this analysis}
         */
        default Computable<Random> getRandom() {
            return getRandomDependency().get((Computable<?>) this);
        }

        /**
         * Sets the pseudorandom number generator of this analysis.
         *
         * @param random the pseudorandom number generator
         */
        default void setRandom(Computable<Random> random) {
            getRandomDependency().set((Computable<?>) this, random);
        }

        /**
         * Sets the pseudorandom number generator of this analysis based on a given seed.
         * Uses Java's default PRNG implementation.
         * If no seed is given, uses the default seed.
         *
         * @param seed the seed
         */
        default void setRandomSeed(Computable<Long> seed) {
            setRandom(seed.mapResult(WithRandom.class, "setRandom", Random::new));
        }
    }
}
