package org.spldev.util.io;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

import org.spldev.util.*;
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

	public static String read(Path path, Charset charset) throws IOException {
		if (!Files.exists(path)) {
			throw new FileNotFoundException(path.toString());
		}
		return new String(Files.readAllBytes(path), charset);
	}

	public static String read(Path path) throws IOException {
		return read(path, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(Path path, Format<T> format, Charset charset) throws IOException {
		return format.getInstance().parse(read(path, charset));
	}

	public static <T> Result<T> parse(Path path, Format<T> format) throws IOException {
		return parse(path, format, DEFAULT_CHARSET);
	}

	public static <T> Result<T> parse(Path path, FormatSupplier<T> formatSupplier, Charset charset) {
		try {
			final String content = read(path, charset);
			return formatSupplier.getFormat(content, getFileExtension(path))
				.flatMap(f -> f.getInstance().parse(content));
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public static <T> Result<T> parse(Path path, FormatSupplier<T> formatSupplier) {
		return parse(path, formatSupplier, DEFAULT_CHARSET);
	}

	public static <T> void write(T object, Path path, Format<T> format, Charset charset) throws IOException {
		Files.write(path, //
			format.getInstance().serialize(object).getBytes(charset), //
			StandardOpenOption.TRUNCATE_EXISTING, //
			StandardOpenOption.CREATE, //
			StandardOpenOption.WRITE);
	}

	public static <T> void write(T object, Path path, Format<T> format) throws IOException {
		write(object, path, format, DEFAULT_CHARSET);
	}

}
