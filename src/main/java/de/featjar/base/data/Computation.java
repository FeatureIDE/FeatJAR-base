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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Describes how to obtain a computation result for an object.
 * The computation can depend on other computations, whose results are provided by a {@link Store}.
 * Its progress can be reported with a {@link Monitor}.
 * A computation in a given {@link Store} is uniquely identified by {@link #getIdentifier()}.
 *
 * @param <T> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Computation<T> extends Supplier<FutureResult<T>>, Extension { // todo Validatable (validate against Feature Model)
    // a computation depends on other computations by adding them as attributes
    // whether an input is a Computation<T> or just a T depends on subjective judgment (e.g., twisesample(formula, t) -> formula is a computation, t is usually just a given integer)

    // do not rename as reflection in Store depends on this
    FutureResult<T> compute(); // always computes - only call directly if caching is undesired

    // todo: hashcode depends on all inputs. default hashcode implementation?

    @Override
    default FutureResult<T> get() { // only computes if not cached yet
        return Feat.store().compute(this);
    }

//    default Result<T> computeResult() { // computes (considering no cache) and waits for result
//        return compute().get(); // todo who decides what is cached, what is not? use cflowbelow to en/disable cache for certain computations? for now, cache everything
//    }

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
                return Computation.this.compute().thenCompute((t, monitor) -> computationFunction.apply(t));
            }
        };
    }

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
