/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
public class UpdateThread extends Thread {

    private static final int DEFAULT_UPDATE_TIME = 1_000;

    private final UpdateFunction function;

    protected boolean monitorRun = true;
    private long updateTime;

    public UpdateThread(UpdateFunction function) {
        this(function, DEFAULT_UPDATE_TIME);
    }

    /**
     * @param function   is called at every update
     * @param updateTime in ms
     */
    public UpdateThread(UpdateFunction function, long updateTime) {
        super();
        this.function = function;
        this.updateTime = updateTime;
    }

    @Override
    public void run() {
        monitorRun = function.update();
        try {
            while (monitorRun) {
                Thread.sleep(updateTime);
                monitorRun = function.update();
            }
        } catch (final InterruptedException e) {
        } finally {
            function.update();
        }
    }

    public void finish() {
        // to ensure to stop the monitor thread
        monitorRun = false;
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
            Logger.logError(e);
        }
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
