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
import de.featjar.base.env.IBrowsable;
import de.featjar.base.extension.IInitializer;
import de.featjar.base.env.StackTrace;
import de.featjar.base.io.graphviz.GraphVizTreeFormat;
import de.featjar.base.task.IMonitor;
import de.featjar.base.tree.structure.ITree;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches computation results by storing a map of computations to their future results.
 * TODO: Currently, this is implemented as a single large store (a Singleton inside {@link de.featjar.base.FeatJAR}).
 *  See the notes in {@link IMonitor} how this might be improved.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Cache implements IInitializer, IBrowsable<GraphVizTreeFormat<IComputation<?>>> {
    /**
     * Specifies which computation results a cache should contain.
     */
    public interface CachePolicy {
        /**
         * Caches no computation results.
         */
        CachePolicy CACHE_NONE = (computation, stackTrace) -> false;

        /**
         * Caches all computation results, even those nested in other computations.
         */
        CachePolicy CACHE_ALL = (computation, stackTrace) -> true;

        /**
         * Caches top-level computation results; that is, those not nested in other computations.
         * Nested computations are detected by checking if {@link IComputation#computeResult()} is already on the stack.
         */
        CachePolicy CACHE_TOP_LEVEL = (computation, stackTrace) ->
                !stackTrace.containsMethodCall(IComputation.class, "computeResult");

        /**
         * {@return whether the calling cache should store the given computation}
         *
         * @param computation the computation
         * @param stackTrace  the current stack trace
         */
        boolean shouldCache(IComputation<?> computation, StackTrace stackTrace);
    }

    /**
     * Configures a cache.
     */
    public static class Configuration {
        protected CachePolicy cachePolicy = CachePolicy.CACHE_NONE;

        /**
         * Configures the cache policy.
         *
         * @param cachePolicy the cache policy
         * @return this configuration
         */
        public Configuration setCachePolicy(CachePolicy cachePolicy) {
            this.cachePolicy = cachePolicy;
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
     * A {@link IComputation} of type {@code T} should be mapped to a {@link FutureResult} of the same type {@code T}.
     */
    protected final Map<IComputation<?>, FutureResult<?>> computationMap = new ConcurrentHashMap<>();

    protected final Map<IComputation<?>, Long> hitStatistics = new ConcurrentHashMap<>();

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
     * Returns the {@link FutureResult} for the {@link IComputation} from the cache if it has already been stored.
     * Otherwise, computes the {@link FutureResult} and returns it.
     * The computed {@link FutureResult} is cached if the current {@link CachePolicy} agrees.
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     */
    public <T> FutureResult<T> computeFutureResult(IComputation<T> computation) {
        if (has(computation)) {
            Feat.log().debug("cache hit for " + computation);
            hitStatistics.putIfAbsent(computation, 0L);
            hitStatistics.put(computation, hitStatistics.get(computation) + 1);
            return get(computation).get();
        }
        Feat.log().debug("cache miss for " + computation);
        FutureResult<T> futureResult = computation.computeFutureResult();
        if (configuration.cachePolicy.shouldCache(computation, new StackTrace())) {
            Feat.log().debug("cache write for " + computation);
            put(computation, futureResult);
        }
        return futureResult;
    }

    /**
     * {@return whether the given computation has been cached in this cache}
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     */
    public <T> boolean has(IComputation<T> computation) {
        return computationMap.containsKey(computation);
    }

    /**
     * {@return the cached result of a given computation, if any}
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     */
    @SuppressWarnings("unchecked")
    public <T> Result<FutureResult<T>> get(IComputation<T> computation) {
        return has(computation)
                ? Result.of((FutureResult<T>) computationMap.get(computation))
                : Result.empty();
    }

    /**
     * Sets the cached result for a given computation, if not already cached.
     * Does nothing if the computation has already been cached.
     *
     * @param computation  the computation
     * @param futureResult the future result
     * @param <T>          the type of the computation result
     * @return whether the operation affected this cache
     */
    public <T> boolean put(IComputation<T> computation, FutureResult<T> futureResult) {
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
     * @param <T>         the type of the computation result
     * @return whether the operation affected this cache
     */
    public <T> boolean remove(IComputation<T> computation) {
        if (!has(computation))
            return false;
        Feat.log().debug("cache remove for " + computation);
        computationMap.remove(computation);
        return true;
    }

    /**
     * Removes all cached computation results.
     */
    public void clear() {
        Feat.log().debug("clearing cache");
        computationMap.clear();
    }

    public Long getNumberOfHits(IComputation<?> computation) {
        return Result.ofNullable(hitStatistics.get(computation)).orElse(0L);
    }

    public List<IComputation<?>> getCachedComputations() {
        ArrayList<IComputation<?>> computations = new ArrayList<>(computationMap.keySet());
        computations.sort(Comparator.comparingInt(ITree::hashCodeTree));
        return computations;
    }

    public IComputation<List<?>> getCacheComputation() {
        return Computations.allOf(getCachedComputations());
    }

    @Override
    public Result<URI> getBrowseURI(GraphVizTreeFormat<IComputation<?>> graphVizComputationTreeFormat) {
        graphVizComputationTreeFormat.setIncludeRoot(false);
        return getCacheComputation().getBrowseURI(graphVizComputationTreeFormat);
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
