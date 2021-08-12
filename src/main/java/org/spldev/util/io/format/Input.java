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

import org.spldev.util.*;
import org.spldev.util.logging.*;

/**
 * Input for a format.
 * 
 * @author Sebastian Krieter
 */
public final class Input implements AutoCloseable {

	private final InputStream source;

	private final Charset charset;

	private final Path path;

	public Input(Path path, Charset charset) throws IOException {
		if (!Files.exists(path)) {
			throw new FileNotFoundException(path.toString());
		}
		source = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ));
		this.path = path;
		this.charset = charset;
	}

	public Input(String text) {
		this(text, null);
	}

	public Input(String text, Path path) {
		source = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		this.path = null;
		charset = StandardCharsets.UTF_8;
	}

	public Charset getCharset() {
		return charset;
	}

	public Path getPath() {
		return path;
	}

	public Result<String> getCompleteText() {
		try {
			return Result.of(new String(source.readAllBytes(), charset));
		} catch (final IOException e) {
			Logger.logError(e);
			return Result.empty(e);
		}
	}

	public Stream<String> getLines() {
		return new BufferedReader(new InputStreamReader(source, charset)).lines();
	}

	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(source, charset));
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
				return Result.of(new InputHeader(path, //
					byteCount == InputHeader.MAX_HEADER_SIZE ? bytes : Arrays.copyOf(bytes, byteCount), //
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
