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
package org.spldev.util.io.format;

import org.spldev.util.data.Result;

/**
 * Provides a format for a given file content and file path.
 * 
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface FormatSupplier<T> {

	static <T> FormatSupplier<T> of(Format<T> format) {
		return inputHeader -> Result.of(format);
	}

	/**
	 * Returns the format that fits the given parameter.
	 *
	 * @param sourceHeader the beginning of the file's content
	 *
	 * @return A {@link Format format} that uses the file extension of the given
	 *         path. Result may be if there is no suitable format.
	 */
	Result<Format<T>> getFormat(SourceHeader sourceHeader);

}
