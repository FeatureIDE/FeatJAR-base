package de.featjar.base.io.input;

import de.featjar.base.data.Maps;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Maps virtual paths to string inputs.
 */
public class StringInputMapper extends AInputMapper {
    /**
     * Creates a string input mapper for a collection of strings.
     *
     * @param pathStringMap the map of paths to inputs
     * @param rootPath      the root path
     * @param mainPath      the main path
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StringInputMapper(
            LinkedHashMap<Path, java.lang.String> pathStringMap,
            Path rootPath,
            Path mainPath,
            Charset charset,
            java.lang.String fileExtension) {
        super(relativizeRootPath(rootPath, mainPath));
        checkParameters(pathStringMap.keySet(), rootPath, mainPath);
        for (Path currentPath : pathStringMap.keySet()) {
            ioMap.put(
                    relativizeRootPath(rootPath, currentPath),
                    new StringInput(pathStringMap.get(currentPath), charset, fileExtension));
        }
    }

    /**
     * Creates a string input mapper for a single string.
     *
     * @param string        the string
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StringInputMapper(java.lang.String string, Charset charset, java.lang.String fileExtension) {
        this(Maps.of(DEFAULT_MAIN_PATH, string), null, DEFAULT_MAIN_PATH, charset, fileExtension);
    }
}
