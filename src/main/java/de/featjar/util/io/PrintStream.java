package de.featjar.util.io;

import java.io.OutputStream;

public class PrintStream extends java.io.PrintStream {

    public PrintStream(OutputStream out) {
        super(out);
    }

    public OutputStream getOutputStream() {
        return out;
    }
}
