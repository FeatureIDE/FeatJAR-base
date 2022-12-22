package de.featjar.base.io.output;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * A string output.
 */
public class StringOutput extends AOutput {
    /**
     * Creates a string output.
     *
     * @param charset the charset
     */
    public StringOutput(Charset charset) {
        super(new ByteArrayOutputStream(), charset);
    }
}
