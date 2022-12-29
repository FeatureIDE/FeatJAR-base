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
package de.featjar.base.task;

import de.featjar.base.data.Result;

/**
 * Monitor that only allows to cancel a task and ignores progress information.
 * TODO: replace this with a NullMonitor, as cancellation is already handled by FutureResult
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 * @deprecated this should be merged with the new computation concept
 */
@Deprecated
public class CancelableMonitor implements IMonitor {
    protected final CancelableMonitor parentMonitor;
    protected String taskName;
    protected boolean canceled = false;
    protected boolean done = false;

    public CancelableMonitor() {
        parentMonitor = null;
    }

    protected CancelableMonitor(CancelableMonitor parentMonitor) {
        this.parentMonitor = parentMonitor;
        canceled = parentMonitor.canceled;
        done = parentMonitor.done;
    }

    @Override
    public Result<String> getTaskName() {
        return Result.of(taskName);
    }

    @Override
    public void setTaskName(String name) {
        taskName = name;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void requestCancel() {
        canceled = true;
    }

    @Override
    public void checkCancel() throws TaskCanceledException {
        if (canceled) {
            throw new TaskCanceledException();
        }
        if (parentMonitor != null) {
            parentMonitor.checkCancel();
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void setDone() {
        done = true;
    }

    @Override
    public CancelableMonitor newChildMonitor(int stepsInParent) {
        return new CancelableMonitor(this);
    }
}
