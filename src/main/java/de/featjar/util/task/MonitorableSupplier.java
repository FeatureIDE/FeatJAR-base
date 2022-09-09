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

import java.util.function.Function;

/**
 * A task that potentially takes a long time to finish and may fail to return a result.
 * Can be executed with the {@link Executor} and monitored with a {@link Monitor}.
 * Calling {@link #apply(Monitor)} directly is discouraged, use the {@link Executor} instead.
 *
 * @param <T> the supplied object's type
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface MonitorableSupplier<T> extends Function<Monitor, Result<T>> {
    /**
     * Executes this task.
     *
     * @param monitor the monitor
     * @return the supplied object, if any
     */
    Result<T> apply(Monitor monitor);
}