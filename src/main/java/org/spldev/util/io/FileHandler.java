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
		try (Input in = new Input(inputStream, charset, null)) {
			return parse(in, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(Path path, Format<T> format //
	) {
		return load(path, format, DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(Path path, Format<T> format, Supplier<T> objectSupplier //
	) {
		return load(path, format, objectSupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier //
	) {
		return load(path, formatSupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, Supplier<T> objectSupplier //
	) {
		return load(path, formatSupplier, objectSupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, FactorySupplier<T> factorySupplier //
	) {
		return load(path, formatSupplier, factorySupplier, DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Charset charset //
	) {
		try (Input in = new Input(path, charset)) {
			return parse(in, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Supplier<T> factory, //
		Charset charset //
	) {
		try (Input in = new Input(path, charset)) {
			return parse(in, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory, //
		Charset charset //
	) {
		try (Input in = new Input(path, charset)) {
			return parse(in, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Charset charset //
	) {
		try (Input in = new Input(path, charset)) {
			return parse(in, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier, //
		Charset charset //
	) {
		try (Input in = new Input(path, charset)) {
			return parse(path, in, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> loadFromSource(String content, Format<T> format //
	) {
		try (Input in = new Input(content)) {
			return parse(in, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> loadFromSource(String content, Format<T> format, Factory<T> factory //
	) {
		try (Input in = new Input(content)) {
			return parse(in, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> loadFromSource(String content, Path path, FormatSupplier<T> formatSupplier,
		Factory<T> factory //
	) {
		try (Input in = new Input(content, getFileExtension(path))) {
			return parse(in, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> loadFromSource(String content, Path path, FormatSupplier<T> formatSupplier //
	) {
		try (Input in = new Input(content, getFileExtension(path))) {
			return parse(in, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> loadFromSource(String content, Path path, FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier //
	) {
		try (Input in = new Input(content, getFileExtension(path))) {
			return parse(path, in, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	private static <T> Result<T> parse(Input in, Format<T> format, Supplier<T> factory) {
		return format.supportsParse() ? format.getInstance().parse(in, factory)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(Input in, Format<T> format) {
		return format.supportsParse() ? format.getInstance().parse(in)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(//
		Input input, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory //
	) {
		return input.getInputHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(input, format, factory));
	}

	private static <T> Result<T> parse(//
		Input input, //
		FormatSupplier<T> formatSupplier //
	) {
		return input.getInputHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(input, format));
	}

	private static <T> Result<T> parse(//
		Path path, //
		Input input, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier //
	) {
		return input.getInputHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> factorySupplier.getFactory(path, format) //
				.flatMap(factory -> parse(input, format, factory)));
	}

	public static <T> void save(T object, Path path, Format<T> format) throws IOException {
		save(object, path, format, DEFAULT_CHARSET);
	}

	public static <T> void save(T object, Path path, Format<T> format, Charset charset) throws IOException {
		if (format.supportsSerialize()) {
			try (Output out = new Output(path, charset)) {
				format.getInstance().write(object, out);
			}
		}
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format)
		throws IOException {
		save(object, outStream, format, DEFAULT_CHARSET);
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format, Charset charset)
		throws IOException {
		if (format.supportsSerialize()) {
			try (Output out = new Output(outStream, charset)) {
				format.getInstance().write(object, out);
			}
		}
	}

	public static <T> void write(String source, Path path) throws IOException {
		write(source, path, DEFAULT_CHARSET);
	}

	public static <T> void write(String source, Path path, Charset charset) throws IOException {
		Files.write(path, //
			source.getBytes(charset), //
			StandardOpenOption.TRUNCATE_EXISTING, //
			StandardOpenOption.CREATE, //
			StandardOpenOption.WRITE);
	}

}
