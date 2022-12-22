package de.featjar.base;

import de.featjar.base.cli.ArgumentParser;
import de.featjar.base.cli.CommandLineInterface;
import de.featjar.base.computation.DependencyManager;
import de.featjar.base.data.Result;
import de.featjar.base.computation.Cache;
import de.featjar.base.extension.IExtension;
import de.featjar.base.extension.ExtensionManager;
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.io.IO;
import de.featjar.base.log.CallerFormatter;
import de.featjar.base.log.Log;
import de.featjar.base.log.TimeStampFormatter;

import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Configures, initializes, and runs FeatJAR.
 * To use FeatJAR, create a {@link FeatJAR} object and use it.
 * After usage, call {@link #close()} or use a try...with block.
 * If only a quick computation is needed, call {@link #run(Consumer)} or {@link #apply(Function)}.
 * For convenience, this class inherits all methods in {@link IO} and provides
 * access to the {@link Log} with {@link #log()} and {@link Cache} with {@link #cache()}.
 * Both  {@link #log()} and {@link #cache()} return fallback instances when used outside a FeatJAR instantiation.
 * For simplicity, only one FeatJAR instance can exist at a time (although this limitation may be lifted in the future).
 * Thus, do not create FeatJAR objects at the same time in different threads.
 * Also, do not nest {@link #run(Consumer)} or {@link #apply(Function)} calls.
 * However, different FeatJAR instances can be created over time in the same thread (e.g., to change the configuration).
 *
 * @author Elias Kuiter
 */
public class FeatJAR extends IO implements AutoCloseable {
    public static final String ROOT_PACKAGE_NAME = "de.featjar";
    public static final String LIBRARY_NAME = "feat.jar";

    /**
     * Configures FeatJAR.
     */
    public static class Configuration {
        /**
         * This configuration's log sub-configuration.
         */
        protected final Log.Configuration log = new Log.Configuration();

        /**
         * This configuration's cache sub-configuration.
         */
        protected final Cache.Configuration cache = new Cache.Configuration();

        /**
         * Configures this configuration's log sub-configuration.
         *
         * @param configurationConsumer the log configuration consumer
         * @return this configuration
         */
        public Configuration log(Consumer<Log.Configuration> configurationConsumer) {
            configurationConsumer.accept(log);
            return this;
        }

        /**
         * Configures this configuration's cache sub-configuration.
         *
         * @param configurationConsumer the cache configuration consumer
         * @return this configuration
         */
        public Configuration cache(Consumer<Cache.Configuration> configurationConsumer) {
            configurationConsumer.accept(cache);
            return this;
        }
    }

    /**
     * The current instance of FeatJAR.
     * Only one instance can exist at a time.
     */
    private static FeatJAR instance;

    /**
     * This FeatJAR instance's extension manager.
     * Holds references to all loaded extension points and extensions.
     */
    protected final ExtensionManager extensionManager;

    /**
     * Set to {@code true} after this FeatJAR instance has been initialized.
     */
    protected boolean initialized;

    /**
     * The default verbosity of FeatJAR, if not adjusted otherwise.
     * Can be set at startup to allow showing log output even before this value is adjusted.
     */
    public static Log.Verbosity defaultVerbosity;

    /**
     * Configures the default log configuration, if not adjusted otherwise.
     */
    public static final Function<Log.Configuration, Log.Configuration> defaultLogConfiguration =
            cfg -> cfg
                    .logAtMost(defaultVerbosity)
                    .addFormatter(new TimeStampFormatter())
                    .addFormatter(new CallerFormatter());

    /**
     * Configures the default cache configuration, if not adjusted otherwise.
     */
    public static final Function<Cache.Configuration, Cache.Configuration> defaultCacheConfiguration =
            cfg -> cfg
                    .setCachingPolicy(Cache.CachingPolicy.CACHE_TOP_LEVEL);

    /**
     * {@return the current FeatJAR instance}
     */
    public static FeatJAR getInstance() {
        return instance == null ? (instance = new FeatJAR()) : instance;
    }

    /**
     * Initializes FeatJAR.
     *
     * @param configuration the FeatJAR configuration
     */
    public FeatJAR(Configuration configuration) {
        if (instance != null)
            throw new RuntimeException("FeatJAR already initialized");
        log().debug("initializing FeatJAR");
        instance = this;
        Log.setDefaultConfiguration(configuration.log);
        Cache.setDefaultConfiguration(configuration.cache);
        extensionManager = new ExtensionManager();
        initialized = true;
    }

    /**
     * Initializes FeatJAR.
     * The log reports only error and info messages.
     */
    public FeatJAR() {
        this(new Configuration()
                .log(defaultLogConfiguration::apply)
                .cache(defaultCacheConfiguration::apply));
    }

    /**
     * De-initializes FeatJAR.
     */
    @Override
    public void close() {
        log().debug("de-initializing FeatJAR");
        instance = null;
        extensionManager.close();
    }

    /**
     * {@return this FeatJAR instance's extension manager}
     */
    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }

    /**
     * {@return the extension point for a given class installed in this FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public <T extends AExtensionPoint<?>> Result<T> getExtensionPoint(Class<T> klass) {
        return extensionManager.getExtensionPoint(klass);
    }

    /**
     * {@return the extension for a given class installed in this FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension's class
     * @param klass the extension's class
     */
    public <T extends IExtension> Result<T> getExtension(Class<T> klass) {
        return extensionManager.getExtension(klass);
    }

    /**
     * {@return this FeatJAR instance's log, or a fallback log if uninitialized}
     */
    public Log getLog() {
        return initialized ? getExtension(Log.class).orElse(Log.Fallback::new) : new Log.Fallback();
    }

    /**
     * {@return this FeatJAR instance's cache, or a fallback cache if uninitialized}
     */
    public Cache getCache() {
        return initialized ? getExtension(Cache.class).orElse(Cache.Fallback::new) : new Cache.Fallback();
    }

    /**
     * {@return this FeatJAR instance's dependency manager}
     */
    public DependencyManager getDependencyManager() {
        if (initialized) return getExtension(DependencyManager.class).orElseThrow();
        throw new IllegalStateException();
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configuration the FeatJAR configuration
     * @param fn            the function
     */
    public static void run(Configuration configuration, Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = new FeatJAR(configuration)) {
            fn.accept(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param fn the function
     */
    public static void run(Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = new FeatJAR()) {
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
        T t;
        try (FeatJAR featJAR = new FeatJAR(configuration)) {
            t = fn.apply(featJAR);
        }
        return t;
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param fn the function
     * @return the supplied object
     */
    public static <T> T apply(Function<FeatJAR, T> fn) {
        T t;
        try (FeatJAR featJAR = new FeatJAR()) {
            t = fn.apply(featJAR);
        }
        return t;
    }

    /**
     * {@return the extension point for a given class installed in the current FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends AExtensionPoint<?>> T extensionPoint(Class<T> klass) {
        if (instance == null)
            throw new RuntimeException("FeatJAR not initialized yet");
        Result<T> extensionPoint = instance.getExtensionPoint(klass);
        if (extensionPoint.isEmpty())
            throw new RuntimeException("extension point " + klass + " not currently installed in FeatJAR");
        return extensionPoint.get();
    }

    /**
     * {@return the extension point for a given class installed in the current FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends IExtension> T extension(Class<T> klass) {
        if (instance == null)
            throw new RuntimeException("FeatJAR not initialized yet");
        Result<T> extension = instance.getExtension(klass);
        if (extension.isEmpty())
            throw new RuntimeException("extension " + klass + " not currently installed in FeatJAR");
        return extension.get();
    }

    /**
     * {@return the current FeatJAR instance's log, or a fallback log if uninitialized}
     */
    public static Log log() {
        return instance == null ? new Log.Fallback() : instance.getLog();
    }

    /**
     * {@return the current FeatJAR instance's cache, or a fallback cache if uninitialized}
     */
    public static Cache cache() {
        return instance == null ? new Cache.Fallback() : instance.getCache();
    }

    /**
     * {@return the current FeatJAR instance's dependency manager}
     */
    public static DependencyManager dependencyManager() {
        if (instance == null) throw new IllegalStateException();
        return instance.getDependencyManager();
    }

    /**
     * Main entry point of FeatJAR.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        ArgumentParser argumentParser = new ArgumentParser(args);
        defaultVerbosity = argumentParser.getVerbosity();
        FeatJAR.run(featJAR -> CommandLineInterface.run(argumentParser));
    }
}
