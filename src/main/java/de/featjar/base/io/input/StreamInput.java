package de.featjar.base.io.input;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A stream input.
 */
public class StreamInput extends AInput {
    /**
     * Creates a stream input.
     *
     * @param inputStream   the input stream
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StreamInput(InputStream inputStream, Charset charset, java.lang.String fileExtension) {
        super(inputStream, charset, fileExtension);
    }
}
