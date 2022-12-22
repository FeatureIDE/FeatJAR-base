package de.featjar.base.io.output;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An entry in a ZIP file.
 * Used to create a {@link ZIPFileOutputMapper}.
 */
public class ZIPEntryOutput extends AOutput {
    protected final Path path;

    /**
     * Creates an entry in a ZIP file.
     *
     * @param path            the path
     * @param zipOutputStream the ZIP output stream
     * @param charset         the charset
     */
    public ZIPEntryOutput(Path path, ZipOutputStream zipOutputStream, Charset charset) {
        super(zipOutputStream, charset);
        this.path = path;
    }

    @Override
    public void write(String string) throws IOException {
        ZipEntry zipEntry = new ZipEntry(path.toString());
        ((ZipOutputStream) outputStream).putNextEntry(zipEntry);
        super.write(string);
        ((ZipOutputStream) outputStream).closeEntry();
    }
}
