package org.spldev.utils.io.formats;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

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
		final int extensionIndex = fileName.lastIndexOf('.');
		return (extensionIndex > 0) ? fileName.substring(extensionIndex + 1) : "";
	}

	public static <T> Optional<T> read(Path path, Format<T> format, Charset charset) throws IOException {
		if (!Files.exists(path)) {
			throw new FileNotFoundException(path.toString());
		}
		return format.getInstance().parse(new String(Files.readAllBytes(path), charset));
	}

	public static <T> void write(T object, Path path, Format<T> format, Charset charset) throws IOException {
		Files.write(path, //
			format.getInstance().serialize(object).getBytes(charset), //
			StandardOpenOption.TRUNCATE_EXISTING, //
			StandardOpenOption.CREATE, //
			StandardOpenOption.WRITE);
	}

	public static <T> Optional<T> read(Path path, Format<T> format) throws IOException {
		return read(path, format, DEFAULT_CHARSET);
	}

	public static <T> void write(T object, Path path, Format<T> format) throws IOException {
		write(object, path, format, DEFAULT_CHARSET);
	}

	private Path path;
	private Format<T> format;
	private Charset charset = DEFAULT_CHARSET;

	public FileHandler(Format<T> format) {
		setFormat(format);
	}

	public FileHandler(Path path, Format<T> format) {
		setPath(path);
		setFormat(format);
	}

	public void write(T object) throws IOException {
		write(object, path, format, charset);
	}

	public void write(T object, Path path) throws IOException {
		write(object, path, format, charset);
	}

	public Optional<T> read(Path path) throws IOException {
		return read(path, format, charset);
	}

	public Charset getCharset() {
		return charset;
	}

	public Format<T> getFormat() {
		return format;
	}

	public Path getPath() {
		return path;
	}

	public void setCharset(Charset charset) {
		Objects.requireNonNull(charset);
		this.charset = charset;
	}

	public void setFormat(Format<T> format) {
		Objects.requireNonNull(format);
		this.format = format;
	}

	public void setPath(Path path) {
		Objects.requireNonNull(path);
		this.path = path;
	}

}
