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
package org.spldev.util.logging;

/**
 * Thread to run an arbitrary function at a regular time interval.
 *
 * @author Sebastian Krieter
 */
public class MonitorThread extends Thread {

	private final Runnable function;

	private boolean monitorRun = true;
	private long updateTime;

	public MonitorThread(Runnable function) {
		this(function, 1_000);
	}

	/**
	 * @param function   is called at every update
	 * @param updateTime in ms
	 */
	public MonitorThread(Runnable function, long updateTime) {
		super();
		this.function = function;
		this.updateTime = updateTime;
	}

	@Override
	public void run() {
		function.run();
		try {
			while (monitorRun) {
				Thread.sleep(updateTime);
				function.run();
			}
		} catch (final InterruptedException e) {
		}
		function.run();
	}

	public void finish() {
		// to ensure to stop the monitor thread
		monitorRun = false;
		interrupt();
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

}
