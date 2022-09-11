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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

/**
 * Maps paths to outputs.
 * Can represent a single output (e.g., one physical file) or a file hierarchy
 * (e.g., physical files referring to each other).
 *
 * @author Elias Kuiter
 */
public abstract class OutputMapper extends IOMapper<Output> {
    protected OutputMapper(Path mainPath) {
        super(mainPath);
    }

    protected OutputMapper(Map<Path, Output> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    protected abstract Output createOutput(Path path) throws IOException;

    /**
     * Maps virtual paths to stream outputs.
     */
    public static class Stream extends OutputMapper {
        protected Stream(Map<Path, Output> ioMap, Path mainPath) {
            super(ioMap, mainPath);
        }

        /**
         * Creates a stream output mapper for a single stream.
         *
         * @param outputStream the output stream
         * @param charset the charset
         */
        public Stream(OutputStream outputStream, Charset charset) {
            super(Map.of(DEFAULT_MAIN_PATH, new Output.Stream(outputStream, charset)), DEFAULT_MAIN_PATH);
        }

        @Override
        protected Output createOutput(Path path) {
            throw new UnsupportedOperationException("cannot guess kind of requested output stream");
        }
    }

    /**
     * Maps physical paths to physical outputs.
     */
    public static class File extends OutputMapper {
        protected final Path rootPath;
        protected final Charset charset;

        /**
         * Creates a file output mapper for a collection of files.
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
                ioMap.put(relativizeRootPath(rootPath, currentPath), new Output.File(currentPath, charset));
            }
            this.rootPath = rootPath;
            this.charset = charset;
        }

        /**
         * Creates a file output mapper for a single file.
         *
         * @param mainPath the main path
         * @param charset the charset
         */
        public File(Path mainPath, Charset charset) throws IOException {
            this(List.of(mainPath), mainPath.getParent(), mainPath, charset);
        }

        @Override
        protected Output createOutput(Path path) throws IOException {
            return new Output.File(resolveRootPath(rootPath, path), charset);
        }
    }

    /**
     * Maps virtual paths to string outputs.
     */
    public static class String extends OutputMapper {
        protected final Charset charset;

        /**
         * Creates a file output mapper for a collection of strings.
         *
         * @param charset the charset
         */
        public String(Charset charset) {
            super(Map.of(DEFAULT_MAIN_PATH, new Output.String(charset)), DEFAULT_MAIN_PATH);
            this.charset = charset;
        }

        @Override
        protected Output createOutput(Path path) {
            return new Output.String(charset);
        }

        /**
         * {@return the collection of strings}
         */
        public Map<Path, java.lang.String> getOutputStrings() {
            return ioMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
                    .getOutputStream()
                    .toString()));
        }
    }

    /**
     * Maps virtual paths to a ZIP file output.
     */
    public static class ZIPFile extends OutputMapper {
        protected final ZipOutputStream zipOutputStream;
        protected final Charset charset;

        /**
         * Creates a ZIP file output mapper.
         *
         * @param zipPath the ZIP file path
         * @param mainPath the main path
         * @param charset the charset
         */
        public ZIPFile(Path zipPath, Path mainPath, Charset charset) throws IOException {
            super(mainPath);
            this.zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toString()));
            this.charset = charset;
            ioMap.put(mainPath, new Output.ZIPEntry(mainPath, zipOutputStream, charset));
        }

        @Override
        protected Output createOutput(Path path) {
            return new Output.ZIPEntry(path, zipOutputStream, charset);
        }

        @Override
        public void close() throws IOException {
            super.close();
            zipOutputStream.close();
        }
    }

    /**
     * Maps virtual paths to a JAR file output.
     */
    public static class JARFile extends OutputMapper {
        protected final JarOutputStream jarOutputStream;
        protected final Charset charset;

        /**
         * Creates a JAR file output mapper.
         *
         * @param jarPath the JAR file path
         * @param mainPath the main path
         * @param charset the charset
         */
        public JARFile(Path jarPath, Path mainPath, Charset charset) throws IOException {
            super(mainPath);
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            this.jarOutputStream = new JarOutputStream(new FileOutputStream(jarPath.toString()), manifest);
            this.charset = charset;
            ioMap.put(mainPath, new Output.JAREntry(mainPath, jarOutputStream, charset));
        }

        @Override
        protected Output createOutput(Path path) {
            return new Output.JAREntry(path, jarOutputStream, charset);
        }

        @Override
        public void close() throws IOException {
            super.close();
            jarOutputStream.close();
        }
    }

    /**
     * {@return a file output mapper that optionaly writes to a ZIP or JAR file}
     *
     * @param mainPath the main path
     * @param charset the charset
     * @param options the {@link IOMapper} options
     * @throws IOException if an I/O exception occurs
     */
    public static OutputMapper of(Path mainPath, Charset charset, Options... options) throws IOException {
        return Arrays.asList(options).contains(Options.OUTPUT_FILE_JAR)
                ? new JARFile(IOObject.getPathWithExtension(mainPath, "jar"), mainPath.getFileName(), charset)
                : Arrays.asList(options).contains(Options.OUTPUT_FILE_ZIP)
                        ? new ZIPFile(IOObject.getPathWithExtension(mainPath, "zip"), mainPath.getFileName(), charset)
                        : new OutputMapper.File(mainPath, charset);
    }

    /**
     * A runnable that may throw an {@link IOException}.
     */
    @FunctionalInterface
    public interface IORunnable {
        void run() throws IOException;
    }

    /**
     * Temporarily shifts the focus of this output mapper to another main path to execute some function.
     * Useful to parse a {@link de.featjar.base.io.format.Format} recursively.
     *
     * @param newMainPath the new main path
     * @param ioRunnable the runnable
     * @throws IOException if an I/O exception occurs
     */
    @SuppressWarnings("resource")
    public void withMainPath(Path newMainPath, IORunnable ioRunnable) throws IOException {
        // todo: handle relative paths / subdirs?
        create(newMainPath);
        Path oldMainPath = mainPath;
        mainPath = newMainPath;
        try {
            ioRunnable.run();
        } finally {
            mainPath = oldMainPath;
        }
    }

    /**
     * {@return a new output at a given path}
     *
     * @param path the path
     * @throws IOException if an I/O exception occurs
     */
    public Output create(Path path) throws IOException {
        Optional<Output> outputOptional = super.get(path);
        if (outputOptional.isPresent()) return outputOptional.get();
        Output output = createOutput(path);
        ioMap.put(path, output);
        return output;
    }
}
