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
package de.featjar.util.task;

import java.util.function.Supplier;

/**
 * Thread that runs a function at a regular time interval given in milliseconds.
 * The thread stops automatically when the monitored task is done.
 * The thread can also be stopped manually by calling {@link #interrupt()}, which will call the function one last time.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class IntervalThread extends Thread {
    protected final Supplier<Boolean> function;

    protected final long interval;

    public IntervalThread(Supplier<Boolean> function, long interval) {
        super();
        this.function = function;
        this.interval = interval;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        try {
            while (function.get()) {
                Thread.sleep(interval);
            }
        } catch (final InterruptedException ignored) {
        }
        function.get();
    }
}
