package org.spldev.util.job;

/**
 * Interface for methods that take a long time to finish.<br>
 * Can be executed with the {@link Executor}.
 *
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface MonitorableFunction<T, R> {

	R execute(T input, InternalMonitor monitor) throws Exception;

}
