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
