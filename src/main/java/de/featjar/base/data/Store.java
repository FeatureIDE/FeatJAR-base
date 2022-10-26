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

import de.featjar.base.task.MonitorableSupplier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores computation results.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Store {
    private static final Store INSTANCE = new Store();

    protected Store() {
    }

    /**
     * {@return the global store}
     */
    public static Store getInstance() {
        return INSTANCE; // todo: for simplicity, this is a singleton for now
    }

    protected final Map<Computation<?>, FutureResult<?>> computationMap = new ConcurrentHashMap<>(); // assume that Computation<T> is mapped to FutureResult<T> (same T)

    public <T> FutureResult<T> compute(Computation<T> computation) {
        if (has(computation))
            return get(computation).get();
        FutureResult<T> futureResult = computation.compute();
        put(computation, futureResult);
        return futureResult;
    }

    public <T> boolean has(Computation<T> computation) {
        return computationMap.containsKey(computation);
    }

    public <T> Result<FutureResult<T>> get(Computation<T> computation) {
        if (!has(computation))
            return Result.empty();
        return Result.of((FutureResult<T>) computationMap.get(computation));
    }

    public <T> boolean put(Computation<T> computation, FutureResult<T> futureResult) {
        if (has(computation)) // once set, immutable
            return false;
        computationMap.put(computation, futureResult);
        return true;
    }

    public <T> boolean remove(Computation<T> computation) {
        if (!has(computation))
            return false;
        computationMap.remove(computation);
        return true;
    }

    public void clear() {
        computationMap.clear();
    }
}
