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
package org.spldev.util.io.format;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.spldev.util.data.Result;
import org.spldev.util.io.*;
import org.spldev.util.logging.*;

/**
 * Input for a format.
 * 
 * @author Sebastian Krieter
 */
public final class Input implements AutoCloseable {

	private final InputStream source;

	private final Charset charset;

	private final String fileExtension;

	public Input(InputStream source, Charset charset, String fileExtension) {
		this.source = source;
		this.charset = charset;
		this.fileExtension = fileExtension;
	}

	public Input(Path path, Charset charset) throws IOException {
		this(new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ)), charset, FileHandler
			.getFileExtension(path));
	}

	public Input(String text, Charset charset, String fileExtension) {
		this(new ByteArrayInputStream(text.getBytes(charset)), charset, fileExtension);
	}

	public Input(String text, String fileExtension) {
		this(text, StandardCharsets.UTF_8, fileExtension);
	}

	public Input(String text) {
		this(text, StandardCharsets.UTF_8, null);
	}

	public Charset getCharset() {
		return charset;
	}

	public Result<String> getCompleteText() {
		try {
			return Result.of(new String(source.readAllBytes(), charset));
		} catch (final IOException e) {
			Logger.logError(e);
			return Result.empty(e);
		}
	}

	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(source, charset));
	}

	public Stream<String> getLines() {
		return getReader().lines();
	}

	public InputStream getInputStream() {
		return source;
	}

	public Result<InputHeader> getInputHeader() {
		final byte[] bytes = new byte[InputHeader.MAX_HEADER_SIZE];
		try {
			try {
				source.mark(InputHeader.MAX_HEADER_SIZE);
				final int byteCount = source.read(bytes, 0, InputHeader.MAX_HEADER_SIZE);
				return Result.of(new InputHeader(fileExtension, //
					byteCount == InputHeader.MAX_HEADER_SIZE
						? bytes
						: Arrays.copyOf(bytes, byteCount), //
					charset));
			} finally {
				source.reset();
			}
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	@Override
	public void close() throws IOException {
		source.close();
	}

}
