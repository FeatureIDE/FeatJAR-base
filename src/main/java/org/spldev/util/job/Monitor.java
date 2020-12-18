package org.spldev.util.job;

/**
 * Control object for {@link MonitorableSupplier} and
 * {@link MonitorableFunction}. Can be used to cancel a function's execution and
 * to get the progress of the given function.
 *
 * @author Sebastian Krieter
 */
public interface Monitor {

	int getTotalWork();

	int getRemainingWork();

	int getWorkDone();

	double getRelativeWorkDone();

	String getTaskName();

	void cancel();

	boolean isCanceled();

}
