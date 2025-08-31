/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Logs messages to standard output and files. Formats log messages with
 * {@link IFormatter formatters}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class BufferedLog implements Log {

    public static class Message {
        private final Supplier<String> message;
        private final Verbosity verbosity;
        private final boolean format;

        public Message(Supplier<String> message, Verbosity verbosity, boolean format) {
            this.message = message;
            this.verbosity = verbosity;
            this.format = format;
        }

        public Supplier<String> getMessage() {
            return message;
        }

        public Verbosity getVerbosity() {
            return verbosity;
        }

        public boolean isFormat() {
            return format;
        }
    }

    private final LinkedList<Message> logBuffer = new LinkedList<>();

    @Override
    public void print(Supplier<String> message, Verbosity verbosity, boolean format) {
        synchronized (logBuffer) {
            logBuffer.add(new Message(message, verbosity, format));
        }
    }

    @Override
    public void println(Supplier<String> message, Verbosity verbosity, boolean format) {
        synchronized (logBuffer) {
            logBuffer.add(new Message(() -> message.get() + "\n", verbosity, format));
        }
    }

    @Override
    public void println(Throwable error, Verbosity verbosity) {
        synchronized (logBuffer) {
            logBuffer.add(
                    new Message(() -> Log.getErrorMessage(error, error.getMessage() == null) + "\n", verbosity, false));
        }
    }

    public void flush(Consumer<Message> messageConsumer) {
        synchronized (logBuffer) {
            for (Message message : logBuffer) {
                messageConsumer.accept(message);
            }
            logBuffer.clear();
        }
    }
}
