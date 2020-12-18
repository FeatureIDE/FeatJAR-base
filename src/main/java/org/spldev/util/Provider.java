package org.spldev.util;

import java.nio.file.*;
import java.util.function.*;

import org.spldev.util.io.*;
import org.spldev.util.io.format.*;
import org.spldev.util.job.*;

/**
 * Abstract creator to derive an element from a {@link Cache feature model}.
 *
 * @param <T> The type of the element.
 *
 * @author Sebastian Krieter
 */
public interface Provider<T> extends Function<Cache, Result<T>> {

	Object defaultParameters = new Object();

	Identifier<T> getIdentifier();

	default Object getParameters() {
		return defaultParameters;
	}

	static <T, R> Result<R> convert(Cache cache, Provider<T> provider, MonitorableFunction<T, R> function) {
		return cache.get(provider).flatMap(o -> Executor.run(function, o));
	}

	static <R> Result<R> load(Path path, FormatSupplier<R> formatSupplier) {
		return FileHandler.parse(path, formatSupplier);
	}

}
