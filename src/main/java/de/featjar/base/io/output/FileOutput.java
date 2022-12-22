package de.featjar.base.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A physical file output.
 */
public class FileOutput extends AOutput {
    /**
     * Creates a physical file output.
     *
     * @param path    the path
     * @param charset the charset
     * @throws IOException if an I/O error occurs
     */
    public FileOutput(Path path, Charset charset) throws IOException {
        super(newOutputStream(path), charset);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static OutputStream newOutputStream(Path path) throws IOException {
        // TODO: currently, we always allow creating new files. this could be weakened with a flag, if necessary.
        // TODO: also, we always truncate files. we could consider allowing appending to files as well.
        if (path.getParent() != null)
            path.getParent().toFile().mkdirs();
        return Files.newOutputStream(
                path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
