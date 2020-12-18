package org.spldev.util.job;

/**
 * Interface for methods that take a long time to finish.<br>
 * Can be executed with the {@link Executor}.
 *
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface MonitorableSupplier<T> {

	T execute(InternalMonitor monitor) throws Exception;

}
