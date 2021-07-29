/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.io.format;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.spldev.util.Result;
import org.spldev.util.extension.ExtensionPoint;
import org.spldev.util.io.FileHandler;

/**
 * Manages additional formats for a certain object.
 *
 * @author Sebastian Krieter
 */
public class FormatManager<T> extends ExtensionPoint<Format<T>> implements FormatSupplier<T> {

	public Result<Format<T>> getFormatById(String id) throws NoSuchExtensionException {
		return getExtension(id);
	}

	public List<Format<T>> getFormatListForExtension(Path path) {
		if (path == null) {
			return Collections.emptyList();
		}
		return getFormatList(FileHandler.getFileExtension(path));
	}

	@Override
	public Result<Format<T>> getFormat(InputHeader inputHeader) {
		final String fileExtension = FileHandler.getFileExtension(inputHeader.getPath());
		return getExtensions().stream()
			.filter(format -> fileExtension.equals(format.getFileExtension()))
			.filter(format -> format.supportsContent(inputHeader))
			.findFirst()
			.map(Result::of)
			.orElse(Result.empty(new NoSuchExtensionException("No suitable format found for file extension ."
				+ fileExtension)));
	}

	private List<Format<T>> getFormatList(final String fileExtension) {
		return getExtensions().stream()
			.filter(format -> format.supportsParse())
			.filter(format -> fileExtension.equals(format.getFileExtension()))
			.collect(Collectors.toList());
	}

}
