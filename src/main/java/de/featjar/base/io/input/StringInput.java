package de.featjar.base.io.input;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * A string input.
 */
public class StringInput extends AInput {
    /**
     * Creates a string input.
     *
     * @param string        the string
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StringInput(java.lang.String string, Charset charset, java.lang.String fileExtension) {
        super(new ByteArrayInputStream(string.getBytes(charset)), charset, fileExtension);
    }
}
