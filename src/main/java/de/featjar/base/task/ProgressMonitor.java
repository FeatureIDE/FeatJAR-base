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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Monitor that allows to cancel a task and reports progress information.
 * Also allows the creation of child monitors that report progress of subordinate tasks.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ProgressMonitor extends CancelableMonitor {

    protected final List<ProgressMonitor> children = new CopyOnWriteArrayList<>();
    protected final int stepsInParent;
    protected int totalSteps;
    protected int currentStep;

    public ProgressMonitor() {
        stepsInParent = 0;
    }

    private ProgressMonitor(ProgressMonitor parentMonitor, int stepsInParent) {
        super(parentMonitor);
        this.stepsInParent = stepsInParent;
    }

    @Override
    public int getTotalSteps() {
        return totalSteps;
    }

    @Override
    public final void setTotalSteps(int steps) {
        totalSteps = steps;
        checkCancel();
    }

    @Override
    public int getRemainingSteps() {
        return totalSteps - getCurrentStep();
    }

    @Override
    public int getCurrentStep() {
        int steps = currentStep;
        for (final ProgressMonitor child : children) {
            steps += child.getProgress() * child.stepsInParent;
        }
        return steps;
    }

    @Override
    public final void addSteps(int steps) throws TaskCanceledException {
        currentStep += steps;
        checkCancel();
    }

    @Override
    public final void addUncertainSteps(int steps) throws TaskCanceledException {
        currentStep += steps;
        totalSteps += steps;
        checkCancel();
    }

    @Override
    public double getProgress() {
        if (totalSteps == 0) {
            return 0;
        }
        double steps = currentStep;
        for (final ProgressMonitor child : children) {
            steps += child.getProgress() * child.stepsInParent;
        }
        return steps / totalSteps;
    }

    @Override
    public void setDone() {
        super.setDone();
        currentStep = totalSteps;
    }

    @Override
    public ProgressMonitor createChildMonitor(int stepsInParent) {
        final ProgressMonitor child = new ProgressMonitor(this, stepsInParent);
        children.add(child);
        return child;
    }
}
