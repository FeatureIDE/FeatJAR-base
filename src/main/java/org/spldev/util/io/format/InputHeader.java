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

import java.nio.charset.*;
import java.util.stream.*;

/**
 * Input header to determine whether a format can parse a particular content.
 * 
 * @author Sebastian Krieter
 */
public class InputHeader {

	/**
	 * Maximum number of bytes in the header.
	 */
	public static final int MAX_HEADER_SIZE = 0x00100000; // 1 MiB

	private final byte[] header;

	private final Charset charset;

	private final String fileExtension;

	public InputHeader(String fileExtension, byte[] header, Charset charset) {
		this.fileExtension = fileExtension;
		this.header = header;
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public byte[] getBytes() {
		return header;
	}

	public String getText() {
		return new String(header, charset);
	}

	public Stream<String> getLines() {
		return getText().lines();
	}

}
