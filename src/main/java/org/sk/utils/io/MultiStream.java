package org.sk.utils.io;

import java.io.*;
import java.util.*;

public class MultiStream extends OutputStream {

	private final List<OutputStream> streamList = new ArrayList<>();

	public MultiStream(OutputStream... streamList) {
		super();
		this.streamList.addAll(Arrays.asList(streamList));
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
