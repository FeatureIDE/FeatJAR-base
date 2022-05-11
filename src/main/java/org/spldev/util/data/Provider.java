/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021-2022  Sebastian Krieter
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
package org.spldev.util.data;

import java.nio.file.*;
import java.util.function.*;

import org.spldev.util.io.*;
import org.spldev.util.io.format.*;
import org.spldev.util.job.*;

/**
 * A function that derives objects from feature models and formulas, taking into
 * account a {@link Cache} and {@link InternalMonitor}. When the given
 * {@link Cache} already holds the requested object, returns the cached object
 * instead.
 *
 * @param <T> The type of the element.
 *
 * @author Sebastian Krieter
 */
public interface Provider<T> extends BiFunction<Cache, InternalMonitor, Result<T>> {

	Object defaultParameters = new Object();

	Identifier<T> getIdentifier();

	default Object getParameters() {
		return defaultParameters;
	}
	
	default boolean storeInCache() {
		return true;
	}

	static <T, R> Result<R> convert(Cache cache, Identifier<T> identifier, MonitorableFunction<T, R> function,
		InternalMonitor monitor) {
		return cache.get(identifier).flatMap(o -> Executor.run(function, o, monitor));
	}

	static <T, R> Result<R> convert(Cache cache, Provider<T> provider, MonitorableFunction<T, R> function,
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
