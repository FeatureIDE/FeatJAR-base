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
package org.spldev.util.io.file;

import org.spldev.util.data.Result;
import org.spldev.util.io.FileHandler;
import org.spldev.util.io.format.Format;
import org.spldev.util.logging.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Input file for a {@link Format}, which can be read from. Can be a physical
 * file, string, or arbitrary input stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public final class InputFile implements File {
	private final InputStream inputStream;
	private final Charset charset;
	private final String fileExtension;

	public InputFile(InputStream inputStream, Charset charset, String fileExtension) {
		this.inputStream = inputStream; // new BufferedInputStream(inputStream);
		this.charset = charset;
		this.fileExtension = fileExtension;
	}

	public InputFile(Path path, Charset charset) throws IOException {
		this(Files.newInputStream(path, StandardOpenOption.READ),
			charset,
			FileHandler.getFileExtension(path));
	}

	public InputFile(String text, Charset charset, String fileExtension) {
		this(new ByteArrayInputStream(text.getBytes(charset)), charset, fileExtension);
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

	public Result<InputFileHeader> getInputFileHeader() {
		final byte[] bytes = new byte[InputFileHeader.MAX_HEADER_SIZE];
		try {
			try {
				inputStream.mark(InputFileHeader.MAX_HEADER_SIZE);
				final int byteCount = inputStream.read(bytes, 0, InputFileHeader.MAX_HEADER_SIZE);
				return Result.of(new InputFileHeader(fileExtension, //
					byteCount == InputFileHeader.MAX_HEADER_SIZE
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

	@Override
	public void close() throws IOException {
		inputStream.close();
	}

}
