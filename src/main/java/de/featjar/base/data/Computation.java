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

import de.featjar.base.extension.Extension;
import de.featjar.base.task.Monitor;
import de.featjar.base.task.MonitorableFunction;

import java.util.Optional;

/**
 * Describes how to obtain a computation result for an object.
 * The computation can depend on other computation results provided by a {@link Store}.
 * Its progress can be reported with a {@link Monitor}.
 * A computation in a given {@link Store} is uniquely identified by {@link #getIdentifier()}
 * and {@link #getParameters()}.
 *
 * @param <T> the type of the input
 * @param <U> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
@FunctionalInterface
public interface Computation<T, U> extends MonitorableFunction<T, U>, Extension {

    /**
     * {@return the parameters of this computation}
     * Should represent all parameters that are not in scope of the associated {@link Store} to allow later lookup.
     * Returns {@code null} if there are no such parameters.
     */
    default Object getParameters() {
        return null;
    }

    /**
     * {@return the preferred computation for the input of this computation}
     * Can be used to specify the recommended input for this computation.
     * @param <S> the type of the input of the preferred input computation
     */
    default <S> Optional<Computation<S, T>> getPreferredInputComputation() {
        return Optional.empty();
    }

    default Result<U> apply(T input, Monitor monitor, Store store) {
        if (store.has(this))
            return store.get(this);
        else {
            Result<U> output = this.apply(input, monitor);
            store.put(this, output);
            return output;
        }
    }

    /**
     * {@return a composition of this computation with another computation}
     * If this computation fails, the entire computation fails.
     *
     * @param computation the computation
     * @param <V> the type of the returned computation's result
     */
    default <V> Computation<T, Result<V>> andThen(Computation<U, V> computation, Store store) {
        // todo: either create child monitor on parent monitor or implement compose with ... variadic number of monitorable functions
        return (t, monitor) ->
                this.apply(t, monitor.createChildMonitor(), store)
                        .map(u -> computation.apply(u, monitor, store));
    }

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
