package de.featjar.util.bin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class JAR {
    public static void extractResource(Path resourcePath, Path outputPath) throws IOException {
        URL url = JAR.class.getClassLoader().getResource(resourcePath.toString());
        if (url == null)
            throw new IOException("no resource found at " + resourcePath);
        try (InputStream in = url.openStream()) {
            Files.copy(in, outputPath);
        }
    }
}
