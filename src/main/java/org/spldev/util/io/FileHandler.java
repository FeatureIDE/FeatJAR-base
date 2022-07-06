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
import java.util.Collections;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;

import org.spldev.util.data.*;
import org.spldev.util.io.file.FileMapper;
import org.spldev.util.io.file.InputFileMapper;
import org.spldev.util.io.file.OutputFileMapper;
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
		try (InputFileMapper inputFileMapper = InputFileMapper.ofInputStream(inputStream, charset,
			EMPTY_FILE_EXTENSION)) {
			return parse(inputFileMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(Path path, Format<T> format, //
		FileMapper.Options... fileMapperOptions) {
		return load(path, format, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> Result<T> load(Path path, Format<T> format, Supplier<T> objectSupplier, //
		FileMapper.Options... fileMapperOptions) {
		return load(path, format, objectSupplier, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, //
		FileMapper.Options... fileMapperOptions) {
		return load(path, formatSupplier, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, Supplier<T> objectSupplier, //
		FileMapper.Options... fileMapperOptions) {
		return load(path, formatSupplier, objectSupplier, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, FactorySupplier<T> factorySupplier, //
		FileMapper.Options... fileMapperOptions) {
		return load(path, formatSupplier, factorySupplier, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Charset charset, //
		FileMapper.Options... fileMapperOptions) {
		try (InputFileMapper inputFileMapper = InputFileMapper.of(path, charset, fileMapperOptions)) {
			return parse(inputFileMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Supplier<T> factory, //
		Charset charset, //
		FileMapper.Options... fileMapperOptions) {
		try (InputFileMapper inputFileMapper = InputFileMapper.of(path, charset, fileMapperOptions)) {
			return parse(inputFileMapper, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory, //
		Charset charset, //
		FileMapper.Options... fileMapperOptions) {
		try (InputFileMapper inputFileMapper = InputFileMapper.of(path, charset, fileMapperOptions)) {
			return parse(inputFileMapper, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Charset charset, //
		FileMapper.Options... fileMapperOptions) {
		try (InputFileMapper inputFileMapper = InputFileMapper.of(path, charset, fileMapperOptions)) {
			return parse(inputFileMapper, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier, //
		Charset charset, //
		FileMapper.Options... fileMapperOptions) {
		try (InputFileMapper inputFileMapper = InputFileMapper.of(path, charset, fileMapperOptions)) {
			return parse(path, inputFileMapper, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Format<T> format //
	) {
		try (InputFileMapper inputFileMapper = InputFileMapper.ofString(content, DEFAULT_CHARSET,
			EMPTY_FILE_EXTENSION)) {
			return parse(inputFileMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Format<T> format, Factory<T> factory //
	) {
		try (InputFileMapper inputFileMapper = InputFileMapper.ofString(content, DEFAULT_CHARSET,
			EMPTY_FILE_EXTENSION)) {
			return parse(inputFileMapper, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier,
		Factory<T> factory //
	) {
		try (InputFileMapper inputFileMapper = InputFileMapper.ofString(content, DEFAULT_CHARSET, getFileExtension(
			path))) {
			return parse(inputFileMapper, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier //
	) {
		try (InputFileMapper inputFileMapper = InputFileMapper.ofString(content, DEFAULT_CHARSET, getFileExtension(
			path))) {
			return parse(inputFileMapper, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier //
	) {
		try (InputFileMapper inputFileMapper = InputFileMapper.ofString(content, DEFAULT_CHARSET, getFileExtension(
			path))) {
			return parse(path, inputFileMapper, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	private static <T> Result<T> parse(InputFileMapper inputFileMapper, Format<T> format, Supplier<T> factory) {
		return format.supportsParse() ? format.getInstance().parse(inputFileMapper, factory)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(InputFileMapper inputFileMapper, Format<T> format) {
		return format.supportsParse() ? format.getInstance().parse(inputFileMapper)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(//
		InputFileMapper inputFileMapper, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory //
	) {
		return inputFileMapper.getMainFile().getInputFileHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(inputFileMapper, format, factory));
	}

	private static <T> Result<T> parse(//
		InputFileMapper inputFileMapper, //
		FormatSupplier<T> formatSupplier //
	) {
		return inputFileMapper.getMainFile().getInputFileHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(inputFileMapper, format));
	}

	private static <T> Result<T> parse(//
		Path path, //
		InputFileMapper inputFileMapper, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier //
	) {
		return inputFileMapper.getMainFile().getInputFileHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> factorySupplier.getFactory(path, format) //
				.flatMap(factory -> parse(inputFileMapper, format, factory)));
	}

	public static <T> void save(T object, Path path, Format<T> format, FileMapper.Options... fileMapperOptions)
		throws IOException {
		save(object, path, format, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> void save(T object, Path path, Format<T> format, Charset charset,
		FileMapper.Options... fileMapperOptions) throws IOException {
		if (format.supportsSerialize()) {
			try (OutputFileMapper outputFileMapper = OutputFileMapper.of(path, charset, fileMapperOptions)) {
				format.getInstance().write(object, outputFileMapper);
			}
		}
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format,
		FileMapper.Options... fileMapperOptions)
		throws IOException {
		save(object, outStream, format, DEFAULT_CHARSET, fileMapperOptions);
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format, Charset charset,
		FileMapper.Options... fileMapperOptions)
		throws IOException {
		if (format.supportsSerialize()) {
			try (OutputFileMapper outputFileMapper = OutputFileMapper.ofOutputStream(outStream, charset,
				fileMapperOptions)) {
				format.getInstance().write(object, outputFileMapper);
			}
		}
	}

	public static <T> String print(T object, Format<T> format, FileMapper.Options... fileMapperOptions)
			throws IOException {
		if (format.supportsSerialize()) {
			try (OutputFileMapper outputFileMapper = OutputFileMapper.ofString(DEFAULT_CHARSET, fileMapperOptions)) {
				format.getInstance().write(object, outputFileMapper);
				return outputFileMapper.getMainFile().getOutputStream().toString();
			}
		}
		return "";
	}

	public static <T> Map<Path, String> printHierarchy(T object, Format<T> format, FileMapper.Options... fileMapperOptions)
			throws IOException {
		if (format.supportsSerialize()) {
			try (OutputFileMapper outputFileMapper = OutputFileMapper.ofString(DEFAULT_CHARSET, fileMapperOptions)) {
				format.getInstance().write(object, outputFileMapper);
				return outputFileMapper.getOutputStreams().entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
			}
		}
		return Collections.emptyMap();
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
