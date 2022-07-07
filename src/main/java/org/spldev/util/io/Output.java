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

import org.spldev.util.io.format.Format;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Output for a {@link Format}, which can be written to. Can be a physical
 * file or arbitrary output stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class Output implements IOObject {
	protected final OutputStream outputStream;
	protected final Charset charset;

	protected Output(OutputStream outputStream, Charset charset) {
		Objects.requireNonNull(outputStream);
		Objects.requireNonNull(charset);
		this.outputStream = outputStream;
		this.charset = charset;
	}

	public static class Stream extends Output {
		public Stream(OutputStream outputStream, Charset charset) {
			super(outputStream, charset);
		}
	}

	public static class File extends Output {
		public File(Path path, Charset charset) throws IOException {
			super(Files.newOutputStream(path,
							StandardOpenOption.TRUNCATE_EXISTING,
							StandardOpenOption.CREATE,
							StandardOpenOption.WRITE),
					charset);
		}
	}

	public static class String extends Output {
		public String(Charset charset) {
			super(new ByteArrayOutputStream(), charset);
		}
	}

	public static class ZIPEntry extends Output {
		protected final Path path;

		public ZIPEntry(Path path, ZipOutputStream zipOutputStream, Charset charset) {
			super(zipOutputStream, charset);
			this.path = path;
		}

		@Override
		public void writeText(java.lang.String text) throws IOException {
			ZipEntry zipEntry = new ZipEntry(path.toString());
			((ZipOutputStream) outputStream).putNextEntry(zipEntry);
			super.writeText(text);
			((ZipOutputStream) outputStream).closeEntry();
		}
	}

	public static class JAREntry extends Output {
		protected final Path path;

		public JAREntry(Path path, JarOutputStream jarOutputStream, Charset charset) {
			super(jarOutputStream, charset);
			this.path = path;
		}

		@Override
		public void writeText(java.lang.String text) throws IOException {
			JarEntry jarEntry = new JarEntry(path.toString());
			((JarOutputStream) outputStream).putNextEntry(jarEntry);
			super.writeText(text);
			((JarOutputStream) outputStream).closeEntry();
		}
	}

	public Charset getCharset() {
		return charset;
	}

	public void writeText(java.lang.String text) throws IOException {
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
}
