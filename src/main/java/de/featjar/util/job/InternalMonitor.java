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
package de.featjar.util.job;

/**
 * Control object for {@link MonitorableSupplier} and
 * {@link MonitorableFunction}. Can be used to check for cancel request and
 * allows to provide the progress of the given function.
 *
 * @author Sebastian Krieter
 */
public interface InternalMonitor extends Monitor {

    public static class MethodCancelException extends RuntimeException {

        public MethodCancelException() {
            super("Method was canceled");
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Set the amount of work to be done.
     *
     * @param work Absolute amount (must be positive).
     */
    void setTotalWork(int work);

    /**
     * Increases the monitor's progress, invokes the intermediate function (with
     * {@code null}), and checks for cancel.
     */
    void step() throws MethodCancelException;

    /**
     * Increases the monitor's progress, invokes the intermediate function (with
     * {@code null}), and checks for cancel.
     *
     * @param work the amount of work done
     */
    void step(int work) throws MethodCancelException;

    void uncertainStep() throws MethodCancelException;

    void uncertainStep(int work) throws MethodCancelException;

    InternalMonitor subTask(int size);

    void setTaskName(String name);

    /**
     * <b>Use {@link #step()} if possible.</b><br>
     * Throws a {@link MethodCancelException} if the monitor's {@link #cancel()}
     * method was called.
     *
     * @throws MethodCancelException is thrown if the function is canceled and calls
     *                               the {@link #checkCancel()} method.
     */
    void checkCancel() throws MethodCancelException;

    void done();
}
