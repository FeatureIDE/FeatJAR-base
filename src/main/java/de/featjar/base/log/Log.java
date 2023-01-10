/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
package de.featjar.base.log;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.extension.IInitializer;
import de.featjar.base.io.MultiStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Logs messages to standard output and files.
 * Formats log messages with {@link IFormatter formatters}.
 * TODO: add log methods accepting lambdas (Supplier of String) to avoid creating strings when log is disabled
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Log implements IInitializer {
    /**
     * Logging verbosity.
     * Each verbosity (save for {@link Verbosity#NONE}) defines a type of message that can be logged.
     * In addition, defines a log level that includes all log messages of the message type and all types above.
     */
    public enum Verbosity {
        /**
         * Indicates that no messages should be logged.
         */
        NONE,
        /**
         * Error message.
         * Typically used to log critical exceptions and errors.
         */
        ERROR,
        /**
         * Warning message.
         * Typically used to log non-critical warnings.
         */
        WARNING,
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
        PROGRESS;

        public static boolean isValid(String verbosityString) {
            String[] verbosities = new String[] {"none", "error", "warning", "info", "debug", "progress"};
            return Arrays.asList(verbosities).contains(verbosityString);
        }

        public static Result<Verbosity> of(String verbosityString) {
            if (!isValid(verbosityString)) return Result.empty();
            return Result.of(Verbosity.valueOf(verbosityString.toUpperCase()));
        }
    }

    /**
     * Configures a log.
     */
    public static class Configuration {
        // TODO: to make this more general, we could use an OutputMapper here to
        //  log to anything supported by an OutputMapper (even a ZIP file).
        protected final LinkedHashMap<Verbosity, de.featjar.base.io.PrintStream> logStreams = Maps.empty();
        protected final LinkedList<IFormatter> formatters = new LinkedList<>();

        {
            resetLogStreams();
        }

        public Configuration resetLogStreams() {
            logStreams.clear();
            Arrays.asList(Verbosity.values())
                    .forEach(verbosity ->
                            logStreams.put(verbosity, new de.featjar.base.io.PrintStream(new MultiStream())));
            return this;
        }

        /**
         * Configures a stream to be a logging target.
         *
         * @param stream      the stream
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToStream(PrintStream stream, Verbosity... verbosities) {
            Arrays.asList(verbosities)
                    .forEach(verbosity ->
                            ((MultiStream) logStreams.get(verbosity).getOutputStream()).addStream(stream));
            return this;
        }

        /**
         * Configures a file to be a logging target.
         *
         * @param path        the path to the file
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToFile(Path path, Verbosity... verbosities) throws FileNotFoundException {
            logToStream(
                    new PrintStream(new FileOutputStream(
                            path.toAbsolutePath().normalize().toFile())),
                    verbosities);
            return this;
        }

        /**
         * Configures the standard output stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToSystemOut(Verbosity... verbosities) {
            logToStream(System.out, verbosities);
            return this;
        }

        /**
         * Configures the standard error stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToSystemErr(Verbosity... verbosities) {
            logToStream(System.err, verbosities);
            return this;
        }

        /**
         * Configures a formatter for all logging targets.
         *
         * @param formatter the formatter
         * @return this configuration
         */
        public Configuration addFormatter(IFormatter formatter) {
            formatters.add(formatter);
            return this;
        }

        public Configuration logAtMost(Verbosity verbosity) {
            switch (verbosity) {
                case NONE:
                    break;
                case ERROR:
                    logToSystemErr(Verbosity.ERROR);
                    break;
                case WARNING:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    break;
                case INFO:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Log.Verbosity.INFO);
                    break;
                case DEBUG:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Log.Verbosity.INFO, Log.Verbosity.DEBUG);
                    break;
                case PROGRESS:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Log.Verbosity.INFO, Log.Verbosity.DEBUG, Log.Verbosity.PROGRESS);
                    break;
            }
            return this;
        }

        public Configuration logAtMost(String verbosityString) {
            Result<Verbosity> verbosity = Verbosity.of(verbosityString);
            if (verbosity.isEmpty()) throw new IllegalArgumentException("invalid verbosity " + verbosityString);
            logAtMost(verbosity.get());
            return this;
        }
    }

    protected final PrintStream originalSystemOut = System.out;
    protected final PrintStream originalSystemErr = System.err;
    protected static Configuration defaultConfiguration = null;
    protected Configuration configuration;

    /**
     * Sets the default configuration used for new logs.
     *
     * @param defaultConfiguration the default configuration
     */
    public static void setDefaultConfiguration(Configuration defaultConfiguration) {
        FeatJAR.log().debug("setting new default log configuration");
        Log.defaultConfiguration = defaultConfiguration;
    }

    /**
     * Creates a log based on the default configuration.
     */
    public Log() {
        this(defaultConfiguration);
    }

    /**
     * Creates a log.
     * Overrides the standard output/error streams.
     * That is, calls to {@link System#out} are equivalent to calling {@link #info(String)}.
     * Analogously, calls to {@link System#err} are equivalent to calling {@link #error(String)}.
     *
     * @param configuration the configuration
     */
    public Log(Configuration configuration) {
        this.configuration = configuration;
        redirectSystemStreams(configuration);
    }

    /**
     * {@inheritDoc}
     * Resets the standard output/error streams.
     */
    @Override
    public void close() {
        if (configuration != null) {
            FeatJAR.log().debug("de-initializing log");
            System.setOut(originalSystemOut);
            System.setErr(originalSystemErr);
        }
    }

    protected static void redirectSystemStreams(Configuration configuration) {
        if (configuration != null) {
            FeatJAR.log().debug("initializing log");
            System.setOut(configuration.logStreams.get(Verbosity.INFO));
            System.setErr(configuration.logStreams.get(Verbosity.ERROR));
        }
    }

    /**
     * {@return this log's configuration}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets this log's configuration.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        FeatJAR.log().debug("setting new log configuration");
        this.configuration = configuration;
        redirectSystemStreams(configuration);
    }

    /**
     * Logs a problem.
     *
     * @param problem the problem
     */
    public void problem(Problem problem) {
        switch (problem.getSeverity()) {
            case ERROR:
                error(problem.getException());
                break;
            case WARNING:
                warning(problem.getException()); // todo: pass as throwable?
                break;
        }
    }

    /**
     * Logs problems.
     *
     * @param problems the problems
     */
    public void problem(List<Problem> problems) {
        problems.forEach(this::problem);
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
     * Logs an error message.
     *
     * @param error the error object
     */
    public void error(Throwable error) {
        println(error);
    }

    /**
     * Logs a warning message.
     *
     * @param message the warning message
     */
    public void warning(String message) {
        println(message, Verbosity.WARNING);
    }

    /**
     * Logs a warning message.
     *
     * @param messageObject the message object
     */
    public void warning(Object messageObject) {
        warning(String.valueOf(messageObject));
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
     * Logs an info message.
     *
     * @param messageObject the message object
     */
    public void info(Object messageObject) {
        info(String.valueOf(messageObject));
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
     * Logs a debug message.
     *
     * @param messageObject the message object
     */
    public void debug(Object messageObject) {
        debug(String.valueOf(messageObject));
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
     * Logs a progress message.
     *
     * @param messageObject the message object
     */
    public void progress(Object messageObject) {
        progress(String.valueOf(messageObject));
    }

    /**
     * Logs a message.
     *
     * @param message   the message
     * @param verbosity the verbosities
     */
    public void log(String message, Verbosity verbosity) {
        println(message, verbosity);
    }

    protected void println(String message, Verbosity verbosity) {
        final String formattedMessage = formatMessage(message);
        if (formattedMessage != null) {
            if (configuration != null) {
                configuration.logStreams.get(verbosity).println(formattedMessage);
            } else {
                if (verbosity == Verbosity.WARNING || verbosity == Verbosity.ERROR) {
                    System.err.println(formattedMessage);
                } else {
                    System.out.println(formattedMessage);
                }
            }
        }
    }

    protected void println(Throwable error) {
        final String formattedMessage = formatMessage(error.getMessage());
        if (formattedMessage != null) {
            if (configuration != null) {
                configuration.logStreams.get(Verbosity.ERROR).println(formattedMessage);
                error.printStackTrace(configuration.logStreams.get(Verbosity.ERROR));
            } else {
                System.err.println(formattedMessage);
                error.printStackTrace(System.err);
            }
        }
    }

    protected String formatMessage(String message) {
        if (configuration == null || configuration.formatters.isEmpty()) {
            return message;
        } else {
            final StringBuilder sb = new StringBuilder();
            for (final IFormatter formatter : configuration.formatters) {
                sb.append(formatter.getPrefix());
            }
            sb.append(message);
            for (final IFormatter formatter : configuration.formatters) {
                sb.append(formatter.getSuffix());
            }
            return sb.toString();
        }
    }

    /**
     * Logs messages during (de-)initialization of FeatJAR, as there is no {@link Log} configured then.
     */
    public static class Fallback extends Log {
        IFormatter timeStampFormatter = new TimeStampFormatter();
        IFormatter callerFormatter = new CallerFormatter();

        public Fallback() {
            super(null);
        }

        @Override
        protected String formatMessage(String message) {
            return FeatJAR.defaultVerbosity.compareTo(Verbosity.DEBUG) >= 0
                    ? timeStampFormatter.getPrefix() + callerFormatter.getPrefix() + message
                    : null;
        }
    }
}
