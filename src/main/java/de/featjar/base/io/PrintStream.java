package de.featjar.base.io;

import java.io.OutputStream;

/**
 * A {@link java.io.PrintStream} that exposes its wrapped {@link OutputStream}.
 *
 * @author Elias Kuiter
 */
public class PrintStream extends java.io.PrintStream {

    public PrintStream(OutputStream out) {
        super(out);
    }

    public OutputStream getOutputStream() {
        return out;
    }
}
