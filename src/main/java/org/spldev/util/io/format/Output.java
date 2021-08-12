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

/**
 * Output for a format.
 * 
 * @author Sebastian Krieter
 */
public class Output implements AutoCloseable {

	private final OutputStream target;

	private final Charset charset;

	private final Path path;

	public Output(Path path, Charset charset) throws IOException {
		target = new BufferedOutputStream(Files.newOutputStream(path, //
			StandardOpenOption.TRUNCATE_EXISTING, //
			StandardOpenOption.CREATE, //
			StandardOpenOption.WRITE));
		this.path = path;
		this.charset = charset;
	}

	public void writeText(String text) throws IOException {
		target.write(text.getBytes(charset));
		target.flush();
	}

	public OutputStream getOutputStream() {
		return target;
	}

	public Charset getCharset() {
		return charset;
	}

	public Path getPath() {
		return path;
	}

	@Override
	public void close() throws IOException {
		target.close();
	}

}
