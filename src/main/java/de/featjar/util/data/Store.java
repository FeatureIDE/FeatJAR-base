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
package de.featjar.util.data;

import de.featjar.util.task.Monitor;
import de.featjar.util.task.MonitorableSupplier;

import java.util.Arrays;

/**
 * Stores computation results.
 * A store is usually scoped to the object whose computation results it stores (e.g., a formula or feature model).
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Store {
    default <R> boolean has(Computation<?, R> computation) {
        return false;
    } // should always return false for lambda instantiations

    default <R> Result<R> get(Computation<?, R> computation) {
        return Result.empty();
    }

    default <R> void put(Computation<?, R> computation, Result<R> result) {} // should do nothing for lambda instantiations

    @SuppressWarnings({"unchecked", "rawtypes"})
    default MonitorableSupplier<Object> compute(Computation... computations) {
        return monitor -> Result.ofOptional(Arrays.stream(computations).reduce((c1, c2) -> c1.andThen(c2, this)))
                .flatMap(computation -> computation.apply(null, monitor, this));
    }

    class Seed<T> implements Computation<Void, T> {
        T t;

        public Seed(T t) {
            this.t = t;
        }

        @Override
        public Result<T> execute(Void input, Monitor monitor) {
            return Result.of(t);
        }
    }
}
