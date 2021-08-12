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
package org.spldev.util.data;

import java.nio.file.*;

import org.spldev.util.*;
import org.spldev.util.io.format.*;

/**
 * Provides a factory for a given format and file path.
 * 
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface FactorySupplier<T> {

	static <T> FactorySupplier<T> of(Factory<T> factory) {
		return (path, format) -> Result.of(factory);
	}

	/**
	 * Returns the factory that fits the given parameter.
	 *
	 * @param path   the file path
	 * @param format the file format
	 *
	 * @return A {@link Factory factory} that uses the given extension. Result may
	 *         be empty if there is no suitable factory.
	 */
	Result<Factory<T>> getFactory(Path path, Format<T> format);

}
