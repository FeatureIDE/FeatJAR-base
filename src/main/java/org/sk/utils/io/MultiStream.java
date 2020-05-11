package org.sk.utils.io;

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

	public void flush() throws IOException {
		for (OutputStream outputStream : streamList) {
			try {
				outputStream.flush();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		for (OutputStream outputStream : streamList) {
			try {
				outputStream.write(buf, off, len);
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream outputStream : streamList) {
			try {
				outputStream.write(b);
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		for (OutputStream outputStream : streamList) {
			try {
				outputStream.write(b);
			} catch (IOException e) {
			}
		}
	}

}
