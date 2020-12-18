package org.spldev.util.job;

/**
 * Rudimentary implementation of {@link InternalMonitor} and
 * {@link Monitor}.<br>
 * Provides only support for canceling a function's execution.
 *
 * @author Sebastian Krieter
 */
public final class NullMonitor implements InternalMonitor {

	private boolean canceled = false;

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void done() {
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
