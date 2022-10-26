package de.featjar.base;

import de.featjar.base.cli.CommandLine;
import de.featjar.base.data.Store;
import de.featjar.base.extension.ExtensionManager;
import de.featjar.base.io.IO;
import de.featjar.base.log.Log;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Configures, initializes, and runs FeatJAR.
 * To use FeatJAR extensions, call {@link #install()} before using any FeatJAR functions, typically at the
 * start of a program.
 * If desired, this initialization can be undone with {@link #uninstall()}.
 * If only a quick computation is needed, call {@link #run(Runnable)}.
 * For convenience, this class inherits all methods in {@link IO} and provides access to the {@link #log()}.
 * Only one FeatJAR instance can exist at a time due to potential global mutable state in extensions.
 * Thus, do not call {@link #install()} at the same time from different threads.
 * Also, do not nest {@link #install()} calls.
 * However, different FeatJAR instances can be re-initialized across time (e.g., to change the configuration).
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
        public Log.Configuration log = new Log.Configuration();
    }

    /**
     * The current FeatJAR instance.
     */
    private static FeatJAR instance;

    /**
     * {@return the current FeatJAR instance, initializing it if needed}
     *
     * @param configuration the FeatJAR configuration
     */
    protected static FeatJAR getInstance(Configuration configuration) {
        return instance == null ? new FeatJAR(configuration) : instance;
    }

    /**
     * Removes the current FeatJAR instance.
     * The next call to {@link #getInstance(Configuration)} will create a new FeatJAR instance.
     */
    protected static void resetInstance() {
        instance = null;
    }

    protected FeatJAR(Configuration configuration) {
        log().setConfiguration(configuration.log);
        ExtensionManager.resetInstance();
        ExtensionManager.getInstance();
    }

    /**
     * Uninstalls all extensions.
     */
    @Override
    public void close() {
        ExtensionManager.getInstance().close();
        resetInstance();
    }

    /**
     * Install a new FeatJAR instance.
     *
     * @param configurationConsumer the FeatJAR configuration consumer
     */
    public static void install(Consumer<Configuration> configurationConsumer) {
        Configuration configuration = new Configuration();
        configurationConsumer.accept(configuration);
        resetInstance();
        getInstance(configuration);
    }

    /**
     * Install a new FeatJAR instance.
     * The log reports only error and info messages.
     */
    public static void install() {
        install(cfg ->
                cfg.log.logToSystemErr(Log.Verbosity.ERROR)
                        .logToSystemOut(Log.Verbosity.INFO));
    }

    /**
     * Uninstalls the current FeatJAR instance.
     */
    public static void uninstall() {
        if (instance != null) {
            instance.close();
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configurationConsumer the FeatJAR configuration consumer
     * @param runnable the runnable
     */
    public static void run(Consumer<Configuration> configurationConsumer, Runnable runnable) {
        install(configurationConsumer);
        runnable.run();
        uninstall();
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param runnable the runnable
     */
    public static void run(Runnable runnable) {
        install();
        runnable.run();
        uninstall();
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configurationConsumer the FeatJAR configuration consumer
     * @param supplier the supplier
     * @return the supplied object
     */
    public static <T> T get(Consumer<Configuration> configurationConsumer, Supplier<T> supplier) {
        install(configurationConsumer);
        T t = supplier.get();
        uninstall();
        return t;
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param supplier the supplier
     * @return the supplied object
     */
    public static <T> T get(Supplier<T> supplier) {
        install();
        T t = supplier.get();
        uninstall();
        return t;
    }

    /**
     * {@return the current log}
     */
    public static Log log() {
        return Log.getInstance();
    }

    /**
     * {@return the current store}
     */
    public static Store store() {
        return Store.getInstance();
    }

    /**
     * Main entry point of FeatJAR.

     * @param args command-line arguments
     */
    public static void main(String[] args) {
        FeatJAR.install();
        CommandLine.run(args);
    }
}
