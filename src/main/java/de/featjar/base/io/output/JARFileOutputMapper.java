package de.featjar.base.io.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Maps virtual paths to a JAR file output.
 */
public class JARFileOutputMapper extends AOutputMapper {
    protected final JarOutputStream jarOutputStream;
    protected final Charset charset;

    /**
     * Creates a JAR file output mapper.
     *
     * @param jarPath  the JAR file path
     * @param mainPath the main path
     * @param charset  the charset
     */
    public JARFileOutputMapper(Path jarPath, Path mainPath, Charset charset) throws IOException {
        super(mainPath);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        this.jarOutputStream = new JarOutputStream(new FileOutputStream(jarPath.toString()), manifest);
        this.charset = charset;
        ioMap.put(mainPath, new JAREntryOutput(mainPath, jarOutputStream, charset));
    }

    @Override
    protected AOutput newOutput(Path path) {
        return new JAREntryOutput(path, jarOutputStream, charset);
    }

    @Override
    public void close() throws IOException {
        super.close();
        jarOutputStream.close();
    }
}
