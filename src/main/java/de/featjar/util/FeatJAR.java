package de.featjar.util;

import de.featjar.util.extension.Extensions;
import de.featjar.util.logging.Logger;

import java.util.function.Consumer;

public class FeatJAR {
    /**
     * Installs extensions and the logger.
     * Reports only error and info messages.
     */
    public static synchronized void install() {
        install(cfg -> {
            cfg.logToSystemErr(Logger.MessageType.ERROR);
            cfg.logToSystemOut(Logger.MessageType.INFO);
        });
    }

    /**
     * Installs extensions and the logger.
     *
     * @param loggerConfigurationConsumer a callback for configuring the logger
     */
    public static synchronized void install(Consumer<Logger.LoggerConfiguration> loggerConfigurationConsumer) {
        Extensions.install();
        Logger.install(loggerConfigurationConsumer);
    }

    /**
     * Uninstalls extensions and the logger.
     */
    public static synchronized void uninstall() {
        Extensions.uninstall();
        Logger.uninstall();
    }
}
