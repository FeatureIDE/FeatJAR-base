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
package org.spldev.util.io;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.function.*;

import org.spldev.util.*;
import org.spldev.util.data.*;
import org.spldev.util.io.format.*;

/**
 * Enables reading and writing of a file in a certain {@link Format}.
 *
 * @author Sebastian Krieter
 */
public class FileHandler<T> {

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * Retrieves the file name of a {@link Path} without its extension.
	 *
	 * @param path the given path
	 * @return the file name
	 */
	public static String getFileNameWithoutExtension(Path path) {
		return getFileNameWithoutExtension(path.getFileName().toString());
	}

	/**
	 * Retrieves the file name of a {@link Path} without its extension.
	 *
	 * @param fileName the file name with a (possible) extension
	 * @return the file name
	 */
	public static String getFileNameWithoutExtension(String fileName) {
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
	public static String getFileExtension(Path path) {
		if (path == null) {
			return "";
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
	public static String getFileExtension(String fileName) {
		if (fileName == null) {
			return "";
		}
		final int extensionIndex = fileName.lastIndexOf('.');
		return (extensionIndex > 0) ? fileName.substring(extensionIndex + 1) : "";
	}

	public static String read(Path path) throws IOException {
		return read(path, DEFAULT_CHARSET);
	}

	public static String read(Path path, Charset charset) throws IOException {
		if (!Files.exists(path)) {
			throw new FileNotFoundException(path.toString());
		}
		return new String(Files.readAllBytes(path), charset);
	}

	public static <T> Result<T> parse(
		Path path,
		Format<T> format //
	) {
		return parse(path, format, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(
		Path path,
		Format<T> format,
		Supplier<T> objectSupplier //
	) {
		return parse(path, format, objectSupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(
		Path path,
		FormatSupplier<T> formatSupplier //
	) {
		return parse(path, formatSupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(
		Path path,
		FormatSupplier<T> formatSupplier,
		Supplier<T> objectSupplier //
	) {
		return parse(path, formatSupplier, objectSupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(
		Path path,
		FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier //
	) {
		return parse(path, formatSupplier, factorySupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(
		Path path,
		Format<T> format,
		Charset charset //
	) {
		try {
			return parseFromSource(read(path, charset), format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> parse(
		Path path,
		Format<T> format,
		Supplier<T> objectSupplier,
		Charset charset //
	) {
		try {
			return parseFromSource(read(path, charset), format, objectSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> parse(
		Path path,
		FormatSupplier<T> formatSupplier,
		Supplier<T> objectSupplier,
		Charset charset //
	) {
		try {
			return parseFromSource(read(path, charset), path, formatSupplier, objectSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> parse(
		Path path,
		FormatSupplier<T> formatSupplier,
		Charset charset //
	) {
		try {
			return parseFromSource(read(path, charset), path, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> parse(
		Path path,
		FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier,
		Charset charset //
	) {
		try {
			return parseFromSource(read(path, charset), path, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> parseFromSource(
		CharSequence content,
		Format<T> format //
	) {
		return format.getInstance().parse(content);
	}

	public static <T> Result<T> parseFromSource(
		CharSequence content,
		Format<T> format,
		Supplier<T> objectSupplier //
	) {
		return format.getInstance().parse(content, objectSupplier);
	}

	public static <T> Result<T> parseFromSource(
		CharSequence content,
		Path path,
		FormatSupplier<T> formatSupplier //
	) {
		final Result<Format<T>> format = formatSupplier.getFormat(path, content);
		return format.flatMap(
			fo -> fo.getInstance().parse(content));
	}

	public static <T> Result<T> parseFromSource(
		CharSequence content,
		Path path,
		FormatSupplier<T> formatSupplier,
		Supplier<T> objectSupplier //
	) {
		final Result<Format<T>> format = formatSupplier.getFormat(path, content);
		return format.flatMap(
			fo -> fo.getInstance().parse(content, objectSupplier));
	}

	public static <T> Result<T> parseFromSource(
		CharSequence content,
		Path path,
		FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier //
	) {
		final Result<Format<T>> format = formatSupplier.getFormat(path, content);
		final Result<Factory<T>> factory = format.flatMap(f -> factorySupplier.getFactory(path, f));
		return format.flatMap(
			fo -> factory.flatMap(
				fa -> fo.getInstance().parse(content, fa)));
	}

	public static <T> void serialize(T object, Path path, Format<T> format, Charset charset) throws IOException {
		write(format.getInstance().serialize(object), path, charset);
	}

	public static <T> void serialize(T object, Path path, Format<T> format) throws IOException {
		write(format.getInstance().serialize(object), path);
	}
	
	public static <T> void write(String source, Path path, Charset charset) throws IOException {
		Files.write(path, //
			source.getBytes(charset), //
			StandardOpenOption.TRUNCATE_EXISTING, //
			StandardOpenOption.CREATE, //
			StandardOpenOption.WRITE);
	}

	public static <T> void write(String source, Path path) throws IOException {
		write(source, path, DEFAULT_CHARSET);
	}

}
