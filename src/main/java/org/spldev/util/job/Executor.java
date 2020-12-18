package org.spldev.util.job;

import org.spldev.util.*;
import org.spldev.util.job.InternalMonitor.*;

/**
 * Class that can execute instances of {@link MonitorableSupplier} and
 * {@link MonitorableFunction}.<br>
 * Provides a default {@link Monitor} implementation, catches any exceptions
 * thrown by the given function, and wraps the given function's return value in
 * a {@link Result}.
 *
 * @author Sebastian Krieter
 */
public final class Executor {

	private Executor() {
	}

	public static <T> Result<T> run(MonitorableSupplier<T> supplier) {
		return run(supplier, new NullMonitor());
	}

	public static <T, R> Result<R> run(MonitorableFunction<T, R> function, T input) {
		return run(function, input, new NullMonitor());
	}

	public static <T> Result<T> run(MonitorableSupplier<T> supplier, InternalMonitor monitor) {
		monitor = monitor != null ? monitor : new NullMonitor();
		try {
			return Result.of(supplier.execute(monitor));
		} catch (final Exception e) {
			return Result.empty(e);
		} finally {
			monitor.done();
		}
	}

	public static <T, R> Result<R> run(MonitorableFunction<T, R> function, T input, InternalMonitor monitor)
		throws MethodCancelException {
		monitor = monitor != null ? monitor : new NullMonitor();
		try {
			return Result.of(function.execute(input, monitor));
		} catch (final Exception e) {
			return Result.empty(e);
		} finally {
			monitor.done();
		}
	}

}
