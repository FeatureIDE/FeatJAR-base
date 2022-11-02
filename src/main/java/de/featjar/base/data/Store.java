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
import de.featjar.base.extension.Initializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches computation results by storing a map of computations to their future results.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Store implements Initializer {
    /**
     * Specifies which computation results a store should cache.
     */
    public interface CachingPolicy {
        /**
         * Caches no computation results.
         */
        CachingPolicy CACHE_NONE = (computation, stackTrace) -> false;

        /**
         * Caches all computation results, even those nested in other computations.
         */
        CachingPolicy CACHE_ALL = (computation, stackTrace) -> true;

        /**
         * Caches top-level computation results; that is, those not nested in other computations.
         * Nested computations are detected by checking if {@link Computation#compute()} is already on the stack.
         * Nesting inside anonymous computations (e.g., lambdas) is not detected and therefore cached.
         */
        CachingPolicy CACHE_TOP_LEVEL = new CachingPolicy() {
            private boolean isComputationComputeMethod(StackTraceElement stackTraceElement) {
                try {
                    return Computation.class.isAssignableFrom(Class.forName(stackTraceElement.getClassName())) &&
                            stackTraceElement.getMethodName().equals("compute");
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }

            @Override
            public boolean shouldCache(Computation<?> computation, StackTraceElement[] stackTrace) {
                return Arrays.stream(stackTrace).noneMatch(this::isComputationComputeMethod);
            }
        };

        /**
         * {@return whether the calling store should cache the given computation}
         *
         * @param computation the computation
         * @param stackTrace the current stack trace
         */
        boolean shouldCache(Computation<?> computation, StackTraceElement[] stackTrace);
    }

    /**
     * Configures a store.
     */
    public static class Configuration {
        protected CachingPolicy cachingPolicy = CachingPolicy.CACHE_NONE;

        /**
         * Configures the caching policy.
         *
         * @param cachingPolicy the caching policy
         * {@return this configuration}
         */
        public Configuration setCachingPolicy(CachingPolicy cachingPolicy) {
            this.cachingPolicy = cachingPolicy;
            return this;
        }
    }

    /**
     * The default configuration used for new stores.
     */
    protected static Configuration defaultConfiguration = null;

    /**
     * This store's configuration.
     */
    protected Configuration configuration;

    /**
     * A cache that maps computations to their future results.
     * A {@link Computation} of type {@code T} should be mapped to a {@link FutureResult} of the same type {@code T}.
     */
    protected final Map<Computation<?>, FutureResult<?>> computationMap = new ConcurrentHashMap<>();

    /**
     * Sets the default configuration used for new stores.
     *
     * @param defaultConfiguration the default configuration
     */
    public static void setDefaultConfiguration(Configuration defaultConfiguration) {
        Feat.log().debug("setting new default store configuration");
        Store.defaultConfiguration = defaultConfiguration;
    }

    /**
     * Creates a store based on the default configuration.
     */
    public Store() {
        this(defaultConfiguration);
    }

    /**
     * Creates a store.
     *
     * @param configuration the configuration
     */
    public Store(Store.Configuration configuration) {
        Feat.log().debug("initializing store");
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * Clears this store.
     */
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
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        Feat.log().debug("setting new store configuration");
        this.configuration = configuration;
    }

    /**
     * {@return the result of a given computation}
     * Returns the {@link FutureResult} for the {@link Computation} from the cache if it has already been stored.
     * Otherwise, computes the {@link FutureResult} and returns it.
     * The computed {@link FutureResult} is cached if the current {@link CachingPolicy} agrees.
     *
     * @param computation the computation
     * @param <T> the type of the computation result
     */
    public <T> FutureResult<T> compute(Computation<T> computation) {
        if (has(computation))
            return get(computation).get();
        FutureResult<T> futureResult = computation.compute();
        if (configuration.cachingPolicy.shouldCache(computation, Thread.currentThread().getStackTrace()))
            put(computation, futureResult);
        return futureResult;
    }

    /**
     * {@return whether the given computation has been cached in this store}
     *
     * @param computation the computation
     * @param <T> the type of the computation result
     */
    public <T> boolean has(Computation<T> computation) {
        return computationMap.containsKey(computation);
    }

    /**
     * {@return the cached result of a given computation, if any}
     *
     * @param computation the computation
     * @param <T> the type of the computation result
     */
    @SuppressWarnings("unchecked")
    public <T> Result<FutureResult<T>> get(Computation<T> computation) {
        return has(computation)
                ? Result.of((FutureResult<T>) computationMap.get(computation))
                : Result.empty();
    }

    /**
     * Sets the cached result for a given computation, if not already cached.
     * Does nothing if the computation has already been cached.
     *
     * @param computation the computation
     * @param futureResult the future result
     * @param <T> the type of the computation result
     * @return whether the operation affected this store
     */
    public <T> boolean put(Computation<T> computation, FutureResult<T> futureResult) {
        if (has(computation)) // once set, immutable
            return false;
        computationMap.put(computation, futureResult);
        return true;
    }

    /**
     * Removes the cached result for a given computation, if already cached.
     * Does nothing if the computation has not already been cached.
     *
     * @param computation the computation
     * @param <T> the type of the computation result
     * @return whether the operation affected this store
     */
    public <T> boolean remove(Computation<T> computation) {
        if (!has(computation))
            return false;
        computationMap.remove(computation);
        return true;
    }

    /**
     * Removes all cached computation results.
     */
    public void clear() {
        computationMap.clear();
    }

    /**
     * Stores nothing during (de-)initialization of FeatJAR, as there is no {@link Store} configured then.
     */
    public static class Fallback extends Store {
        public Fallback() {
            super(new Configuration());
        }
    }
}
