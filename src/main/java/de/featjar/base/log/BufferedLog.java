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

import de.featjar.base.data.Pair;
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

    private final LinkedList<Pair<Verbosity, Supplier<String>>> logBuffer = new LinkedList<>();

    @Override
    public void print(Supplier<String> message, Verbosity verbosity) {
        synchronized (logBuffer) {
            logBuffer.add(new Pair<>(verbosity, message));
        }
    }

    @Override
    public void println(Supplier<String> message, Verbosity verbosity) {
        synchronized (logBuffer) {
            logBuffer.add(new Pair<>(verbosity, () -> message.get() + "\n"));
        }
    }

    @Override
    public void println(Throwable error, Verbosity verbosity) {
        synchronized (logBuffer) {
            logBuffer.add(new Pair<>(verbosity, () -> Log.getErrorMessage(error, false) + "\n"));
        }
    }

    public void flush(Consumer<Pair<Verbosity, Supplier<String>>> messageConsumer) {
        synchronized (logBuffer) {
            for (Pair<Verbosity, Supplier<String>> message : logBuffer) {
                messageConsumer.accept(message);
            }
            logBuffer.clear();
        }
    }
}
