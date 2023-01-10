package de.featjar.base.io.input;

import de.featjar.base.io.IIOObject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A physical file input.
 */
public class FileInput extends AInput {
    /**
     * Creates a physical file input.
     *
     * @param path    the path
     * @param charset the charset
     * @throws IOException if an I/O error occurs
     */
    public FileInput(Path path, Charset charset) throws IOException {
        super(
                Files.newInputStream(path, StandardOpenOption.READ),
                charset,
                IIOObject.getFileExtension(path).orElse(null));
    }
}
