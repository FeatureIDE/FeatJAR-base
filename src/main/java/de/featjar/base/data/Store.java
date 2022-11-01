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
import de.featjar.base.extension.Initializable;
import de.featjar.base.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores computation results.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Store implements Initializable {
    /**
     * Specifies whether and which computation results a store should cache.
     */
    public interface CachingPolicy {
        boolean shouldCache(Computation<?> computation, StackTraceElement[] stackTrace);

        CachingPolicy CACHE_ALL_COMPUTATIONS = (computation, stackTrace) -> true;
        CachingPolicy CACHE_NO_COMPUTATIONS = (computation, stackTrace) -> false;

        CachingPolicy CACHE_TOP_LEVEL_COMPUTATIONS = new CachingPolicy() {
            private boolean isComputationComputeMethod(StackTraceElement stackTraceElement) {
                try {
                    return Computation.class.isAssignableFrom(Class.forName(stackTraceElement.getClassName())) &&
                            stackTraceElement.getMethodName().equals("compute");
                } catch (ClassNotFoundException e) {
                    return false; // what if the class name is a lambda?
                }
            }

            // check whether some call to Computation#compute is already on the stack (= this call is nested)
            @Override
            public boolean shouldCache(Computation<?> computation, StackTraceElement[] stackTrace) {
                return Arrays.stream(stackTrace).noneMatch(this::isComputationComputeMethod);
            }
        };
    }

    /**
     * Configures a store.
     */
    public static class Configuration {
        protected CachingPolicy cachingPolicy = CachingPolicy.CACHE_NO_COMPUTATIONS;

        public Configuration setCachingPolicy(CachingPolicy cachingPolicy) {
            this.cachingPolicy = cachingPolicy;
            return this;
        }
    }

    protected static Configuration defaultConfiguration = null;
    protected Configuration configuration = defaultConfiguration;

    /**
     * Sets the configuration used for new stores.
     */
    public static void setDefaultConfiguration(Configuration defaultConfiguration) {
        Feat.log().debug("setting new default store configuration");
        Store.defaultConfiguration = defaultConfiguration;
    }

    public Store() {
        Feat.log().debug("initializing store");
    }

    @Override
    public void close() {
        Feat.log().debug("de-initializing store");
        clear();
    }

    /**
     * {@return this store's configuration}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets this store's configuration.
     */
    public void setConfiguration(Configuration configuration) {
        Feat.log().debug("setting new store configuration");
        this.configuration = configuration;
    }

    protected final Map<Computation<?>, FutureResult<?>> computationMap = new ConcurrentHashMap<>(); // assume that Computation<T> is mapped to FutureResult<T> (same T)

    public <T> FutureResult<T> compute(Computation<T> computation) {
        if (has(computation))
            return get(computation).get();
        FutureResult<T> futureResult = computation.compute();
        if (configuration.cachingPolicy.shouldCache(computation, Thread.currentThread().getStackTrace()))
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
