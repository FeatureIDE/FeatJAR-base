package de.featjar.base.io.input;

import de.featjar.base.io.AIOMapper;
import de.featjar.base.io.IOMapperOptions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Maps physical paths to physical file inputs.
 */
public class FileInputMapper extends AInputMapper {
    /**
     * Creates a file input mapper for a collection of files.
     *
     * @param paths    the list of file paths
     * @param rootPath the root path
     * @param mainPath the main path
     * @param charset  the charset
     */
    public FileInputMapper(List<Path> paths, Path rootPath, Path mainPath, Charset charset) throws IOException {
        super(relativizeRootPath(rootPath, mainPath));
        checkParameters(paths, rootPath, mainPath);
        for (Path currentPath : paths) {
            ioMap.put(relativizeRootPath(rootPath, currentPath), new FileInput(currentPath, charset));
        }
    }

    /**
     * Creates a file input mapper for a single file or file hierarchy.
     *
     * @param mainPath the main path
     * @param charset  the charset
     * @param options  the {@link AIOMapper} options
     */
    public FileInputMapper(Path mainPath, Charset charset, IOMapperOptions... options) throws IOException {
        this(
                Arrays.asList(options).contains(IOMapperOptions.INPUT_FILE_HIERARCHY)
                        ? getFilePathsInDirectory(mainPath.getParent())
                        : List.of(mainPath),
                mainPath.getParent(),
                mainPath,
                charset);
    }
}
