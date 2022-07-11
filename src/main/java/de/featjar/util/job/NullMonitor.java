/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
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
 * -----------------------------------------------------------------------------
 */
package de.featjar.util.job;

/**
 * Rudimentary implementation of {@link InternalMonitor} and
 * {@link Monitor}.<br>
 * Provides only support for canceling a function's execution.
 *
 * @author Sebastian Krieter
 */
public final class NullMonitor implements InternalMonitor {

	private boolean canceled = false;
	private boolean done = false;

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void done() {
		done = true;
	}

	@Override
	public void checkCancel() throws MethodCancelException {
		if (canceled) {
			throw new MethodCancelException();
		}
	}

	@Override
	public NullMonitor subTask(int size) {
		return new NullMonitor();
	}

	@Override
	public String getTaskName() {
		return "";
	}

	@Override
	public int getRemainingWork() {
		return 0;
	}

	@Override
	public int getTotalWork() {
		return 0;
	}

	@Override
	public int getWorkDone() {
		return 0;
	}

	@Override
	public double getRelativeWorkDone() {
		return 0;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void setTotalWork(int work) {
	}

	@Override
	public void step() throws MethodCancelException {
	}

	@Override
	public void step(int work) throws MethodCancelException {
	}

	@Override
	public void uncertainStep() throws MethodCancelException {

	}

	@Override
	public void uncertainStep(int work) throws MethodCancelException {

	}

	@Override
	public void setTaskName(String name) {
	}

}
