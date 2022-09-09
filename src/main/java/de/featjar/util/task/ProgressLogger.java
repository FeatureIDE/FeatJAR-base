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

import de.featjar.util.logging.Logger;

import java.util.function.Supplier;

/**
 * Thread to run an arbitrary function at a regular time interval.
 *
 * @author Sebastian Krieter
 */
public class ProgressLogger implements Supplier<Boolean> {
    protected final Monitor monitor;

    public ProgressLogger(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public Boolean get() {
        if (monitor.isCanceled() || monitor.isDone()) {
            return false;
        } else {
            Logger.logProgress(((Math.floor(monitor.getProgress() * 1000)) / 10.0) + "%");
            return true;
        }
    }
}