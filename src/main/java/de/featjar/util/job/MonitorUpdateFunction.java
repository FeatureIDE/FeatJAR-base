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
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.job;

import de.featjar.util.logging.Logger;

/**
 * Thread to run an arbitrary function at a regular time interval.
 *
 * @author Sebastian Krieter
 */
public final class MonitorUpdateFunction implements UpdateFunction {

    private final Monitor monitor;

    public MonitorUpdateFunction(Monitor monitor) {
        this.monitor = monitor;
        Logger.startProgressEstimation();
    }

    @Override
    public boolean update() {
        if (monitor.isCanceled() || monitor.isDone()) {
            Logger.stopProgressEstimation();
            return false;
        } else {
            Logger.showProgress(monitor);
            return true;
        }
    }

    public Monitor getMonitor() {
        return monitor;
    }
}
