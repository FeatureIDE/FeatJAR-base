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

import java.util.Optional;

/**
 * Monitors the execution of a long-running task.
 * When executed by the {@link Executor}, a task is a {@link MonitorableFunction} or a {@link MonitorableSupplier}.
 * A monitor can be used to cancel a task's execution and get information on its progress.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Monitor {
    /**
     * Thrown to signal that a task has been canceled.
     */
    class TaskCanceledException extends RuntimeException {
        public TaskCanceledException() {
            super("Task was canceled");
        }
    }

    /**
     * {@return the monitored task's name, if any}
     */
    default Optional<String> getTaskName() {
        return Optional.empty();
    }

    /**
     * Sets the monitored task's name.
     *
     * @param taskName the task's name
     */
    default void setTaskName(String taskName) {}

    /**
     * {@return the monitored task's total number of steps}
     */
    default int getTotalSteps() {
        return 0;
    }

    /**
     * Sets the monitored task's total number of steps.
     *
     * @param steps positive number of steps
     */
    default void setTotalSteps(int steps) {}

    /**
     * {@return the monitored task's remaining number of steps}
     */
    default int getRemainingSteps() {
        return 0;
    }

    /**
     * {@return the monitored task's current step}
     */
    default int getCurrentStep() {
        return 0;
    }

    /**
     * Increases the monitored task's current step by one and checks for cancellation.
     *
     * @throws TaskCanceledException when the monitored task has been canceled
     */
    default void addStep() throws TaskCanceledException {
        addSteps(1);
    }

    /**
     * Increases the monitored task's current step by an amount and checks for cancellation.
     *
     * @param steps positive number of steps
     * @throws TaskCanceledException when the monitored task has been canceled
     */
    default void addSteps(int steps) throws TaskCanceledException {}

    /**
     * Increases the monitored task's current step and total number of steps by one and checks for cancellation.
     * Can be used to monitor the progress of tasks with an uncertain amount of total steps.
     *
     * @throws TaskCanceledException when the monitored task has been canceled
     */
    default void addUncertainStep() throws TaskCanceledException {
        addUncertainSteps(1);
    }

    /**
     * Increases the monitored task's current step and total number of steps by an amount and checks for cancellation.
     * Can be used to monitor the progress of tasks with an uncertain amount of total steps.
     *
     * @param steps positive number of steps
     * @throws TaskCanceledException when the monitored task has been canceled
     */
    default void addUncertainSteps(int steps) throws TaskCanceledException {}

    /**
     * {@return the monitored task's progress (i.e., the current step divided by the total number of steps)}
     */
    default double getProgress() {
        return 0;
    }

    /**
     * {@return whether the monitored task has been canceled}
     */
    boolean isCanceled();

    /**
     * Signals that the execution of the monitored task should be canceled.
     * Requires the task to regularly call {@link #checkCancel()}.
     */
    void requestCancel();

    /**
     * Checks whether the monitored task has been canceled and throws if so.
     * When progress information is available, prefer using {@link #addStep()}.
     *
     * @throws TaskCanceledException if the task has been canceled with {@link #requestCancel()}
     */
    void checkCancel() throws TaskCanceledException;

    /**
     * {@return whether the monitored task is done}
     * For many tasks, this is the case when {@link #getRemainingSteps()} is 0.
     * However, for tasks with an uncertain amount of total steps, this is not generally the case.
     */
    boolean isDone();

    /**
     * Signals that the execution of the monitored task is done.
     * Always called by the {@link Executor}, but can already be called before by the task.
     */
    void setDone();

    /**
     * Creates a new child monitor to be used for monitoring a subordinate task.
     *
     * @return the child monitor
     */
    default Monitor createChildMonitor() {
        return createChildMonitor(1);
    }

    /**
     * Creates a new child monitor to be used for monitoring a subordinate task.
     *
     * @param stepsInParent the number of steps the child monitor should occupy in the parent monitor
     * @return the child monitor
     */
    Monitor createChildMonitor(int stepsInParent);
}
