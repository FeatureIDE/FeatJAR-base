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
package de.featjar.util.task;

import de.featjar.util.data.Result;
import de.featjar.util.task.Monitor.TaskCanceledException;

/**
 * Executes long-running tasks.
 * A task is a {@link MonitorableFunction} or a {@link MonitorableSupplier}.
 * A {@link Monitor} can be passed to allow cancellation and report progress of tasks.
 * Tasks return a {@link Result} to allow incomplete or failing computations.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Executor {
    /**
     * {@return the result of the given supplier}
     *
     * @param supplier the monitorable supplier
     * @param <T> the type of the result
     */
    public static <T> Result<T> get(MonitorableSupplier<T> supplier) {
        return get(supplier, null);
    }

    /**
     * {@return the result of the given supplier}
     *
     * @param supplier the monitorable supplier
     * @param monitor  the monitor
     * @param <T> the type of the result
     */
    public static <T> Result<T> get(MonitorableSupplier<T> supplier, Monitor monitor) {
        monitor = monitor != null ? monitor : new ProgressMonitor();
        try {
            return supplier.apply(monitor);
        } catch (final Exception e) {
            return Result.empty(e);
        } finally {
            monitor.setDone();
        }
    }

    /**
     * {@return the result of the given function applied to an input}
     *
     * @param function the monitorable function
     * @param input the input to the function
     * @param <T> the type of the result
     */
    public static <T, R> Result<R> apply(MonitorableFunction<T, R> function, T input) {
        return apply(function, input, null);
    }

    /**
     * {@return the result of the given function applied to an input}
     *
     * @param function the monitorable function
     * @param input the input to the function
     * @param monitor  the monitor
     * @param <T> the type of the result
     */
    public static <T, R> Result<R> apply(MonitorableFunction<T, R> function, T input, Monitor monitor)
            throws TaskCanceledException {
        monitor = monitor != null ? monitor : new ProgressMonitor();
        try {
            return function.apply(input, monitor);
        } catch (final Exception e) {
            return Result.empty(e);
        } finally {
            monitor.setDone();
        }
    }
}
