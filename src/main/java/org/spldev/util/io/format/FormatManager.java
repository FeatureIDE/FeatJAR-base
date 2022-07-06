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

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.spldev.util.data.Result;
import org.spldev.util.extension.*;
import org.spldev.util.io.*;

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
	public Result<Format<T>> getFormat(SourceHeader sourceHeader) {
		final List<Format<T>> extensions = getExtensions();
		return extensions.stream()
			.filter(format -> Objects.equals(sourceHeader.getFileExtension(), format.getFileExtension()))
			.filter(format -> format.supportsContent(sourceHeader))
			.findFirst()
			.map(Result::of)
			.orElseGet(() -> Result.empty(new NoSuchExtensionException("No suitable format found for file extension \"."
				+ sourceHeader.getFileExtension() + "\". Possible Formats: " + getExtensions())));
	}

	private List<Format<T>> getFormatList(final String fileExtension) {
		return getExtensions().stream()
			.filter(Format::supportsParse)
			.filter(format -> fileExtension.equals(format.getFileExtension()))
			.collect(Collectors.toList());
	}

}
