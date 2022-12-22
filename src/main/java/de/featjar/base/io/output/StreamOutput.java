package de.featjar.base.io.output;

import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A stream output.
 */
public class StreamOutput extends AOutput {
    /**
     * Creates a stream output.
     *
     * @param outputStream the output stream
     * @param charset      the charset
     */
    public StreamOutput(OutputStream outputStream, Charset charset) {
        super(outputStream, charset);
    }
}
