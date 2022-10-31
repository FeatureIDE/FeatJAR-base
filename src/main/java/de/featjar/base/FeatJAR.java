package de.featjar.base;

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
 * If only a quick computation is needed, call {@link #apply(Function)}.
 * For convenience, this class inherits all methods in {@link IO} and provides
 * access to the {@link #log()} and {@link #store()}.
 * For simplicity, only one FeatJAR instance can exist at a time (although this limitation may be lifted in the future).
 * Thus, do not create FeatJAR objects at the same time in different threads or in nested {@link #apply(Function)} calls.
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
         * Configures the log.
         */
        protected final Log.Configuration log = new Log.Configuration();
        protected final Store.Configuration store = new Store.Configuration();

        public Configuration log(Consumer<Log.Configuration> configurationConsumer) {
            configurationConsumer.accept(log);
            return this;
        }

        public Configuration store(Consumer<Store.Configuration> configurationConsumer) {
            configurationConsumer.accept(store);
            return this;
        }
    }

    private static FeatJAR instance;
    protected final ExtensionManager extensionManager;
    protected boolean initialized;

    public static FeatJAR getInstance() {
        return instance == null ? (instance = new FeatJAR()) : instance;
    }

    /**
     * Installs FeatJAR, uninstalling it if necessary.
     *
     * @param configuration the FeatJAR configuration
     */
    public FeatJAR(Configuration configuration) {
        if (instance != null)
            throw new RuntimeException("FeatJAR already initialized");
        log().debug("initializing FeatJAR");
        instance = this;
        Log.setConfiguration(configuration.log);
        Store.setConfiguration(configuration.store);
        extensionManager = new ExtensionManager();
        initialized = true;
    }

    /**
     * Installs FeatJAR, uninstalling it if necessary.
     * The log reports only error and info messages.
     */
    public FeatJAR() {
        this(new Configuration()
                .log(cfg -> {
                    cfg.logAtMost(Log.Verbosity.DEBUG); // todo: only INFO
                    cfg.addFormatter(new TimeStampFormatter());
                    cfg.addFormatter(new CallerFormatter());
                })
                .store(cfg -> cfg.setCachingPolicy(Store.CachingPolicy.CACHE_TOP_LEVEL_COMPUTATIONS)));
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

    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }

    public <T extends ExtensionPoint<?>> Result<T> getExtensionPoint(Class<T> klass) {
        return extensionManager.getExtensionPoint(klass);
    }

    public <T extends Extension> Result<T> getExtension(Class<T> klass) {
        return extensionManager.getExtension(klass);
    }

    /**
     * {@return the log}
     */
    public Log getLog() {
        return initialized ? getExtension(Log.class).orElse(Log.BootLog::new) : new Log.BootLog();
    }

    /**
     * {@return the store}
     */
    public Store getStore() {
        return getExtension(Store.class).orElseThrow();
    }

    /**
     * Runs some function in a temporary FeatJAR installation.
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
     * Runs some function in a temporary FeatJAR installation.
     *
     * @param fn the function
     */
    public static void run(Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = new FeatJAR()) {
            fn.accept(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR installation.
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
     * Runs some function in a temporary FeatJAR installation.
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

    public static <T extends ExtensionPoint<?>> T extensionPoint(Class<T> klass) {
        if (instance == null)
            throw new RuntimeException("FeatJAR not initialized yet");
        Result<T> extensionPoint = instance.getExtensionPoint(klass);
        if (extensionPoint.isEmpty())
            throw new RuntimeException("extension point " + klass + " not currently installed in FeatJAR");
        return extensionPoint.get();
    }

    public static <T extends Extension> T extension(Class<T> klass) {
        if (instance == null)
            throw new RuntimeException("FeatJAR not initialized yet");
        Result<T> extension = instance.getExtension(klass);
        if (extension.isEmpty())
            throw new RuntimeException("extension " + klass + " not currently installed in FeatJAR");
        return extension.get();
    }

    /**
     * {@return the log}
     */
    public static Log log() {
        return instance == null ? new Log.BootLog() : instance.getLog();
    }

    /**
     * {@return the store}
     */
    public static Store store() {
        if (instance == null)
            throw new RuntimeException("FeatJAR not initialized yet");
        return instance.getStore();
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
