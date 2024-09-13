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

import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Cache;
import de.featjar.base.computation.FallbackCache;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.extension.ExtensionManager;
import de.featjar.base.extension.IExtension;
import de.featjar.base.io.IO;
import de.featjar.base.log.BufferedLog;
import de.featjar.base.log.CallerFormatter;
import de.featjar.base.log.ConfigurableLog;
import de.featjar.base.log.Log;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.log.TimeStampFormatter;
import de.featjar.base.log.VerbosityFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * Main entry point of FeatJAR.
     *
     * @param arguments command-line arguments
     */
    public static void main(String[] arguments) {
        System.exit(run(arguments));
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
                .addFormatter(new VerbosityFormatter())
                .addFormatter(new CallerFormatter());
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

    public static Configuration createPanicConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .logToSystemOut(Log.Verbosity.MESSAGE)
                .logToSystemErr(Log.Verbosity.ERROR, Log.Verbosity.WARNING);
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

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

    /**
     * Initializes FeatJAR.
     *
     * @param configuration the FeatJAR configuration
     */
    public static synchronized FeatJAR initialize(Configuration configuration) {
        if (instance != null) {
            throw new RuntimeException("FeatJAR already initialized");
        }
        log().debug("initializing FeatJAR");
        instance = new FeatJAR();
        instance.extensionManager = new ExtensionManager();
        if (configuration != null) {
            instance.setConfiguration(configuration);
        }
        return instance;
    }

    /**
     * De-initializes FeatJAR.
     */
    public static synchronized void deinitialize() {
        if (instance != null) {
            FeatJAR.log().debug("de-initializing FeatJAR");

            if (instance.extensionManager != null) {
                instance.extensionManager.close();
            }

            if (instance.cache != null) {
                instance.cache.close();
            }
            instance.cache = null;

            instance.log = null;
            instance = null;
        }
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
     * Interpret arguments and run the specified command.
     *
     * @param arguments command-line arguments
     */
    public static int run(String... arguments) {
        try (FeatJAR featJAR = FeatJAR.initialize(null)) {
            return runAfterInitialization(true, arguments);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return panic();
        }
    }

    public static int runInternally(String... arguments) {
        return runAfterInitialization(false, arguments);
    }

    private static int runAfterInitialization(boolean configure, String... arguments) {
        OptionList optionInput = new OptionList(arguments);

        List<Problem> problems = optionInput.parseArguments();
        if (Problem.containsError(problems)) {
            FeatJAR.log().problems(problems);
            FeatJAR.log().problems(optionInput.parseRemainingArguments());
            FeatJAR.log().message(OptionList.getHelp(optionInput.getCommand().orElse(null)));
            return panic();
        }

        if (optionInput.isHelp()) {
            System.out.println("This is FeatJAR!");
            System.out.println(OptionList.getHelp(optionInput.getCommand().orElse(null)));
        } else if (optionInput.isVersion()) {
            System.out.println(FeatJAR.LIBRARY_NAME + ", development version");
        } else {
            FeatJAR.log().problems(problems);
            Result<ICommand> optionalCommand = optionInput.getCommand();
            if (optionalCommand.isEmpty()) {
                FeatJAR.log().error("No command provided");
                FeatJAR.log().message(OptionList.getHelp());
                return panic();
            } else {
                ICommand command = optionalCommand.get();
                FeatJAR.log().debug("Running command %s", command.getIdentifier());
                List<Problem> commandProblems =
                        optionInput.addOptions(command.getOptions()).parseRemainingArguments();
                FeatJAR.log().problems(commandProblems);
                if (Problem.containsError(commandProblems)) {
                    FeatJAR.log()
                            .message(OptionList.getHelp(optionInput.getCommand().orElse(command)));
                    return panic();
                }
                if (configure) {
                    FeatJAR.getInstance().setConfiguration(optionInput.getConfiguration());
                }
                command.run(optionInput);
            }
        }
        return 0;
    }

    private static int panic() {
        Log log = FeatJAR.log();
        if (log instanceof BufferedLog) {
            ConfigurableLog newLog = new ConfigurableLog();
            newLog.setConfiguration(createPanicConfiguration().logConfig);
            ((BufferedLog) log).flush(m -> {
                Supplier<String> originalMessage = m.getValue();
                Supplier<String> message;
                Verbosity verbosity = m.getKey();
                switch (verbosity) {
                    case DEBUG:
                        message = () -> "DEBUG: " + originalMessage.get();
                        break;
                    case ERROR:
                        message = () -> "ERROR: " + originalMessage.get();
                        break;
                    case INFO:
                        message = () -> "INFO: " + originalMessage.get();
                        break;
                    case MESSAGE:
                        message = m.getValue();
                        break;
                    case PROGRESS:
                        message = () -> "PROGRESS: " + originalMessage.get();
                        break;
                    case WARNING:
                        message = () -> "WARNING: " + originalMessage.get();
                        break;
                    default:
                        throw new IllegalStateException(String.valueOf(verbosity));
                }
                newLog.print(message, verbosity);
            });
        }
        return 1;
    }

    /**
     * {@return the extension point for a given class installed in the current
     * FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends AExtensionPoint<?>> T extensionPoint(Class<T> klass) {
        FeatJAR instance = FeatJAR.instance;
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
        FeatJAR instance = FeatJAR.instance;
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
        FeatJAR instance = FeatJAR.instance;
        return instance == null || instance.log == null ? fallbackLog : instance.log;
    }

    /**
     * {@return the current FeatJAR instance's cache, or a fallback cache if
     * uninitialized}
     */
    public static Cache cache() {
        FeatJAR instance = FeatJAR.instance;
        return instance == null || instance.cache == null ? fallbackCache : instance.cache;
    }

    /**
     * This FeatJAR instance's extension manager. Holds references to all loaded
     * extension points and extensions.
     */
    private ExtensionManager extensionManager;

    private ConfigurableLog log;
    private Cache cache;

    private void setConfiguration(Configuration configuration) {
        ConfigurableLog newLog = getExtension(ConfigurableLog.class).orElseGet(ConfigurableLog::new);
        newLog.setConfiguration(configuration.logConfig);
        log = newLog;
        fallbackLog.flush(m -> log.print(m.getValue(), m.getKey()));

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
        return extensionManager != null ? extensionManager.getExtensionPoint(klass) : Result.empty();
    }

    /**
     * {@return the extension for a given class installed in this FeatJAR instance's
     * extension manager}
     *
     * @param <T>   the type of the extension's class
     * @param klass the extension's class
     */
    public <T extends IExtension> Result<T> getExtension(Class<T> klass) {
        return extensionManager != null ? extensionManager.getExtension(klass) : Result.empty();
    }
}
