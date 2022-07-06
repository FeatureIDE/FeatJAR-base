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
package org.spldev.util.io;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.function.*;

import org.spldev.util.data.*;
import org.spldev.util.io.format.*;

/**
 * Enables reading and writing of a file in a certain {@link Format}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class FileHandler {
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final String EMPTY_FILE_EXTENSION = "";

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
	public static String getFileExtension(String fileName) {
		if (fileName == null) {
			return EMPTY_FILE_EXTENSION;
		}
		final int extensionIndex = fileName.lastIndexOf('.');
		return (extensionIndex > 0) ? fileName.substring(extensionIndex + 1) : "";
	}

	public static <T> Result<T> load(//
		InputStream inputStream, //
		Format<T> format //
	) {
		return load(inputStream, format, DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(//
		InputStream inputStream, //
		Format<T> format, //
		Charset charset //
	) {
		try (SourceMapper sourceMapper = SourceMapper.of(inputStream, OutputStream.nullOutputStream(), charset,
			EMPTY_FILE_EXTENSION)) {
			return parse(sourceMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(Path path, Format<T> format, //
		SourceMapper.Options... sourceMapperOptions) {
		return load(path, format, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> Result<T> load(Path path, Format<T> format, Supplier<T> objectSupplier, //
		SourceMapper.Options... sourceMapperOptions) {
		return load(path, format, objectSupplier, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, //
		SourceMapper.Options... sourceMapperOptions) {
		return load(path, formatSupplier, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, Supplier<T> objectSupplier, //
		SourceMapper.Options... sourceMapperOptions) {
		return load(path, formatSupplier, objectSupplier, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, FactorySupplier<T> factorySupplier, //
		SourceMapper.Options... sourceMapperOptions) {
		return load(path, formatSupplier, factorySupplier, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Charset charset, //
		SourceMapper.Options... sourceMapperOptions) {
		try (SourceMapper sourceMapper = SourceMapper.of(path, charset, sourceMapperOptions)) {
			return parse(sourceMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Supplier<T> factory, //
		Charset charset, //
		SourceMapper.Options... sourceMapperOptions) {
		try (SourceMapper sourceMapper = SourceMapper.of(path, charset, sourceMapperOptions)) {
			return parse(sourceMapper, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory, //
		Charset charset, //
		SourceMapper.Options... sourceMapperOptions) {
		try (SourceMapper sourceMapper = SourceMapper.of(path, charset, sourceMapperOptions)) {
			return parse(sourceMapper, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Charset charset, //
		SourceMapper.Options... sourceMapperOptions) {
		try (SourceMapper sourceMapper = SourceMapper.of(path, charset, sourceMapperOptions)) {
			return parse(sourceMapper, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier, //
		Charset charset, //
		SourceMapper.Options... sourceMapperOptions) {
		try (SourceMapper sourceMapper = SourceMapper.of(path, charset, sourceMapperOptions)) {
			return parse(path, sourceMapper, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Format<T> format //
	) {
		try (SourceMapper sourceMapper = SourceMapper.of(content, DEFAULT_CHARSET, EMPTY_FILE_EXTENSION)) {
			return parse(sourceMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Format<T> format, Factory<T> factory //
	) {
		try (SourceMapper sourceMapper = SourceMapper.of(content, DEFAULT_CHARSET, EMPTY_FILE_EXTENSION)) {
			return parse(sourceMapper, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier,
		Factory<T> factory //
	) {
		try (SourceMapper sourceMapper = SourceMapper.of(content, DEFAULT_CHARSET, getFileExtension(path))) {
			return parse(sourceMapper, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier //
	) {
		try (SourceMapper sourceMapper = SourceMapper.of(content, DEFAULT_CHARSET, getFileExtension(path))) {
			return parse(sourceMapper, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier //
	) {
		try (SourceMapper sourceMapper = SourceMapper.of(content, DEFAULT_CHARSET, getFileExtension(path))) {
			return parse(path, sourceMapper, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	private static <T> Result<T> parse(SourceMapper sourceMapper, Format<T> format, Supplier<T> factory) {
		return format.supportsParse() ? format.getInstance().parse(sourceMapper, factory)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(SourceMapper sourceMapper, Format<T> format) {
		return format.supportsParse() ? format.getInstance().parse(sourceMapper)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(//
		SourceMapper sourceMapper, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory //
	) {
		return sourceMapper.getMainSource().getSourceHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(sourceMapper, format, factory));
	}

	private static <T> Result<T> parse(//
		SourceMapper sourceMapper, //
		FormatSupplier<T> formatSupplier //
	) {
		return sourceMapper.getMainSource().getSourceHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(sourceMapper, format));
	}

	private static <T> Result<T> parse(//
		Path path, //
		SourceMapper sourceMapper, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier //
	) {
		return sourceMapper.getMainSource().getSourceHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> factorySupplier.getFactory(path, format) //
				.flatMap(factory -> parse(sourceMapper, format, factory)));
	}

	public static <T> void save(T object, Path path, Format<T> format, SourceMapper.Options... sourceMapperOptions)
		throws IOException {
		save(object, path, format, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> void save(T object, Path path, Format<T> format, Charset charset,
		SourceMapper.Options... sourceMapperOptions) throws IOException {
		if (format.supportsSerialize()) {
			try (SourceMapper sourceMapper = SourceMapper.of(path, charset, sourceMapperOptions)) {
				format.getInstance().write(object, sourceMapper);
			}
		}
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format,
		SourceMapper.Options... sourceMapperOptions)
		throws IOException {
		save(object, outStream, format, DEFAULT_CHARSET, sourceMapperOptions);
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format, Charset charset,
		SourceMapper.Options... sourceMapperOptions)
		throws IOException {
		if (format.supportsSerialize()) {
			try (SourceMapper sourceMapper = SourceMapper.of(InputStream.nullInputStream(), outStream, charset,
				EMPTY_FILE_EXTENSION, sourceMapperOptions)) {
				format.getInstance().write(object, sourceMapper);
			}
		}
	}

	public static <T> String print(T object, Format<T> format, SourceMapper.Options... sourceMapperOptions)
		throws IOException {
		if (format.supportsSerialize()) {
			try (SourceMapper sourceMapper = SourceMapper.ofString("", DEFAULT_CHARSET, EMPTY_FILE_EXTENSION,
				sourceMapperOptions)) {
				format.getInstance().write(object, sourceMapper);
				return sourceMapper.getMainSource().getOutputStream().toString();
			}
		}
		return "";
	}

	public static <T> void write(String content, Path path, Charset charset) throws IOException {
		Files.write(path, //
			content.getBytes(charset), //
			StandardOpenOption.TRUNCATE_EXISTING, //
			StandardOpenOption.CREATE, //
			StandardOpenOption.WRITE);
	}

	public static <T> void write(String content, Path path) throws IOException {
		write(content, path, DEFAULT_CHARSET);
	}

}
