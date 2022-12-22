package de.featjar.base.io.output;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * An entry in a JAR file.
 * Used to create a {@link JARFileOutputMapper}.
 */
public class JAREntryOutput extends AOutput {
    protected final Path path;

    /**
     * Creates an entry in a JAR file.
     *
     * @param path            the path
     * @param jarOutputStream the JAR output stream
     * @param charset         the charset
     */
    public JAREntryOutput(Path path, JarOutputStream jarOutputStream, Charset charset) {
        super(jarOutputStream, charset);
        this.path = path;
    }

    @Override
    public void write(java.lang.String string) throws IOException {
        JarEntry jarEntry = new JarEntry(path.toString());
        ((JarOutputStream) outputStream).putNextEntry(jarEntry);
        super.write(string);
        ((JarOutputStream) outputStream).closeEntry();
    }
}
