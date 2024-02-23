/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base;

import de.featjar.base.cli.Commands;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Cache;
import de.featjar.base.computation.FallbackCache;
import de.featjar.base.data.Result;
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.extension.ExtensionManager;
import de.featjar.base.extension.IExtension;
import de.featjar.base.io.IO;
import de.featjar.base.log.BufferedLog;
import de.featjar.base.log.CallerFormatter;
import de.featjar.base.log.ConfigurableLog;
import de.featjar.base.log.Log;
import de.featjar.base.log.TimeStampFormatter;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Configures, initializes, and runs FeatJAR. To use FeatJAR, create a
 * {@link FeatJAR} object and use it. After usage, call {@link #close()} or use
 * a try...with block. If only a quick computation is needed, call
 * {@link #run(Consumer)} or {@link #apply(Function)}. For convenience, this
 * class inherits all methods in {@link IO} and provides access to the
 * {@link Log} with {@link #log()} and {@link Cache} with {@link #cache()}. Both
 * {@link #log()} and {@link #cache()} return fallback instances when used
 * outside a FeatJAR instantiation. For simplicity, only one FeatJAR instance
 * can exist at a time (although this limitation may be lifted in the future).
 * Thus, do not create FeatJAR objects at the same time in different threads.
 * Also, do not nest {@link #run(Consumer)} or {@link #apply(Function)} calls.
 * However, different FeatJAR instances can be created over time in the same
 * thread (e.g., to change the configuration).
 *
 * @author Elias Kuiter
 */
public final class FeatJAR extends IO implements AutoCloseable {
    public static final String ROOT_PACKAGE_NAME = "de.featjar";
    public static final String LIBRARY_NAME = "feat.jar";

    /**
     * Configures FeatJAR.
     */
    public static class Configuration {

        /**
         * This configuration's log sub-configuration.
         */
        public final ConfigurableLog.Configuration logConfig = new ConfigurableLog.Configuration();

        /**
         * This configuration's cache sub-configuration.
         */
        public final Cache.Configuration cacheConfig = new Cache.Configuration();

        /**
         * Configures this configuration's log sub-configuration.
         *
         * @param configurationConsumer the log configuration consumer
         * @return this configuration
         */
        public Configuration log(Consumer<ConfigurableLog.Configuration> configurationConsumer) {
            configurationConsumer.accept(logConfig);
            return this;
        }

        /**
         * Configures this configuration's cache sub-configuration.
         *
         * @param configurationConsumer the cache configuration consumer
         * @return this configuration
         */
        public Configuration cache(Consumer<Cache.Configuration> configurationConsumer) {
            configurationConsumer.accept(cacheConfig);
            return this;
        }

        public void initialize() {
            FeatJAR.initialize(this);
        }
    }

    /**
     * The current instance of FeatJAR. Only one instance can exist at a time.
     */
    private static FeatJAR instance;

    private static BufferedLog fallbackLog = new BufferedLog();
    private static FallbackCache fallbackCache = new FallbackCache();

    /**
     * {@return the current FeatJAR instance}
     */
    public static FeatJAR getInstance() {
        return instance;
    }

    /**
     * {@return {@code true} if FeatJAR is initialized, {@code false} otherwise}
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Initializes FeatJAR with a default configuration.
     */
    public static FeatJAR initialize() {
        return initialize(createDefaultConfiguration());
    }

    public static Configuration configure() {
        return new Configuration();
    }

    public static Configuration createDefaultConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .logToSystemOut(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS)
                .logToSystemErr(Log.Verbosity.ERROR)
                .addFormatter(new TimeStampFormatter())
                .addFormatter(new CallerFormatter());
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

    /**
     * Initializes FeatJAR.
     *
     * @param configuration the FeatJAR configuration
     */
    public static FeatJAR initialize(Configuration configuration) {
        if (instance != null) throw new RuntimeException("FeatJAR already initialized");
        instance = new FeatJAR();
        instance.setConfiguration(configuration);
        return instance;
    }

    /**
     * Initializes FeatJAR.
     *
     * @param optionInput a list with options
     */
    public static FeatJAR initialize(OptionList optionInput) {
        if (instance != null) throw new RuntimeException("FeatJAR already initialized");
        instance = new FeatJAR();
        optionInput.parseArguments(false);
        instance.setConfiguration(optionInput.getConfiguration());
        return instance;
    }

    /**
     * De-initializes FeatJAR.
     */
    public static void deinitialize() {
        if (instance != null) {
            instance.log.debug("de-initializing FeatJAR");
            instance.extensionManager.close();
            instance.extensionManager = null;
            instance.log = null;
            instance.cache = null;
            instance = null;
        }
    }

    /**
     * This FeatJAR instance's extension manager. Holds references to all loaded
     * extension points and extensions.
     */
    private ExtensionManager extensionManager;

    private ConfigurableLog log;
    private Cache cache;

    /**
     * Initializes FeatJAR.
     *
     * @param configuration the FeatJAR configuration
     */
    private FeatJAR() {
        log().debug("initializing FeatJAR");
        extensionManager = new ExtensionManager();
    }

    private void setConfiguration(Configuration configuration) {
        ConfigurableLog newLog = getExtension(ConfigurableLog.class).orElseGet(ConfigurableLog::new);
        newLog.setConfiguration(configuration.logConfig);
        log = newLog;
        fallbackLog.flush(m -> log.log(m.getValue(), m.getKey()));

        cache = getExtension(Cache.class).orElseGet(Cache::new);
        cache.setConfiguration(configuration.cacheConfig);
    }

    /**
     * De-initializes FeatJAR.
     */
    @Override
    public void close() {
        deinitialize();
    }

    /**
     * {@return this FeatJAR instance's extension manager}
     */
    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }

    /**
     * {@return the extension point for a given class installed in this FeatJAR
     * instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public <T extends AExtensionPoint<?>> Result<T> getExtensionPoint(Class<T> klass) {
        return extensionManager.getExtensionPoint(klass);
    }

    /**
     * {@return the extension for a given class installed in this FeatJAR instance's
     * extension manager}
     *
     * @param <T>   the type of the extension's class
     * @param klass the extension's class
     */
    public <T extends IExtension> Result<T> getExtension(Class<T> klass) {
        return extensionManager.getExtension(klass);
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configuration the FeatJAR configuration
     * @param fn            the function
     */
    public static void run(Configuration configuration, Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize(configuration)) {
            fn.accept(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param fn the function
     */
    public static void run(Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize()) {
            fn.accept(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configuration the FeatJAR configuration
     * @param fn            the function
     * @return the supplied object
     */
    public static <T> T apply(Configuration configuration, Function<FeatJAR, T> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize(configuration)) {
            return fn.apply(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param fn the function
     * @return the supplied object
     */
    public static <T> T apply(Function<FeatJAR, T> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize()) {
            return fn.apply(featJAR);
        }
    }

    /**
     * {@return the extension point for a given class installed in the current
     * FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends AExtensionPoint<?>> T extensionPoint(Class<T> klass) {
        if (instance == null) throw new IllegalStateException("FeatJAR not initialized yet");
        Result<T> extensionPoint = instance.getExtensionPoint(klass);
        if (extensionPoint.isEmpty())
            throw new RuntimeException("extension point " + klass + " not currently installed in FeatJAR");
        return extensionPoint.get();
    }

    /**
     * {@return the extension point for a given class installed in the current
     * FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends IExtension> T extension(Class<T> klass) {
        if (instance == null) throw new IllegalStateException("FeatJAR not initialized yet");
        Result<T> extension = instance.getExtension(klass);
        if (extension.isEmpty())
            throw new RuntimeException("extension " + klass + " not currently installed in FeatJAR");
        return extension.get();
    }

    /**
     * {@return the current FeatJAR instance's log, or a fallback log if
     * uninitialized}
     */
    public static Log log() {
        return instance == null || instance.log == null ? fallbackLog : instance.log;
    }

    /**
     * {@return the current FeatJAR instance's cache, or a fallback cache if
     * uninitialized}
     */
    public static Cache cache() {
        return instance == null || instance.cache == null ? fallbackCache : instance.cache;
    }

    /**
     * Main entry point of FeatJAR.
     *
     * @param arguments command-line arguments
     */
    public static void main(String[] arguments) {
        try {
            OptionList optionInput = new OptionList(arguments);
            try (FeatJAR featJAR = FeatJAR.initialize(optionInput)) {
                Commands.run(optionInput);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
