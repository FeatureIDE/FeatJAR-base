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
package de.featjar.base.task;

import de.featjar.base.data.Result;

import java.util.function.BiFunction;

/**
 * A task that potentially takes a long time to finish and may fail to return a result.
 * Can be monitored with a {@link Monitor}.
 * TODO: this class can most likely be dropped in favor of Computation + FutureResult.
 *
 * @param <T> the input object's type
 * @param <R> the supplied object's type
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
@FunctionalInterface
public interface MonitorableFunction<T, R> extends BiFunction<T, Monitor, Result<R>> {
    /**
     * {@return the result of this monitorable function applied to an input}
     * Performs no sanity checks, call {@link #apply(Object, Monitor)} instead.
     *
     * @param input the input to the function
     * @param monitor the monitor
     */
    Result<R> execute(T input, Monitor monitor);

    /**
     * {@return the result of this monitorable function applied to an input}
     * Performs sanity checks.
     *
     * @param input the input to the function
     * @param monitor the monitor
     */
    @Override
    default Result<R> apply(T input, Monitor monitor) {
        monitor = monitor != null ? monitor : new ProgressMonitor();
        try {
            return execute(input, monitor);
        } catch (final Exception e) {
            return Result.empty(e);
        } finally {
            monitor.setDone();
        }
    }

    /**
     * {@return the result of this monitorable function applied to an input}
     * Performs sanity checks.
     */
    default Result<R> apply(T input) {
        return apply(input , null);
    }

    /**
     * {@return a composition of this monitorable function with another monitorable function}
     * If this monitorable function fails, the entire monitorable function fails.
     *
     * @param monitorableFunction the monitorable function
     * @param <S> the type of the returned monitorable function's result
     */
    default <S> MonitorableFunction<T, Result<S>> andThen(MonitorableFunction<R, S> monitorableFunction) {
        // todo: either create child monitor on parent monitor or implement compose with ... variadic number of monitorable functions
        return (t, monitor) ->
                this.apply(t, monitor.newChildMonitor())
                .map(r -> monitorableFunction.apply(r, monitor));
    }
}
