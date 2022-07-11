/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
 * 
 * This file is part of util.
 * 
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 * 
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/FeatJAR/util> for further information.
 * -----------------------------------------------------------------------------
 */
package de.featjar.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiStream extends OutputStream {

	private final List<OutputStream> streamList = new ArrayList<>();

	public MultiStream(OutputStream... streamList) {
		super();
		this.streamList.addAll(Arrays.asList(streamList));
	}

	public MultiStream(List<OutputStream> streamList) {
		super();
		this.streamList.addAll(new ArrayList<>(streamList));
	}

	@Override
	public void flush() throws IOException {
		for (final OutputStream outputStream : streamList) {
			try {
				outputStream.flush();
			} catch (final IOException e) {
			}
		}
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		for (final OutputStream outputStream : streamList) {
			try {
				outputStream.write(buf, off, len);
			} catch (final IOException e) {
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		for (final OutputStream outputStream : streamList) {
			try {
				outputStream.write(b);
			} catch (final IOException e) {
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		for (final OutputStream outputStream : streamList) {
			try {
				outputStream.write(b);
			} catch (final IOException e) {
			}
		}
	}

}
