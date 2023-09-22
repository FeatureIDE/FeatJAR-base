/*
 * Copyright (C) 2023 FeatJAR-Development-Team
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
import de.featjar.base.data.Result;
import de.featjar.base.extension.IInitializer;
import de.featjar.base.io.MultiStream;
import de.featjar.base.io.OpenPrintStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Logs messages to standard output and files. Formats log messages with
 * {@link IFormatter formatters}. TODO: instead of logging values directly, only
 * pass suppliers that are called if some log target is configured. this saves
 * time for creating log strings
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ConfigurableLog implements Log, IInitializer {

    /**
     * Configures a log.
     */
    public static class Configuration {
        private static final PrintStream originalSystemOut = System.out;
        private static final PrintStream originalSystemErr = System.err;

        // TODO: to make this more general, we could use an OutputMapper here to
        // log to anything supported by an OutputMapper (even a ZIP file).
        protected final LinkedHashMap<Verbosity, OpenPrintStream> logStreams = Maps.empty();
        protected final LinkedList<IFormatter> formatters = new LinkedList<>();

        public Configuration() {
            resetLogStreams();
        }

        public Configuration resetLogStreams() {
            logStreams.clear();
            Arrays.asList(Verbosity.values())
                    .forEach(verbosity -> logStreams.put(verbosity, new OpenPrintStream(new MultiStream())));
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
                    new PrintStream(
                            new FileOutputStream(
                                    path.toAbsolutePath().normalize().toFile()),
                            false,
                            StandardCharsets.UTF_8),
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
            logToStream(originalSystemOut, verbosities);
            return this;
        }

        /**
         * Configures the standard error stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToSystemErr(Verbosity... verbosities) {
            logToStream(originalSystemErr, verbosities);
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
                case MESSAGE:
                    logToSystemOut(Verbosity.MESSAGE);
                    break;
                case ERROR:
                    logToSystemErr(Verbosity.ERROR);
                    logToSystemOut(Verbosity.MESSAGE);
                    break;
                case WARNING:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE);
                    break;
                case INFO:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO);
                    break;
                case DEBUG:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.MESSAGE, Verbosity.DEBUG);
                    break;
                case PROGRESS:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.DEBUG, Verbosity.PROGRESS);
                    break;
                case ALL:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.DEBUG, Verbosity.PROGRESS);
                    break;
                default:
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

    private static final PrintStream originalSystemOut = System.out;
    private static final PrintStream originalSystemErr = System.err;

    private Configuration configuration;

    /**
     * Creates a log based on the default configuration.
     */
    public ConfigurableLog() {}

    /**
     * Creates a log. Overrides the standard output/error streams. That is, calls to
     * {@link System#out} are equivalent to calling {@link #info(String)}.
     * Analogously, calls to {@link System#err} are equivalent to calling
     * {@link #error(String)}.
     *
     * @param configuration the configuration
     */
    public ConfigurableLog(Configuration configuration) {
        setConfiguration(configuration);
    }

    /**
     * {@inheritDoc} Resets the standard output/error streams.
     */
    @Override
    public void close() {
        if (configuration != null) {
            FeatJAR.log().debug("de-initializing log");
            System.setOut(originalSystemOut);
            System.setErr(originalSystemErr);
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
        if (configuration != null) {
            System.setOut(configuration.logStreams.get(Verbosity.MESSAGE));
            System.setErr(configuration.logStreams.get(Verbosity.ERROR));
        }
    }

    @Override
    public void println(String message, Verbosity verbosity) {
        final String formattedMessage = formatMessage(message);
        if (formattedMessage != null) {
            if (configuration != null) {
                configuration.logStreams.get(verbosity).println(formattedMessage);
            } else {
                if (verbosity == Verbosity.WARNING || verbosity == Verbosity.ERROR) {
                    originalSystemErr.println(formattedMessage);
                } else {
                    originalSystemOut.println(formattedMessage);
                }
            }
        }
    }

    @Override
    public void println(Throwable error, boolean isWarning) {
        final String formattedMessage = formatMessage(error.getMessage());
        Verbosity verbosity = isWarning ? Verbosity.WARNING : Verbosity.ERROR;
        if (formattedMessage != null) {
            if (configuration != null) {
                configuration.logStreams.get(verbosity).println(formattedMessage);
                error.printStackTrace(configuration.logStreams.get(verbosity));
            } else {
                originalSystemErr.println(formattedMessage);
                error.printStackTrace(originalSystemErr);
            }
        }
    }

    private String formatMessage(String message) {
        if (configuration == null || configuration.formatters.isEmpty()) {
            return message;
        } else {
            final StringBuilder sb = new StringBuilder();
            final ListIterator<IFormatter> it = configuration.formatters.listIterator();
            while (it.hasNext()) {
                sb.append(it.next().getPrefix());
            }
            sb.append(message);
            while (it.hasPrevious()) {
                sb.append(it.previous().getSuffix());
            }
            return sb.toString();
        }
    }
}
