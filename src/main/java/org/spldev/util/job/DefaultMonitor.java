/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2020  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.job;

/**
 * Default implementation of {@link InternalMonitor} and {@link Monitor}.<br>
 * Provides support for reporting progress and canceling a function's execution.
 *
 * @author Sebastian Krieter
 */
public class DefaultMonitor implements InternalMonitor {

	protected final DefaultMonitor parent;

	protected String taskName;

	protected boolean canceled;
	protected int currentWork;
	protected int totalWork;

	public DefaultMonitor() {
		parent = null;
	}

	public DefaultMonitor(DefaultMonitor parent, boolean canceled) {
		this.parent = parent;
		this.canceled = canceled;
	}

	protected void uncertainWorked(int work) {
		currentWork += work;
		totalWork += work;
	}

	protected void worked(int work) {
		currentWork += work;
	}

	@Override
	public synchronized final void uncertainStep() throws MethodCancelException {
		uncertainWorked(1);
		checkCancel();
	}

	@Override
	public synchronized final void uncertainStep(int work) throws MethodCancelException {
		uncertainWorked(work);
		checkCancel();
	}

	@Override
	public synchronized final void step() throws MethodCancelException {
		worked(1);
		checkCancel();
	}

	@Override
	public synchronized final void step(int work) throws MethodCancelException {
		worked(work);
		checkCancel();
	}

	@Override
	public synchronized final void setTotalWork(int work) {
		totalWork = work;
		checkCancel();
	}

	@Override
	public synchronized void done() {
		currentWork = totalWork;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void checkCancel() throws MethodCancelException {
		if (canceled) {
			throw new MethodCancelException();
		}
	}

	@Override
	public int getTotalWork() {
		return totalWork;
	}

	@Override
	public int getRemainingWork() {
		return totalWork - currentWork;
	}

	@Override
	public synchronized int getWorkDone() {
		return currentWork;
	}

	@Override
	public synchronized double getRelativeWorkDone() {
		return currentWork / totalWork;
	}

	@Override
	public synchronized void setTaskName(String name) {
		taskName = name;
	}

	@Override
	public String getTaskName() {
		return String.valueOf(taskName);
	}

	@Override
	public DefaultMonitor subTask(int size) {
		worked(size);
		return new DefaultMonitor(this, canceled);
	}

}
