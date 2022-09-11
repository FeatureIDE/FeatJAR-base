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
package de.featjar.base.log;

import de.featjar.base.data.Problem;
import de.featjar.base.extension.Extension;
import de.featjar.base.io.MultiStream;
import de.featjar.base.task.Monitor;
import de.featjar.base.task.ProgressLogger;
import de.featjar.base.task.IntervalThread;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Logs messages to standard output and files.
 * Formats log messages with {@link Formatter formatters}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Log implements Extension {
    /**
     * Verbosity of the log.
     */
    public enum Verbosity {
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
     * Configures a log.
     */
    public static class Configuration {
        //todo: consider using an OutputMapper instead?
        protected final HashMap<Verbosity, de.featjar.base.io.PrintStream> logStreams = new HashMap<>();
        protected final LinkedList<Formatter> formatters = new LinkedList<>();

        {
            Arrays.asList(Verbosity.values()).forEach(verbosity ->
                    logStreams.put(verbosity, new de.featjar.base.io.PrintStream(new MultiStream())));
        }

        /**
         * Configures a stream to be a logging target.
         *
         * @param stream the stearm
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public synchronized Configuration logToStream(PrintStream stream, Verbosity... verbosities) {
            Arrays.asList(verbosities)
                    .forEach(verbosity -> ((MultiStream) logStreams.get(verbosity).getOutputStream())
                            .addStream(stream));
            return this;
        }

        /**
         * Configures a file to be a logging target.
         *
         * @param path the path to the file
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public synchronized Configuration logToFile(Path path, Verbosity... verbosities) {
            try {
                logToStream(new PrintStream(new FileOutputStream(path.toAbsolutePath().normalize().toFile())), verbosities);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        /**
         * Configures the standard output stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public synchronized Configuration logToSystemOut(Verbosity... verbosities) {
            logToStream(System.out, verbosities);
            return this;
        }

        /**
         * Configures the standard error stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public synchronized Configuration logToSystemErr(Verbosity... verbosities) {
            logToStream(System.err, verbosities);
            return this;
        }

        /**
         * Configures a formatter for all logging targets.
         *
         * @param formatter the formatter
         * @return this configuration
         */
        public synchronized Configuration addFormatter(Formatter formatter) {
            formatters.add(formatter);
            return this;
        }
    }

    private static final Log INSTANCE = new Log();
    protected final PrintStream originalSystemOut = System.out;
    protected final PrintStream originalSystemErr = System.err;
    protected Configuration configuration = new Configuration();

    protected Log() {
    }

    /**
     * {@return the global log}
     */
    public static Log getInstance() {
        return INSTANCE;
    }

    /**
     * {@return this log's configuration}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets this log's configuration.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Installs this log.
     * Overrides the standard output/error streams.
     * That is, calls to {@link System#out} are equivalent to calling {@link #info(String)}.
     * Analogously, calls to {@link System#err} are equivalent to calling {@link #error(String)}.
     */
    @Override
    public boolean install() {
        System.setOut(configuration.logStreams.get(Verbosity.INFO));
        System.setErr(configuration.logStreams.get(Verbosity.ERROR));
        return true;
    }

    /**
     * Uninstalls this log.
     * Resets the standard output/error streams.
     */
    @Override
    public void uninstall() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    /**
     * Logs a list of problems as errors.
     *
     * @param problems the problems
     */
    public void problems(List<Problem> problems) {
        problems.stream()
                .map(Problem::getException)
                .flatMap(Optional::stream)
                .forEach(this::error);
    }

    /**
     * Logs an error message.
     *
     * @param error the error object
     */
    public void error(Throwable error) {
        println(error);
    }

    /**
     * Logs an error message.
     *
     * @param message the error message
     */
    public void error(String message) {
        println(message, Verbosity.ERROR);
    }

    /**
     * Logs an info message.
     *
     * @param messageObject the message object
     */
    public void info(Object messageObject) {
        println(String.valueOf(messageObject), Verbosity.INFO);
    }

    /**
     * Logs an info message.
     *
     * @param message the message
     */
    public void info(String message) {
        println(message, Verbosity.INFO);
    }

    /**
     * Logs a debug message.
     *
     * @param messageObject the message object
     */
    public void debug(Object messageObject) {
        println(String.valueOf(messageObject), Verbosity.DEBUG);
    }

    /**
     * Logs a debug message.
     *
     * @param message the message
     */
    public void debug(String message) {
        println(message, Verbosity.DEBUG);
    }

    /**
     * Logs a progress message.
     *
     * @param messageObject the message object
     */
    public void progress(Object messageObject) {
        println(String.valueOf(messageObject), Verbosity.DEBUG);
    }

    /**
     * Logs a progress message.
     *
     * @param message the message
     */
    public void progress(String message) {
        println(message, Verbosity.PROGRESS);
    }

    /**
     * Logs a message.
     *
     * @param message the message
     * @param verbosity the verbosities
     */
    public void log(String message, Verbosity verbosity) {
        println(message, verbosity);
    }

    protected void println(String message, Verbosity verbosity) {
        final String formattedMessage = formatMessage(message);
        if (configuration != null) {
            configuration.logStreams.get(verbosity).println(formattedMessage);
        } else {
            if (verbosity == Verbosity.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }
    }

    protected void println(Throwable error) {
        final String formattedMessage = formatMessage(error.getMessage());
        if (configuration != null) {
            configuration.logStreams.get(Verbosity.ERROR).println(formattedMessage);
            error.printStackTrace(configuration.logStreams.get(Verbosity.ERROR));
        } else {
            System.err.println(formattedMessage);
            error.printStackTrace(System.err);
        }
    }

    protected String formatMessage(String message) {
        if (configuration == null || configuration.formatters.isEmpty()) {
            return message;
        } else {
            final StringBuilder sb = new StringBuilder();
            for (final Formatter formatter : configuration.formatters) {
                sb.append(formatter.getPrefix());
            }
            sb.append(message);
            for (final Formatter formatter : configuration.formatters) {
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
    public IntervalThread startProgressLogger(Monitor monitor, long interval) {
        final IntervalThread intervalThread = new IntervalThread(new ProgressLogger(monitor), interval);
        intervalThread.start();
        return intervalThread;
    }
}
