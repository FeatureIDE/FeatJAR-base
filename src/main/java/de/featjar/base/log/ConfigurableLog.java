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
package de.featjar.base.log;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Maps;
import de.featjar.base.extension.IInitializer;
import de.featjar.base.io.MultiStream;
import de.featjar.base.io.OpenPrintStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Supplier;

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

    private static final PrintStream originalSystemOut = System.out;
    private static final PrintStream originalSystemErr = System.err;

    /**
     * Configures a log.
     */
    public static class Configuration {
        // TODO: to make this more general, we could use an OutputMapper here to
        // log to anything supported by an OutputMapper (even a ZIP file).
        protected final LinkedHashMap<Verbosity, OpenPrintStream> logStreams = Maps.empty();
        protected final LinkedList<IFormatter> formatters = new LinkedList<>();
        protected boolean printStacktrace = false;

        public Configuration() {
            resetLogStreams();
        }

        public boolean isPrintStacktrace() {
            return printStacktrace;
        }

        public Configuration setPrintStacktrace(boolean printStacktrace) {
            this.printStacktrace = printStacktrace;
            return this;
        }

        public Configuration resetLogStreams() {
            logStreams.clear();
            return this;
        }

        private void addStream(OutputStream stream, Verbosity verbostiy) {
            OpenPrintStream multiStream = logStreams.get(verbostiy);
            if (multiStream == null) {
                multiStream = new OpenPrintStream(new MultiStream(stream));
                logStreams.put(verbostiy, multiStream);
            } else {
                ((MultiStream) multiStream.getOutputStream()).addStream(stream);
            }
        }

        /**
         * Configures a stream to be a logging target.
         *
         * @param stream      the stream
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToStream(PrintStream stream, Verbosity... verbosities) {
            Objects.requireNonNull(stream);
            for (Verbosity verbosity : verbosities) {
                addStream(stream, verbosity);
            }
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
            Objects.requireNonNull(path);
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
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.DEBUG);
                    break;
                case PROGRESS:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.PROGRESS);
                    break;
                default:
                    break;
            }
            return this;
        }

        public Configuration logAll() {
            logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
            logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.DEBUG, Verbosity.PROGRESS);
            return this;
        }
    }

    private Configuration configuration;
    private int progressSize;
    /**
     * Creates a log based on the default configuration.
     */
    public ConfigurableLog() {}

    /**
     * Creates a log. Overrides the standard output/error streams. That is, calls to
     * {@link System#out} are equivalent to calling {@link #info(String, Object...)}.
     * Analogously, calls to {@link System#err} are equivalent to calling
     * {@link Log#error(String, Object...)}.
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

    public void println(Supplier<String> message, Verbosity verbosity) {
        if (configuration == null) {
            println(originalSystemErr, message.get());
        } else {
            OpenPrintStream multiStream = configuration.logStreams.get(verbosity);
            if (multiStream != null) {
                final String formattedMessage = formatMessage(message.get(), verbosity);
                println(multiStream, formattedMessage);
            }
        }
    }

    public void print(Supplier<String> message, Verbosity verbosity) {
        if (configuration == null) {
            print(originalSystemErr, message.get());
        } else {
            OpenPrintStream multiStream = configuration.logStreams.get(verbosity);
            if (multiStream != null) {
                final String formattedMessage = formatMessage(message.get(), verbosity);
                print(multiStream, formattedMessage);
            }
        }
    }

    public void printProgress(Supplier<String> message) {
        if (configuration == null) {
            printProgress(originalSystemOut, message.get());
        } else {
            OpenPrintStream multiStream = configuration.logStreams.get(Verbosity.PROGRESS);
            if (multiStream != null) {
                printProgress(multiStream, formatMessage(message.get(), Verbosity.PROGRESS));
            }
        }
    }

    public void println(PrintStream stream, String message) {
        synchronized (this) {
            char[] charArray = message.toCharArray();
            stream.println(fillBuffer(charArray));
        }
    }

    public void print(PrintStream stream, String message) {
        synchronized (this) {
            stream.print(fillBuffer(message.toCharArray()));
        }
    }

    public void printProgress(PrintStream stream, String message) {
        synchronized (this) {
            char[] charArray = message.toCharArray();
            stream.print(fillBuffer(charArray));
            progressSize = charArray.length;
        }
    }

    private char[] fillBuffer(char[] charArray) {
        if (progressSize == 0) {
            return charArray;
        } else {
            char[] buffer = new char[Math.max(progressSize, charArray.length) + 1];
            buffer[0] = '\r';
            System.arraycopy(charArray, 0, buffer, 1, charArray.length);
            Arrays.fill(buffer, charArray.length + 1, buffer.length, ' ');
            progressSize = 0;
            return buffer;
        }
    }

    public void println(Throwable error, Verbosity verbosity) {
        if (configuration == null) {
            println(originalSystemErr, Log.getErrorMessage(error, true));
        } else {
            OpenPrintStream multiStream = configuration.logStreams.get(verbosity);
            if (multiStream != null) {
                println(
                        multiStream,
                        formatMessage(Log.getErrorMessage(error, configuration.printStacktrace), verbosity));
            }
        }
    }

    private String formatMessage(String message, Verbosity verbosity) {
        if (configuration.formatters.isEmpty()) {
            return message != null ? message : "null";
        } else {
            final StringBuilder sb = new StringBuilder();
            final ListIterator<IFormatter> it = configuration.formatters.listIterator();
            while (it.hasNext()) {
                sb.append(it.next().getPrefix(message, verbosity));
            }
            sb.append(message != null ? message : "null");
            while (it.hasPrevious()) {
                sb.append(it.previous().getSuffix(message, verbosity));
            }
            return sb.toString();
        }
    }
}
