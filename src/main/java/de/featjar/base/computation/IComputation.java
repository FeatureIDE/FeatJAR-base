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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.extension.IExtension;
import de.featjar.base.tree.structure.ITree;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * An {@link IComputation} does not contain the computation result itself, it only computes it on demand.
 * Thus, it can be considered an asynchronous {@link Supplier}.
 * If computed with {@link #get()} or {@link #computeFutureResult()}, the result is returned as an
 * asynchronous {@link FutureResult}, which can be shared, cached, and waited for.
 * When computed with {@link #get()}, results are possibly cached in a {@link Cache}; {@link #computeFutureResult()} does not cache.
 * Computations can depend on other computations by declaring a {@link Dependency} on such a computation
 * and calling {@link Computations#allOf(IComputation[])} or {@link FutureResult#thenFromResult(BiFunction)} in {@link #computeFutureResult()}.
 * To ensure the determinism required by caching, all parameters of a computation must be depended on.
 * Implementors should pass mandatory parameters in the constructor and optional parameters using dedicated setters.
 * This can be facilitated by using specializations of {@link IComputation} (e.g., {@link IInputDependency}).
 * Every computation is a tree of computations, where the dependencies of the computation are its children.
 * TODO: A validation scheme (e.g., against a simple feature model) and serialization scheme
 *  (e.g., to sensibly compare and cache computations based on their parameters and hash code) are missing for now.
 * an empty child result signals an unrecoverable error
 * for recoverable errors, return a non-empty child result with warnings/errors
 *
 * @param <T> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IComputation<T> extends Supplier<FutureResult<T>>, IExtension, ITree<IComputation<?>> {
    //must be deterministic and only depend on results
    // Do not rename this method, as the {@link Cache.CachePolicy} performs
    // reflection that depends on its name to detect nested computations.
    Result<T> computeResult(List<?> results, Progress progress); // todo: only allow indexing into results via Dependency (e.g., create a DependencyList/InputList)

    // todo: computeresult that circumvents creation of future result objects (for quick'n'dirty computations)

    /**
     * {@return the (newly computed) asynchronous result of this computation}
     * The result is returned asynchronously; that is, as a {@link FutureResult}.
     * Calling this function directly is discouraged, as the result is forced to be re-computed.
     * Usually, you should call {@link #get()} instead to leverage cached results.
     */
    default FutureResult<T> computeFutureResult() {
        List<FutureResult<?>> futureResults = new ArrayList<>();
        for (IComputation<?> child : getChildren()) {
            futureResults.add(child.get());
        }
        return FutureResult.allOf(futureResults, getResultMerger())
                .thenResult(this::computeResult);
    }

    default Function<List<? extends Result<?>>, Result<List<?>>> getResultMerger() {
        return Result::mergeAll;
    }

    /**
     * {@return the (possibly cached) asynchronous result of this computation}
     * Behaves just like {@link #computeFutureResult()}, but tries to hit the {@link Cache} first.
     */
    @Override
    default FutureResult<T> get() {
        return FeatJAR.cache().computeFutureResult(this);
    }

    /**
     * {@return the (newly computed) synchronous result of this computation}
     * The result is returned synchronously; that is, as a {@link Result}.
     * Calling this function directly is discouraged, as the result is forced to be re-computed.
     * Usually, you should call {@link #getResult()} instead to leverage cached results.
     */
    default Result<T> computeResult() {
        return computeFutureResult().get();
    }

    /**
     * {@return the (possibly cached) synchronous result of this computation}
     * The result is returned synchronously; that is, as a {@link Result}.
     * Like {@link #get()}, tries to hit the {@link Cache} before calling {@link #computeFutureResult()}.
     */
    default Result<T> getResult() {
        return get().get();
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
     * @param fn    the function
     * @param <U>   the type of the mapped result
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
     * @param fn    the function
     * @param <U>   the type of the mapped result
     */
    default <U> IComputation<U> flatMapResult(Class<?> klass, String scope, Function<T, Result<U>> fn) {
        return new ComputeFunction<>(this, klass, scope, fn);
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
     * @param <U>        the type of the computation result
     */
    default <U> IComputation<U> getDependency(Dependency<U> dependency) {
        return dependency.get(this);
    }

    /**
     * Sets the computation for a given dependency of this computation.
     *
     * @param dependency  the dependency
     * @param computation the computation
     * @param <U>         the type of the computation result
     */
    default <U> void setDependency(Dependency<U> dependency, IComputation<U> computation) {
        dependency.set(this, computation);
    }
}
