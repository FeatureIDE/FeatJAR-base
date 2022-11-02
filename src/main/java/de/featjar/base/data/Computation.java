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
package de.featjar.base.data;

import de.featjar.base.Feat;
import de.featjar.base.extension.Extension;
import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.Monitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * A {@link Computation} does not contain the computation result itself, which is only computed on demand.
 * If computed with {@link #get()} or {@link #compute()}, the result is returned as an
 * asynchronous {@link FutureResult}, which can be shared, cached, and waited for.
 * When computed with {@link #get()}, results are possibly cached in a {@link Store}; {@link #compute()} does not cache.
 * Computation progress can optionally be reported with a {@link Monitor}.
 * Computations can depend on other computations by assigning them to fields and calling {@link #allOf(Computation[])}
 * or {@link FutureResult#thenCompute(BiFunction)} in {@link #compute()}.
 * To ensure the determinism required by caching, all parameters of a computation must be stored in fields.
 * Whether a parameter of type T should be stored as T or Computation&lt;T&gt; depends on whether the parameter
 * is expected to depend on other computation's results.
 *
 * @param <T> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Computation<T> extends Supplier<FutureResult<T>>, Extension { // todo Validatable (validate against Feature Model)
    /**
     * {@return the (newly computed) result of this computation}
     * The result is returned asynchronously; that is, as a {@link FutureResult}.
     * Calling this function directly is discouraged, as the result is forced to be re-computed.
     * Usually, you should call {@link #get()} instea to leverage cached results.
     * Do not rename this method, as the {@link de.featjar.base.data.Store.CachingPolicy} performs
     * reflection that depends on its name to detect nested computations.
     */
    FutureResult<T> compute();

    /**
     * {@return the (possibly cached) result of this computation}
     * Behaves just like {@link #compute()}, but tries to hit the {@link Store} first.
     */
    @Override
    default FutureResult<T> get() { // only computes if not cached yet
        return Feat.store().compute(this);
    }

    default Result<T> getResult() { // computes (considering the cache) and waits for result
        return get().get();
    }

    static <T> Computation<T> of(T object, Monitor monitor) { // todo: refactor all usages to of...then
        return () -> FutureResult.of(object, monitor);
    }

    static <T> Computation<T> of(T object) {
        return of(object, new CancelableMonitor()); // todo NullMonitor
    }

    static <T> Computation<T> empty() {
        return of(null, new CancelableMonitor()); // todo NullMonitor
    }

    static Computation<List<?>> allOf(Computation<?>... computations) {
        return new Computation<>() {
            //todo hashcode?
            @Override
            public FutureResult<List<?>> compute() {
                List<FutureResult<?>> futureResults =
                        Arrays.stream(computations).map(Computation::compute).collect(Collectors.toList());
                return FutureResult.wrap(FutureResult.allOf(futureResults.toArray(CompletableFuture[]::new)))
                        .thenComputeFromResult((unused, monitor) -> {
                            List<?> x = futureResults.stream()
                                    .map(FutureResult::get)
                                    .map(Result::get)
                                    .collect(Collectors.toList());
                            return x.stream().noneMatch(Objects::isNull) ? Result.of(x) : Result.empty();
                        });

            }
        };
    }

    default <U> Computation<U> then(Function<Computation<T>, Computation<U>> computationFunction) {
        return computationFunction.apply(this);
    }

    default <U> Computation<U> then(Class<? extends Computation<U>> computationClass, Object... args) { // todo: drop this because it is not type-safe?
        List<Object> arguments = new ArrayList<>();
        List<Class<?>> argumentClasses = new ArrayList<>();
        arguments.add(this);
        argumentClasses.add(Computation.class);
        arguments.addAll(List.of(args));
        argumentClasses.addAll(Arrays.stream(args).map(Object::getClass).collect(Collectors.toList()));
        try {
            Constructor<? extends Computation<U>> constructor = computationClass.getConstructor(argumentClasses.toArray(Class[]::new));
            return constructor.newInstance(arguments.toArray(Object[]::new));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    default <U> Computation<U> map(Function<T, U> computationFunction) {
        return new Computation<U>() {
            // todo: what is the hash code? probably Computation.this + the identity of computationFunction??
            @Override
            public FutureResult<U> compute() {
                return Computation.this.get().thenCompute((t, monitor) -> computationFunction.apply(t));
            }
        };
    }

    // todo: hashcode depends on all inputs. is there a default hashcode implementation?
    //void serialize(); // use in equals + hashcode. requires that c1.serialize() == c2.serialize ==> same computation result. could abstract away complex identities to improve caching.

    //boolean validate();

//    /**
//     * {@return the parameters of this computation}
//     * Should represent all parameters that are not in scope of the associated {@link Store} to allow later lookup.
//     * Returns {@code null} if there are no such parameters.
//     */
//    default Object getParameters() {
//        return null;
//    }

//    /**
//     * {@return the preferred computation for the input of this computation}
//     * Can be used to specify the recommended input for this computation.
//     * @param <S> the type of the input of the preferred input computation
//     */
//    default <S> Optional<Computation<S, T>> getPreferredInputComputation() {
//        return Optional.empty();
//    }

//    default Result<U> apply(T input, Monitor monitor, Store store) {
//        if (store.has(this))
//            return store.get(this);
//        else {
//            Result<U> output = this.apply(input, monitor);
//            store.put(this, output);
//            return output;
//        }
//    }

//    /**
//     * {@return a composition of this computation with another computation}
//     * If this computation fails, the entire computation fails.
//     *
//     * @param computation the computation
//     * @param <V> the type of the returned computation's result
//     */
//    default <V> Computation<T, Result<V>> andThen(Computation<U, V> computation, Store store) {
//        // todo: either create child monitor on parent monitor or implement compose with ... variadic number of monitorable functions
//        return (t, monitor) ->
//                this.apply(t, monitor.createChildMonitor(), store)
//                        .map(u -> computation.apply(u, monitor, store));
//    }

//    /**
//     * {@return an empty computation}
//     *
//     * @param <T> the type of the returned computation's result
//     */
//    static <T> Computation<T> empty() {
//        return (store, monitor) -> Result.empty();
//    }
//
//    /**
//     * {@return a constant computation}
//     *
//     * @param <T> the type of the returned computation's result
//     */
//    static <T> Computation<T> of(T t) {
//        return (store, monitor) -> Result.of(t);
//    }
//
//    /**
//     * {@return a constant computation}
//     *
//     * @param <T> the type of the returned computation's result
//     */
//    static <T> Computation<T> of(Result<T> t) {
//        return (store, monitor) -> t;
//    }
}
