/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util Lib.
 * 
 * Util Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util;

import java.nio.file.*;
import java.util.function.*;

import org.spldev.util.data.*;
import org.spldev.util.io.*;
import org.spldev.util.io.format.*;
import org.spldev.util.job.*;

/**
 * Abstract creator to derive an element from a {@link CacheHolder feature
 * model}.
 *
 * @param <T> The type of the element.
 *
 * @author Sebastian Krieter
 */
public interface Provider<T> extends BiFunction<CacheHolder, InternalMonitor, Result<T>> {

	Object defaultParameters = new Object();

	Identifier<T> getIdentifier();

	default Object getParameters() {
		return defaultParameters;
	}

	static <T, R> Result<R> convert(CacheHolder cache, Identifier<T> identifier, MonitorableFunction<T, R> function,
		InternalMonitor monitor) {
		return cache.get(identifier).flatMap(o -> Executor.run(function, o, monitor));
	}

	static <T, R> Result<R> convert(CacheHolder cache, Provider<T> provider, MonitorableFunction<T, R> function,
		InternalMonitor monitor) {
		return cache.get(provider).flatMap(o -> Executor.run(function, o, monitor));
	}

	static <R> Result<R> load(Path path, FormatSupplier<R> formatSupplier, FactorySupplier<R> factorySupplier) {
		return FileHandler.load(path, formatSupplier, factorySupplier);
	}

	static <R> Result<R> load(Path path, FormatSupplier<R> formatSupplier) {
		return FileHandler.load(path, formatSupplier);
	}

	static <R> Result<R> load(Path path, Format<R> format) {
		return FileHandler.load(path, format);
	}

}
