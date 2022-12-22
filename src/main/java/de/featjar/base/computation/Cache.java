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
package de.featjar.base.computation;

import de.featjar.base.Feat;
import de.featjar.base.data.Result;
import de.featjar.base.extension.Initializer;
import de.featjar.base.env.StackTrace;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches computation results by storing a map of computations to their future results.
 * TODO: Currently, this is implemented as a single large store (a Singleton inside {@link de.featjar.base.FeatJAR}).
 *  See the notes in {@link de.featjar.base.task.Monitor} how this might be improved.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Cache implements Initializer {
    /**
     * Specifies which computation results a cache should contain.
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
         * Nested computations are detected by checking if {@link Computable#compute()} is already on the stack.
         * Nesting inside anonymous computations (e.g., lambdas) is not detected and therefore cached.
         */
        CachingPolicy CACHE_TOP_LEVEL = (computation, stackTrace) ->
                !stackTrace.containsMethodCall(Computable.class, "compute");

        /**
         * {@return whether the calling cache should store the given computation}
         *
         * @param computable the computation
         * @param stackTrace  the current stack trace
         */
        boolean shouldCache(Computable<?> computable, StackTrace stackTrace);
    }

    /**
     * Configures a cache.
     */
    public static class Configuration {
        protected CachingPolicy cachingPolicy = CachingPolicy.CACHE_NONE;

        /**
         * Configures the caching policy.
         *
         * @param cachingPolicy the caching policy
         * @return this configuration
         */
        public Configuration setCachingPolicy(CachingPolicy cachingPolicy) {
            this.cachingPolicy = cachingPolicy;
            return this;
        }
    }

    /**
     * The default configuration used for new caches.
     */
    protected static Configuration defaultConfiguration = null;

    /**
     * This cache's configuration.
     */
    protected Configuration configuration;

    /**
     * A cache that maps computations to their future results.
     * A {@link Computable} of type {@code T} should be mapped to a {@link FutureResult} of the same type {@code T}.
     */
    protected final Map<Computable<?>, FutureResult<?>> computationMap = new ConcurrentHashMap<>();

    /**
     * Sets the default configuration used for new caches.
     *
     * @param defaultConfiguration the default configuration
     */
    public static void setDefaultConfiguration(Configuration defaultConfiguration) {
        Feat.log().debug("setting new default cache configuration");
        Cache.defaultConfiguration = defaultConfiguration;
    }

    /**
     * Creates a cache based on the default configuration.
     */
    public Cache() {
        this(defaultConfiguration);
    }

    /**
     * Creates a cache.
     *
     * @param configuration the configuration
     */
    public Cache(Cache.Configuration configuration) {
        Feat.log().debug("initializing cache");
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * Clears this cache.
     */
    @Override
    public void close() {
        Feat.log().debug("de-initializing cache");
        clear();
    }

    /**
     * {@return this cache's configuration}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets this cache's configuration.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        Feat.log().debug("setting new cache configuration");
        this.configuration = configuration;
    }

    /**
     * {@return the result of a given computation}
     * Returns the {@link FutureResult} for the {@link Computable} from the cache if it has already been stored.
     * Otherwise, computes the {@link FutureResult} and returns it.
     * The computed {@link FutureResult} is cached if the current {@link CachingPolicy} agrees.
     *
     * @param computable the computation
     * @param <T>         the type of the computation result
     */
    public <T> FutureResult<T> compute(Computable<T> computable) {
        String computationClassName = computable.getClass().getName();
        if (has(computable)) {
            Feat.log().debug("cache hit for " + computationClassName);
            return get(computable).get();
        }
        Feat.log().debug("cache miss for " + computationClassName);
        FutureResult<T> futureResult = computable.compute();
        if (configuration.cachingPolicy.shouldCache(computable, new StackTrace())) {
            Feat.log().debug("cache write for " + computationClassName);
            put(computable, futureResult);
        }
        return futureResult;
    }

    /**
     * {@return whether the given computation has been cached in this cache}
     *
     * @param computable the computation
     * @param <T>         the type of the computation result
     */
    public <T> boolean has(Computable<T> computable) {
        return computationMap.containsKey(computable);
    }

    /**
     * {@return the cached result of a given computation, if any}
     *
     * @param computable the computation
     * @param <T>         the type of the computation result
     */
    @SuppressWarnings("unchecked")
    public <T> Result<FutureResult<T>> get(Computable<T> computable) {
        return has(computable)
                ? Result.of((FutureResult<T>) computationMap.get(computable))
                : Result.empty();
    }

    /**
     * Sets the cached result for a given computation, if not already cached.
     * Does nothing if the computation has already been cached.
     *
     * @param computable  the computation
     * @param futureResult the future result
     * @param <T>          the type of the computation result
     * @return whether the operation affected this cache
     */
    public <T> boolean put(Computable<T> computable, FutureResult<T> futureResult) {
        if (has(computable)) // once set, immutable
            return false;
        computationMap.put(computable, futureResult);
        return true;
    }

    /**
     * Removes the cached result for a given computation, if already cached.
     * Does nothing if the computation has not already been cached.
     *
     * @param computable the computation
     * @param <T>         the type of the computation result
     * @return whether the operation affected this cache
     */
    public <T> boolean remove(Computable<T> computable) {
        if (!has(computable))
            return false;
        Feat.log().debug("cache remove for " + computable);
        computationMap.remove(computable);
        return true;
    }

    /**
     * Removes all cached computation results.
     */
    public void clear() {
        Feat.log().debug("clearing cache");
        computationMap.clear();
    }

    /**
     * Caches nothing during (de-)initialization of FeatJAR, as there is no {@link Cache} configured then.
     */
    public static class Fallback extends Cache {
        public Fallback() {
            super(new Configuration());
        }
    }
}
