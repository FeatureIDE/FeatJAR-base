package de.featjar.base;

import de.featjar.base.bin.HostEnvironment;
import de.featjar.base.cli.CommandLine;
import de.featjar.base.data.Result;
import de.featjar.base.data.Store;
import de.featjar.base.extension.Extension;
import de.featjar.base.extension.ExtensionManager;
import de.featjar.base.extension.ExtensionPoint;
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
 * access to the {@link Log} with {@link #log()} and {@link Store} with {@link #store()}.
 * Both  {@link #log()} and {@link #store()} return fallback instances when used outside a FeatJAR instantiation.
 * For simplicity, only one FeatJAR instance can exist at a time (although this limitation may be lifted in the future).
 * Thus, do not create FeatJAR objects at the same time in different threads.
 * Also, do not nest {@link #run(Consumer)} or {@link #apply(Function)} calls.
 * However, different FeatJAR instances can be created over time in the same thread (e.g., to change the configuration).
 *
 * @author Elias Kuiter
 */
public class FeatJAR extends IO implements AutoCloseable {

    /**
     * Configures FeatJAR.
     */
    public static class Configuration {
        /**
         * This configuration's log sub-configuration.
         */
        protected final Log.Configuration log = new Log.Configuration();

        /**
         * This configuration's store sub-configuration.
         */
        protected final Store.Configuration store = new Store.Configuration();

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
         * Configures this configuration's store sub-configuration.
         *
         * @param configurationConsumer the store configuration consumer
         * @return this configuration
         */
        public Configuration store(Consumer<Store.Configuration> configurationConsumer) {
            configurationConsumer.accept(store);
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
     * To allow showing log output even before this value is adjusted, an environment variable can be used.
     */
    public static final Log.Verbosity defaultVerbosity = Log.Verbosity.of(HostEnvironment.FEATJAR_VERBOSITY);

    /**
     * Configures the default log configuration, if not adjusted otherwise.
     */
    public static final Function<Log.Configuration, Log.Configuration> defaultLogConfiguration =
            cfg -> cfg
                    .logAtMost(defaultVerbosity)
                    .addFormatter(new TimeStampFormatter())
                    .addFormatter(new CallerFormatter());

    /**
     * Configures the default store configuration, if not adjusted otherwise.
     */
    public static final Function<Store.Configuration, Store.Configuration> defaultStoreConfiguration =
            cfg -> cfg
                    .setCachingPolicy(Store.CachingPolicy.CACHE_TOP_LEVEL);

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
        Store.setDefaultConfiguration(configuration.store);
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
                .store(defaultStoreConfiguration::apply));
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
    public <T extends ExtensionPoint<?>> Result<T> getExtensionPoint(Class<T> klass) {
        return extensionManager.getExtensionPoint(klass);
    }

    /**
     * {@return the extension for a given class installed in this FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension's class
     * @param klass the extension's class
     */
    public <T extends Extension> Result<T> getExtension(Class<T> klass) {
        return extensionManager.getExtension(klass);
    }

    /**
     * {@return this FeatJAR instance's log, or a fallback log if uninitialized}
     */
    public Log getLog() {
        return initialized ? getExtension(Log.class).orElse(Log.Fallback::new) : new Log.Fallback();
    }

    /**
     * {@return this FeatJAR instance's store, or a fallback store if uninitialized}
     */
    public Store getStore() {
        return initialized ? getExtension(Store.class).orElse(Store.Fallback::new) : new Store.Fallback();
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
    public static <T extends ExtensionPoint<?>> T extensionPoint(Class<T> klass) {
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
    public static <T extends Extension> T extension(Class<T> klass) {
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
     * {@return the current FeatJAR instance's store, or a fallback store if uninitialized}
     */
    public static Store store() {
        return instance == null ? new Store.Fallback() : instance.getStore();
    }

    /**
     * Main entry point of FeatJAR.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        FeatJAR.run(featJAR -> CommandLine.run(args));
    }
}
