/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
 * 
 * This file is part of util.
 * 
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 * 
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/FeatJAR/util> for further information.
 * -----------------------------------------------------------------------------
 */
package de.featjar.util.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Input or output (e.g., physical file, string, or stream) mapped by a
 * {@link IOMapper}.
 *
 * @author Elias Kuiter
 */
public interface IOObject extends AutoCloseable {
	java.lang.String EMPTY_FILE_EXTENSION = "";
	Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	@Override
	void close() throws IOException;

	/**
	 * Retrieves the file name of a {@link Path} without its extension.
	 *
	 * @param path the given path
	 * @return the file name
	 */
	static String getFileNameWithoutExtension(Path path) {
		return getFileNameWithoutExtension(path.getFileName().toString());
	}

	/**
	 * Retrieves the file name of a {@link Path} without its extension.
	 *
	 * @param fileName the file name with a (possible) extension
	 * @return the file name
	 */
	static String getFileNameWithoutExtension(String fileName) {
		final int extensionIndex = fileName.lastIndexOf('.');
		return (extensionIndex > 0) ? fileName.substring(0, extensionIndex) : fileName;
	}

	/**
	 * Retrieves the file extension of a {@link Path}.<br>
	 * <b>Note:</b> A dot at the first position of the file name is ignored. E.g.,
	 * ".file" has no extension, but ".file.txt" would return "txt".
	 *
	 * @param path the given path
	 * @return the file extension
	 *
	 * @see #getFileExtension(String)
	 */
	static String getFileExtension(Path path) {
		if (path == null) {
			return EMPTY_FILE_EXTENSION;
		}
		return getFileExtension(path.getFileName().toString());
	}

	/**
	 * Retrieves the file extension from a file name.<br>
	 * <b>Note:</b> A dot at the first position of the file name is ignored. E.g.,
	 * ".file" has no extension, but ".file.txt" would return "txt".
	 *
	 * @param fileName the given file name
	 * @return the file extension
	 *
	 * @see #getFileExtension(Path)
	 */
	static String getFileExtension(String fileName) {
		if (fileName == null) {
			return EMPTY_FILE_EXTENSION;
		}
		final int extensionIndex = fileName.lastIndexOf('.');
		return (extensionIndex > 0) ? fileName.substring(extensionIndex + 1) : "";
	}

	static String getFileNameWithExtension(String fileName, String fileExtension) {
		if (fileExtension == null || fileExtension.equals(EMPTY_FILE_EXTENSION))
			return IOObject.getFileNameWithoutExtension(fileName);
		return String.format("%s.%s", IOObject.getFileNameWithoutExtension(fileName), fileExtension);
	}

	static Path getPathWithExtension(Path path, String fileExtension) {
		return path.resolveSibling(getFileNameWithExtension(path.getFileName().toString(), fileExtension));
	}

	static Path getPathWithExtension(String fileName, String fileExtension) {
		return Paths.get(getFileNameWithExtension(fileName, fileExtension));
	}
}
