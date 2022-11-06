/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.io;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Maps paths to inputs.
 * Can represent a single input (e.g., one physical file) or a file hierarchy
 * (e.g., physical files referring to each other).
 *
 * @author Elias Kuiter
 */
public abstract class InputMapper extends IOMapper<Input> {
    protected InputMapper(Path mainPath) {
        super(mainPath);
    }

    protected InputMapper(Map<Path, Input> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    /**
     * Maps virtual paths to stream inputs.
     */
    public static class Stream extends InputMapper {
        /**
         * Creates a stream input mapper for a collection of streams.
         *
         * @param pathInputStreamMap the map of paths to inputs
         * @param rootPath the root path
         * @param mainPath the main path
         * @param charset the charset
         * @param fileExtension the file extension
         */
        public Stream(
                Map<Path, InputStream> pathInputStreamMap,
                Path rootPath,
                Path mainPath,
                Charset charset,
                java.lang.String fileExtension) {
            super(relativizeRootPath(rootPath, mainPath));
            checkParameters(pathInputStreamMap.keySet(), rootPath, mainPath);
            for (Path currentPath : pathInputStreamMap.keySet()) {
                ioMap.put(
                        relativizeRootPath(rootPath, currentPath),
                        new Input.Stream(pathInputStreamMap.get(currentPath), charset, fileExtension));
            }
        }

        /**
         * Creates a stream input mapper for a single stream.
         *
         * @param inputStream the input stream
         * @param charset the charset
         * @param fileExtension the file extension
         */
        public Stream(InputStream inputStream, Charset charset, java.lang.String fileExtension) {
            this(Map.of(DEFAULT_MAIN_PATH, inputStream), null, DEFAULT_MAIN_PATH, charset, fileExtension);
        }
    }

    /**
     * Maps physical paths to physical file inputs.
     */
    public static class File extends InputMapper {
        /**
         * Creates a file input mapper for a collection of files.
         *
         * @param paths the list of file paths
         * @param rootPath the root path
         * @param mainPath the main path
         * @param charset the charset
         */
        public File(List<Path> paths, Path rootPath, Path mainPath, Charset charset) throws IOException {
            super(relativizeRootPath(rootPath, mainPath));
            checkParameters(paths, rootPath, mainPath);
            for (Path currentPath : paths) {
                ioMap.put(relativizeRootPath(rootPath, currentPath), new Input.File(currentPath, charset));
            }
        }

        /**
         * Creates a file input mapper for a single file or file hierarchy.
         *
         * @param mainPath the main path
         * @param charset the charset
         * @param options the {@link IOMapper} options
         */
        public File(Path mainPath, Charset charset, Options... options) throws IOException {
            this(
                    Arrays.asList(options).contains(Options.INPUT_FILE_HIERARCHY)
                            ? getFilePathsInDirectory(mainPath.getParent())
                            : List.of(mainPath),
                    mainPath.getParent(),
                    mainPath,
                    charset);
        }
    }

    /**
     * Maps virtual paths to string inputs.
     */
    public static class String extends InputMapper {
        /**
         * Creates a string input mapper for a collection of strings.
         *
         * @param pathStringMap the map of paths to inputs
         * @param rootPath the root path
         * @param mainPath the main path
         * @param charset the charset
         * @param fileExtension the file extension
         */
        public String(
                Map<Path, java.lang.String> pathStringMap,
                Path rootPath,
                Path mainPath,
                Charset charset,
                java.lang.String fileExtension) {
            super(relativizeRootPath(rootPath, mainPath));
            checkParameters(pathStringMap.keySet(), rootPath, mainPath);
            for (Path currentPath : pathStringMap.keySet()) {
                ioMap.put(
                        relativizeRootPath(rootPath, currentPath),
                        new Input.String(pathStringMap.get(currentPath), charset, fileExtension));
            }
        }

        /**
         * Creates a string input mapper for a single string.
         *
         * @param string the string
         * @param charset the charset
         * @param fileExtension the file extension
         */
        public String(java.lang.String string, Charset charset, java.lang.String fileExtension) {
            this(Map.of(DEFAULT_MAIN_PATH, string), null, DEFAULT_MAIN_PATH, charset, fileExtension);
        }
    }

    /**
     * Temporarily shifts the focus of this input mapper to another main path to execute some function.
     * Useful to parse a {@link de.featjar.base.io.format.Format} recursively.
     *
     * @param newMainPath the new main path
     * @param supplier the supplier
     * @return the result of the supplier
     * @param <T> the type of the supplier's result
     */
    public <T> Result<T> withMainPath(Path newMainPath, Supplier<Result<T>> supplier) {
        // todo: handle relative paths / subdirectories?
        if (ioMap.get(newMainPath) == null)
            return Result.empty(new Problem("could not find main path " + mainPath, Problem.Severity.WARNING));
        Path oldMainPath = mainPath;
        mainPath = newMainPath;
        Result<T> result = supplier.get();
        mainPath = oldMainPath;
        return result;
    }
}
