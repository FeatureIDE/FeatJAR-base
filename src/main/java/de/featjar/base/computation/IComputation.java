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
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.extension.IExtension;
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
 * An {@link IComputation} does not contain the computation result itself, it only computes it on demand.
 * Thus, it can be considered an asynchronous {@link Supplier}.
 * If computed with {@link #get()} or {@link #compute()}, the result is returned as an
 * asynchronous {@link FutureResult}, which can be shared, cached, and waited for.
 * When computed with {@link #get()}, results are possibly cached in a {@link Cache}; {@link #compute()} does not cache.
 * Computation progress can optionally be reported with a {@link Monitor}.
 * Computations can depend on other computations by declaring a {@link Dependency} on such a computation
 * and calling {@link #allOf(IComputation[])} or {@link FutureResult#thenCompute(BiFunction)} in {@link #compute()}.
 * To ensure the determinism required by caching, all parameters of a computation must be depended on.
 * Implementors should pass mandatory parameters in the constructor and optional parameters using dedicated setters.
 * This can be facilitated by using specializations of {@link IComputation} (e.g., {@link WithInput}).
 * Every computation is a tree of computations, where the dependencies of the computation are its children.
 * TODO: A validation scheme (e.g., against a simple feature model) and serialization scheme
 *  (e.g., to sensibly compare and cache computations based on their parameters and hash code) are missing for now.
 * TODO: Monitor and store should be injected once (see notes in {@link Monitor}), and then not worried about any further.
 * TODO: A hash code computation is completely missing, so caching does not work well at all right now.
 *
 * @param <T> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IComputation<T> extends Supplier<FutureResult<T>>, IExtension, Traversable<IComputation<?>> {
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
    static <T> IComputation<T> empty() {
        return of(null, new CancelableMonitor());
    }

    /**
     * {@return a trivial computation that computes a given object}
     *
     * @param object the object
     * @param <T>    the type of the object
     */
    static <T> IComputation<T> of(T object) {
        return of(object, new CancelableMonitor());
    }

    /**
     * {@return a trivial computation that computes a given object}
     *
     * @param object  the object
     * @param monitor the monitor
     * @param <T>     the type of the object
     */
    static <T> IComputation<T> of(T object, Monitor monitor) {
        return new AComputation.Constant<>(object, monitor);
    }

    /**
     * {@return a computation that computes both given computations, summarizing their results in a pair}
     *
     * @param computation1 the first computation
     * @param computation2 the second computation
     */
    @SuppressWarnings("unchecked")
    static <T, U> IComputation<Pair<T, U>> of(IComputation<T> computation1, IComputation<U> computation2) {
        IComputation<?>[] list = new IComputation[]{computation1, computation2};
        return IComputation.allOf(list).mapResult(
                IComputation.class, "of", _list -> new Pair<>((T) _list.get(0), (U) _list.get(1)));
    }

    /**
     * {@return a computation that computes all of the given computations, summarizing their results in a list}
     *
     * @param computations the computations
     */
    static IComputation<List<?>> allOf(List<? extends IComputation<?>> computations) {
        return allOf(computations.toArray(IComputation[]::new));
    }

    /**
     * {@return a computation that computes all its computations, summarizing their results in a list}
     *
     * @param computations the computations
     */
    static IComputation<List<?>> allOf(IComputation<?>... computations) {
        return new AComputation.AllOf(computations);
    }

    /**
     * {@return the value returned by a given function applied to this computation}
     * Typically, this returns a new computation composed with this computation.
     *
     * @param fn the function
     */
    default <U> U map(Function<IComputation<T>, U> fn) {
        return fn.apply(this);
    }

    /**
     * {@return peeks at this computation with a given function}
     *
     * @param fn this computation
     */
    @SuppressWarnings("unchecked")
    default <U extends IComputation<T>> IComputation<T> peek(Consumer<U> fn) {
        fn.accept((U) this);
        return this;
    }

    /**
     * {@return a computation that maps the result of this computation to another value}
     * To allow proper caching, a unique combination of the calling class and scope (e.g., a method name)
     * must be supplied.
     *
     * @param klass the calling class
     * @param scope the calling scope
     * @param fn the function
     * @param <U> the type of the mapped result
     */
    default <U> IComputation<U> mapResult(Class<?> klass, String scope, Function<T, U> fn) {
        return flatMapResult(klass, scope, t -> Result.of(fn.apply(t)));
    }

    /**
     * {@return a computation that maps the result of this computation to another value}
     * To allow proper caching, a unique combination of the calling class and scope (e.g., a method name)
     * must be supplied.
     *
     * @param klass the calling class
     * @param scope the calling scope
     * @param fn the function
     * @param <U> the type of the mapped result
     */
    default <U> IComputation<U> flatMapResult(Class<?> klass, String scope, Function<T, Result<U>> fn) {
        return new AComputation.Mapper<>(this, klass.getCanonicalName() + "." + scope, fn);
    }

    /**
     * {@return a computation that peeks at the result of this computation with a given function}
     *
     * @param fn the function
     */
    default IComputation<T> peekResult(Class<?> klass, String scope, Consumer<T> fn) {
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

    /**
     * {@return the computation for a given dependency of this computation}
     *
     * @param dependency the dependency
     * @param <U> the type of the computation result
     */
    default <U> IComputation<U> getDependency(Dependency<U> dependency) {
        return dependency.get(this);
    }

    /**
     * Sets the computation for a given dependency of this computation.
     *
     * @param dependency the dependency
     * @param computation the computation
     * @param <U> the type of the computation result
     */
    default <U> void setDependency(Dependency<U> dependency, IComputation<U> computation) {
        dependency.set(this, computation);
    }


    /**
     * A computation that depends on some (primary, typically mandatory) input.
     * Assumes that the implementing class can be cast to {@link IComputation}.
     *
     * @param <T> the type of the input
     */
    interface WithInput<T> {
        /**
         * {@return the input dependency of this computation}
         */
        Dependency<T> getInputDependency();

        /**
         * {@return the input computation of this computation}
         */
        default IComputation<T> getInput() {
            return getInputDependency().get((IComputation<?>) this);
        }

        /**
         * Sets the input computation of this computation.
         *
         * @param input the input computation
         */
        default void setInput(IComputation<T> input) {
            getInputDependency().set((IComputation<?>) this, input);
        }
    }

    /**
     * A potentially long-running computation that can be canceled if a given time has passed.
     * This computation terminates with an empty {@link Result} when it has
     * not terminated until the timeout passes.
     */
    interface WithTimeout { // todo: how to handle partial results (i.e., to return a lower bound for counting)?
        /**
         * {@return the timeout dependency of this computation}
         */
        Dependency<Long> getTimeoutDependency();

        /**
         * {@return the timeout computation in milliseconds of this computation}
         */
        default IComputation<Long> getTimeout() {
            return getTimeoutDependency().get((IComputation<?>) this);
        }

        /**
         * Sets the timeout computation in milliseconds of this computation.
         *
         * @param timeout the timeout computation in milliseconds, if any
         */
        default void setTimeout(IComputation<Long> timeout) {
            getTimeoutDependency().set((IComputation<?>) this, timeout);
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

        /**
         * {@return the random dependency of this computation}
         */
        Dependency<Random> getRandomDependency();

        /**
         * {@return the pseudorandom number generator computation of this computation}
         */
        default IComputation<Random> getRandom() {
            return getRandomDependency().get((IComputation<?>) this);
        }

        /**
         * Sets the pseudorandom number generator computation of this computation.
         *
         * @param random the pseudorandom number generator computation
         */
        default void setRandom(IComputation<Random> random) {
            getRandomDependency().set((IComputation<?>) this, random);
        }

        /**
         * Sets the pseudorandom number generator computation of this computation based on a given seed.
         * Uses Java's default pseudorandom number generator implementation.
         * If no seed is given, uses the default seed. (todo: not currently true)
         *
         * @param seed the seed
         */
        default void setRandomSeed(IComputation<Long> seed) {
            setRandom(seed.mapResult(WithRandom.class, "setRandom", Random::new));
        }
    }
}
