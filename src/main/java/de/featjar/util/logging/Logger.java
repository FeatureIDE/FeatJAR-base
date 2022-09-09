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
package de.featjar.util.logging;

import de.featjar.util.data.Problem;
import de.featjar.util.io.MultiStream;
import de.featjar.util.task.Monitor;
import de.featjar.util.task.ProgressLogger;
import de.featjar.util.task.IntervalThread;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Logs messages to standard output and files.
 * Formats log messages with {@link Formatter formatters}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Logger {
    /**
     * Types of log messages.
     */
    public enum MessageType {
        /**
         * Error message.
         * Typically used to log exceptions and warnings.
         */
        ERROR,
        /**
         * Info message.
         * Typically used to log high-level information.
         */
        INFO,
        /**
         * Debug message.
         * Typically used to log low-level information.
         */
        DEBUG,
        /**
         * Progress message.
         * Typically used to signal progress in long-running jobs.
         */
        PROGRESS
    }

    /**
     * Configures the global logger.
     */
    public static class LoggerConfiguration {
        private final HashMap<MessageType, de.featjar.util.io.PrintStream> logStreams = new HashMap<>();
        private final LinkedList<Formatter> formatters = new LinkedList<>();

        {
            Arrays.asList(MessageType.values()).forEach(messageType ->
                    logStreams.put(messageType, new de.featjar.util.io.PrintStream(new MultiStream())));
        }

        /**
         * Configures a stream to be a logging target.
         *
         * @param stream the stearm
         * @param messageTypes the logged message types
         */
        public synchronized void logToStream(PrintStream stream, MessageType... messageTypes) {
            Arrays.asList(messageTypes)
                    .forEach(messageType -> ((MultiStream) logStreams.get(messageType).getOutputStream())
                            .addStream(stream));
        }

        /**
         * Configures a file to be a logging target.
         *
         * @param path the path to the file
         * @param messageTypes the logged message types
         */
        public synchronized void logToFile(Path path, MessageType... messageTypes) {
            try {
                logToStream(new PrintStream(new FileOutputStream(path.toAbsolutePath().normalize().toFile())), messageTypes);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Configures the standard output stream to be a logging target.
         *
         * @param messageTypes the logged message types
         */
        public synchronized void logToSystemOut(MessageType... messageTypes) {
            logToStream(System.out, messageTypes);
        }

        /**
         * Configures the standard error stream to be a logging target.
         *
         * @param messageTypes the logged message types
         */
        public synchronized void logToSystemErr(MessageType... messageTypes) {
            logToStream(System.err, messageTypes);
        }

        /**
         * Configures a formatter for all logging targets.
         *
         * @param formatter the formatter
         */
        public synchronized void addFormatter(Formatter formatter) {
            formatters.add(formatter);
        }
    }

    /**
     * Manipulates a logger configuration.
     */
    public interface LoggerConfigurator extends Consumer<LoggerConfiguration> {}

    private static final PrintStream originalSystemOut = System.out;
    private static final PrintStream originalSystemErr = System.err;
    private static LoggerConfiguration loggerConfiguration;

    /**
     * Installs the global logger.
     * Overrides the standard output/error streams.
     * That is, calls to {@link System#out} are equivalent to calling {@link #logInfo(String)}.
     * Analogously, calls to {@link System#err} are equivalent to calling {@link #logError(String)}.
     *
     * @param loggerConfigurator a logger configurator
     */
    public static synchronized void install(LoggerConfigurator loggerConfigurator) {
        if (loggerConfiguration != null) {
            throw new IllegalStateException("logger already initialized");
        }
        loggerConfiguration = new LoggerConfiguration();
        loggerConfigurator.accept(loggerConfiguration);
        System.setOut(loggerConfiguration.logStreams.get(MessageType.INFO));
        System.setErr(loggerConfiguration.logStreams.get(MessageType.ERROR));
    }

    /**
     * Uninstalls the global logger.
     * Resets the standard output/error streams.
     */
    public static synchronized void uninstall() {
        if (loggerConfiguration == null) {
            throw new IllegalStateException("logger not yet initialized");
        }
        loggerConfiguration = null;
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    /**
     * Logs a list of problems as errors.
     *
     * @param problems the problems
     */
    public static void logProblems(List<Problem> problems) {
        problems.stream()
                .map(Problem::getException)
                .flatMap(Optional::stream)
                .forEach(Logger::logError);
    }

    /**
     * Logs an error message.
     *
     * @param error the error object
     */
    public static void logError(Throwable error) {
        println(error);
    }

    /**
     * Logs an error message.
     *
     * @param message the error message
     */
    public static void logError(String message) {
        println(message, MessageType.ERROR);
    }

    /**
     * Logs an info message.
     *
     * @param messageObject the message object
     */
    public static void logInfo(Object messageObject) {
        println(String.valueOf(messageObject), MessageType.INFO);
    }

    /**
     * Logs an info message.
     *
     * @param message the message
     */
    public static void logInfo(String message) {
        println(message, MessageType.INFO);
    }

    /**
     * Logs a debug message.
     *
     * @param messageObject the message object
     */
    public static void logDebug(Object messageObject) {
        println(String.valueOf(messageObject), MessageType.DEBUG);
    }

    /**
     * Logs a debug message.
     *
     * @param message the message
     */
    public static void logDebug(String message) {
        println(message, MessageType.DEBUG);
    }

    /**
     * Logs a progress message.
     *
     * @param messageObject the message object
     */
    public static void logProgress(Object messageObject) {
        println(String.valueOf(messageObject), MessageType.DEBUG);
    }

    /**
     * Logs a progress message.
     *
     * @param message the message
     */
    public static void logProgress(String message) {
        println(message, MessageType.PROGRESS);
    }

    /**
     * Logs a message.
     *
     * @param message the message
     * @param messageType the message type
     */
    public static void log(String message, MessageType messageType) {
        println(message, messageType);
    }

    private static synchronized void println(String message, MessageType messageType) {
        final String formattedMessage = formatMessage(message);
        if (loggerConfiguration != null) {
            loggerConfiguration.logStreams.get(messageType).println(formattedMessage);
        } else {
            if (messageType == MessageType.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }
    }

    private static synchronized void println(Throwable error) {
        final String formattedMessage = formatMessage(error.getMessage());
        if (loggerConfiguration != null) {
            loggerConfiguration.logStreams.get(MessageType.ERROR).println(formattedMessage);
            error.printStackTrace(loggerConfiguration.logStreams.get(MessageType.ERROR));
        } else {
            System.err.println(formattedMessage);
            error.printStackTrace(System.err);
        }
    }

    private static String formatMessage(String message) {
        if (loggerConfiguration == null || loggerConfiguration.formatters.isEmpty()) {
            return message;
        } else {
            final StringBuilder sb = new StringBuilder();
            for (final Formatter formatter : loggerConfiguration.formatters) {
                sb.append(formatter.getPrefix());
            }
            sb.append(message);
            for (final Formatter formatter : loggerConfiguration.formatters) {
                sb.append(formatter.getSuffix());
            }
            return sb.toString();
        }
    }

    /**
     * Starts a thread that regularly logs the progress of a monitor.
     *
     * @param monitor the monitor
     * @param interval the interval
     * @return the interval thread
     */
    public static IntervalThread startProgressLogger(Monitor monitor, long interval) {
        final IntervalThread intervalThread = new IntervalThread(new ProgressLogger(monitor), interval);
        intervalThread.start();
        return intervalThread;
    }
}
