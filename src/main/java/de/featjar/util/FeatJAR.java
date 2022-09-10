package de.featjar.util;

import de.featjar.util.extension.Extensions;
import de.featjar.util.log.Logger;

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
    public static class Configuration {
        /**
         * Manipulates a configuration.
         */
        public Logger.Configurator logger;
    }

    /**
     * Manipulates a configuration.
     */
    public interface Configurator extends Consumer<Configuration> {
    }

    private static Configuration configuration;

    /**
     * Installs all extensions and the default logger.
     * Reports only error and info messages.
     */
    public static synchronized void install() {
        install(featJAR ->
                featJAR.logger = logger -> {
                    logger.logToSystemErr(Logger.MessageType.ERROR);
                    logger.logToSystemOut(Logger.MessageType.INFO);
                });
    }

    /**
     * Installs all extensions and a custom logger.
     *
     * @param configurator a configurator
     */
    public static synchronized void install(Configurator configurator) {
        if (configuration != null) {
            throw new IllegalStateException("FeatJAR already initialized");
        }
        configuration = new Configuration();
        configurator.accept(configuration);
        Extensions.install();
        Logger.install(configuration.logger);
    }

    /**
     * Uninstalls alls extensions and the logger.
     */
    public static synchronized void uninstall() {
        if (configuration == null) {
            throw new IllegalStateException("FeatJAR not yet initialized");
        }
        configuration = null;
        Extensions.uninstall();
        Logger.uninstall();
    }
}
