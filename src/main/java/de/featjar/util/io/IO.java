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

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.function.*;

import de.featjar.util.data.Factory;
import de.featjar.util.data.FactorySupplier;
import de.featjar.util.data.Result;
import de.featjar.util.io.format.Format;
import de.featjar.util.io.format.FormatSupplier;
import de.featjar.util.data.*;
import de.featjar.util.io.format.*;

/**
 * Enables reading and writing of a file in a certain {@link Format}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class IO {
	public static <T> Result<T> load(//
		InputStream inputStream, //
		Format<T> format //
	) {
		return load(inputStream, format, IOObject.DEFAULT_CHARSET);
	}

	public static <T> Result<T> load(//
		InputStream inputStream, //
		Format<T> format, //
		Charset charset //
	) {
		try (InputMapper inputMapper = new InputMapper.Stream(inputStream, charset,
			IOObject.EMPTY_FILE_EXTENSION)) {
			return parse(inputMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(Path path, Format<T> format, //
		IOMapper.Options... ioMapperOptions) {
		return load(path, format, IOObject.DEFAULT_CHARSET, ioMapperOptions);
	}

	public static <T> Result<T> load(Path path, Format<T> format, Supplier<T> objectSupplier, //
		IOMapper.Options... ioMapperOptions) {
		return load(path, format, objectSupplier, IOObject.DEFAULT_CHARSET, ioMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, //
		IOMapper.Options... ioMapperOptions) {
		return load(path, formatSupplier, IOObject.DEFAULT_CHARSET, ioMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, Supplier<T> objectSupplier, //
		IOMapper.Options... ioMapperOptions) {
		return load(path, formatSupplier, objectSupplier, IOObject.DEFAULT_CHARSET, ioMapperOptions);
	}

	public static <T> Result<T> load(Path path, FormatSupplier<T> formatSupplier, FactorySupplier<T> factorySupplier, //
		IOMapper.Options... ioMapperOptions) {
		return load(path, formatSupplier, factorySupplier, IOObject.DEFAULT_CHARSET, ioMapperOptions);
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Charset charset, //
		IOMapper.Options... ioMapperOptions) {
		try (InputMapper inputMapper = new InputMapper.File(path, charset, ioMapperOptions)) {
			return parse(inputMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		Format<T> format, //
		Supplier<T> factory, //
		Charset charset, //
		IOMapper.Options... ioMapperOptions) {
		try (InputMapper inputMapper = new InputMapper.File(path, charset, ioMapperOptions)) {
			return parse(inputMapper, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory, //
		Charset charset, //
		IOMapper.Options... ioMapperOptions) {
		try (InputMapper inputMapper = new InputMapper.File(path, charset, ioMapperOptions)) {
			return parse(inputMapper, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		Charset charset, //
		IOMapper.Options... ioMapperOptions) {
		try (InputMapper inputMapper = new InputMapper.File(path, charset, ioMapperOptions)) {
			return parse(inputMapper, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(//
		Path path, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier, //
		Charset charset, //
		IOMapper.Options... ioMapperOptions) {
		try (InputMapper inputMapper = new InputMapper.File(path, charset, ioMapperOptions)) {
			return parse(path, inputMapper, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Format<T> format //
	) {
		try (InputMapper inputMapper = new InputMapper.String(content, IOObject.DEFAULT_CHARSET,
			IOObject.EMPTY_FILE_EXTENSION)) {
			return parse(inputMapper, format);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Format<T> format, Factory<T> factory //
	) {
		try (InputMapper inputMapper = new InputMapper.String(content, IOObject.DEFAULT_CHARSET,
			IOObject.EMPTY_FILE_EXTENSION)) {
			return parse(inputMapper, format, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier,
		Factory<T> factory //
	) {
		try (InputMapper inputMapper = new InputMapper.String(content, IOObject.DEFAULT_CHARSET, IOObject
			.getFileExtension(
				path))) {
			return parse(inputMapper, formatSupplier, factory);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier //
	) {
		try (InputMapper inputMapper = new InputMapper.String(content, IOObject.DEFAULT_CHARSET, IOObject
			.getFileExtension(
				path))) {
			return parse(inputMapper, formatSupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> load(String content, Path path, FormatSupplier<T> formatSupplier,
		FactorySupplier<T> factorySupplier //
	) {
		try (InputMapper inputMapper = new InputMapper.String(content, IOObject.DEFAULT_CHARSET, IOObject
			.getFileExtension(
				path))) {
			return parse(path, inputMapper, formatSupplier, factorySupplier);
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	private static <T> Result<T> parse(InputMapper inputMapper, Format<T> format, Supplier<T> factory) {
		return format.supportsParse() ? format.getInstance().parse(inputMapper, factory)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(InputMapper inputMapper, Format<T> format) {
		return format.supportsParse() ? format.getInstance().parse(inputMapper)
			: Result.empty(new UnsupportedOperationException(format.toString()));
	}

	private static <T> Result<T> parse(//
		InputMapper inputMapper, //
		FormatSupplier<T> formatSupplier, //
		Supplier<T> factory //
	) {
		return inputMapper.get().getInputHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(inputMapper, format, factory));
	}

	private static <T> Result<T> parse(//
		InputMapper inputMapper, //
		FormatSupplier<T> formatSupplier //
	) {
		return inputMapper.get().getInputHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> parse(inputMapper, format));
	}

	private static <T> Result<T> parse(//
		Path path, //
		InputMapper inputMapper, //
		FormatSupplier<T> formatSupplier, //
		FactorySupplier<T> factorySupplier //
	) {
		return inputMapper.get().getInputHeader().flatMap(formatSupplier::getFormat) //
			.flatMap(format -> factorySupplier.getFactory(path, format) //
				.flatMap(factory -> parse(inputMapper, format, factory)));
	}

	public static <T> void save(T object, Path path, Format<T> format, IOMapper.Options... ioMapperOptions)
		throws IOException {
		save(object, path, format, IOObject.DEFAULT_CHARSET, ioMapperOptions);
	}

	public static <T> void save(T object, Path path, Format<T> format, Charset charset,
		IOMapper.Options... ioMapperOptions) throws IOException {
		if (format.supportsSerialize()) {
			try (OutputMapper outputMapper = OutputMapper.of(path, charset, ioMapperOptions)) {
				format.getInstance().write(object, outputMapper);
			}
		}
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format)
		throws IOException {
		save(object, outStream, format, IOObject.DEFAULT_CHARSET);
	}

	public static <T> void save(T object, OutputStream outStream, Format<T> format, Charset charset)
		throws IOException {
		if (format.supportsSerialize()) {
			try (OutputMapper outputMapper = new OutputMapper.Stream(outStream, charset)) {
				format.getInstance().write(object, outputMapper);
			}
		}
	}

	public static <T> String print(T object, Format<T> format)
		throws IOException {
		if (format.supportsSerialize()) {
			try (OutputMapper outputMapper = new OutputMapper.String(IOObject.DEFAULT_CHARSET)) {
				format.getInstance().write(object, outputMapper);
				return outputMapper.get().getOutputStream().toString();
			}
		}
		return "";
	}

	public static <T> Map<Path, String> printHierarchy(T object, Format<T> format)
		throws IOException {
		if (format.supportsSerialize()) {
			try (OutputMapper.String outputMapper = new OutputMapper.String(IOObject.DEFAULT_CHARSET)) {
				format.getInstance().write(object, outputMapper);
				return outputMapper.getOutputStrings();
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
		write(content, path, IOObject.DEFAULT_CHARSET);
	}

}
