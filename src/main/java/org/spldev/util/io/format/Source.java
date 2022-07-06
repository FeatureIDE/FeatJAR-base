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
 * Source of data for a {@link Format}, which can be read or written to.
 * Intended for a single parse or write operation - writes to the outputStream
 * are not necessarily reflected in the inputStream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public final class Source implements AutoCloseable {
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final Charset charset;
	private final String fileExtension;

	public Source(InputStream inputStream, OutputStream outputStream, Charset charset, String fileExtension) {
		this.inputStream = inputStream; // new BufferedInputStream(inputStream);
		this.outputStream = outputStream; // new BufferedOutputStream(outputStream);
		this.charset = charset;
		this.fileExtension = fileExtension;
	}

	public Source(Path path, Charset charset) throws IOException {
		this(Files.newInputStream(path, StandardOpenOption.READ),
			Files.newOutputStream(path,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE),
			charset,
			FileHandler.getFileExtension(path));
	}

	public Source(String text, Charset charset, String fileExtension) {
		this(new ByteArrayInputStream(text.getBytes(charset)), new ByteArrayOutputStream(), charset, fileExtension);
	}

	public Charset getCharset() {
		return charset;
	}

	public Result<String> readText() {
		try {
			return Result.of(new String(inputStream.readAllBytes(), charset));
		} catch (final IOException e) {
			Logger.logError(e);
			return Result.empty(e);
		}
	}

	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(inputStream, charset));
	}

	public Stream<String> getLines() {
		return getReader().lines();
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public Result<SourceHeader> getSourceHeader() {
		final byte[] bytes = new byte[SourceHeader.MAX_HEADER_SIZE];
		try {
			try {
				inputStream.mark(SourceHeader.MAX_HEADER_SIZE);
				final int byteCount = inputStream.read(bytes, 0, SourceHeader.MAX_HEADER_SIZE);
				return Result.of(new SourceHeader(fileExtension, //
					byteCount == SourceHeader.MAX_HEADER_SIZE
						? bytes
						: Arrays.copyOf(bytes, byteCount), //
					charset));
			} finally {
				inputStream.reset();
			}
		} catch (final IOException e) {
			return Result.empty(e);
		}
	}

	public void writeText(String text) throws IOException {
		outputStream.write(text.getBytes(charset));
		outputStream.flush();
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
		outputStream.close();
	}

}
