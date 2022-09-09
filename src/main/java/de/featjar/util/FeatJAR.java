package de.featjar.util;

import de.featjar.util.extension.Extensions;
import de.featjar.util.logging.Logger;

import java.util.function.Consumer;

/**
 * Sets up FeatJAR.
 *
 * @author Elias Kuiter
 */
public class FeatJAR {
    /**
     * Configures FeatJAR.
     */
    public static class FeatJARConfiguration {
        /**
         * Manipulates a logger configuration.
         */
        public Logger.LoggerConfigurator loggerConfigurator;
    }

    /**
     * Manipulates a FeatJAR configuration.
     */
    public interface FeatJARConfigurator extends Consumer<FeatJARConfiguration> {
    }

    private static FeatJARConfiguration featJARConfiguration;

    /**
     * Installs all extensions and the default logger.
     * Reports only error and info messages.
     */
    public static synchronized void install() {
        install(featJARConfiguration ->
                featJARConfiguration.loggerConfigurator = loggerConfiguration -> {
                    loggerConfiguration.logToSystemErr(Logger.MessageType.ERROR);
                    loggerConfiguration.logToSystemOut(Logger.MessageType.INFO);
                });
    }

    /**
     * Installs all extensions and a custom logger.
     *
     * @param featJARConfigurator a FeatJAR configurator
     */
    public static synchronized void install(FeatJARConfigurator featJARConfigurator) {
        if (featJARConfiguration != null) {
            throw new IllegalStateException("FeatJAR already initialized");
        }
        featJARConfiguration = new FeatJARConfiguration();
        featJARConfigurator.accept(featJARConfiguration);
        Extensions.install();
        Logger.install(featJARConfiguration.loggerConfigurator);
    }

    /**
     * Uninstalls alls extensions and the logger.
     */
    public static synchronized void uninstall() {
        if (featJARConfiguration == null) {
            throw new IllegalStateException("FeatJAR not yet initialized");
        }
        featJARConfiguration = null;
        Extensions.uninstall();
        Logger.uninstall();
    }
}
