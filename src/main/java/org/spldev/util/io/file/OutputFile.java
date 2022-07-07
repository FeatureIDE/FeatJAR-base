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

import org.spldev.util.io.format.Format;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Output file for a {@link Format}, which can be written to. Can be a physical
 * file or arbitrary output stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class OutputFile implements File {
	protected final OutputStream outputStream;
	protected final Charset charset;

	public OutputFile(OutputStream outputStream, Charset charset) {
		this.outputStream = outputStream; // new BufferedOutputStream(outputStream);
		this.charset = charset;
	}

	public OutputFile(Path path, Charset charset) throws IOException {
		this(Files.newOutputStream(path,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.CREATE,
			StandardOpenOption.WRITE),
			charset);
	}

	public OutputFile(Charset charset) {
		this(new ByteArrayOutputStream(), charset);
	}

	public Charset getCharset() {
		return charset;
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
		outputStream.close();
	}

	public static class ZIP extends OutputFile {
		protected final Path path;

		public ZIP(Path path, ZipOutputStream zipOutputStream, Charset charset) {
			super(zipOutputStream, charset);
			this.path = path;
		}

		@Override
		public void writeText(String text) throws IOException {
			ZipEntry zipEntry = new ZipEntry(path.toString());
			((ZipOutputStream) outputStream).putNextEntry(zipEntry);
			super.writeText(text);
			((ZipOutputStream) outputStream).closeEntry();
		}
	}
}
