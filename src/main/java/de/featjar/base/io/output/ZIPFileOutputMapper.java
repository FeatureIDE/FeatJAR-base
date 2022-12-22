package de.featjar.base.io.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

/**
 * Maps virtual paths to a ZIP file output.
 */
public class ZIPFileOutputMapper extends AOutputMapper {
    protected final ZipOutputStream zipOutputStream;
    protected final Charset charset;

    /**
     * Creates a ZIP file output mapper.
     *
     * @param zipPath  the ZIP file path
     * @param mainPath the main path
     * @param charset  the charset
     */
    public ZIPFileOutputMapper(Path zipPath, Path mainPath, Charset charset) throws IOException {
        super(mainPath);
        this.zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toString()));
        this.charset = charset;
        ioMap.put(mainPath, new ZIPEntryOutput(mainPath, zipOutputStream, charset));
    }

    @Override
    protected AOutput newOutput(Path path) {
        return new ZIPEntryOutput(path, zipOutputStream, charset);
    }

    @Override
    public void close() throws IOException {
        super.close();
        zipOutputStream.close();
    }
}
