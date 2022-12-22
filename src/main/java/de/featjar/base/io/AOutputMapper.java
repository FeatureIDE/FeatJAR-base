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

import de.featjar.base.data.Maps;
import de.featjar.base.io.format.IFormat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

/**
 * Maps paths to outputs.
 * Can represent a single output (e.g., one physical file) or a file hierarchy
 * (e.g., physical files referring to each other).
 *
 * @author Elias Kuiter
 */
public abstract class AOutputMapper extends AIOMapper<AOutput> {
    protected AOutputMapper(Path mainPath) {
        super(mainPath);
    }

    protected AOutputMapper(LinkedHashMap<Path, AOutput> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    protected abstract AOutput newOutput(Path path) throws IOException;

    /**
     * Maps virtual paths to stream outputs.
     */
    public static class Stream extends AOutputMapper {
        protected Stream(LinkedHashMap<Path, AOutput> ioMap, Path mainPath) {
            super(ioMap, mainPath);
        }

        /**
         * Creates a stream output mapper for a single stream.
         *
         * @param outputStream the output stream
         * @param charset the charset
         */
        public Stream(OutputStream outputStream, Charset charset) {
            super(Maps.of(DEFAULT_MAIN_PATH, new AOutput.Stream(outputStream, charset)), DEFAULT_MAIN_PATH);
        }

        @Override
        protected AOutput newOutput(Path path) {
            throw new UnsupportedOperationException("cannot guess kind of requested output stream");
        }
    }

    /**
     * Maps physical paths to physical outputs.
     */
    public static class File extends AOutputMapper {
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
                ioMap.put(relativizeRootPath(rootPath, currentPath), new AOutput.File(currentPath, charset));
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
        protected AOutput newOutput(Path path) throws IOException {
            return new AOutput.File(resolveRootPath(rootPath, path), charset);
        }
    }

    /**
     * Maps virtual paths to string outputs.
     */
    public static class String extends AOutputMapper {
        protected final Charset charset;

        /**
         * Creates a file output mapper for a collection of strings.
         *
         * @param charset the charset
         */
        public String(Charset charset) {
            super(Maps.of(DEFAULT_MAIN_PATH, new AOutput.String(charset)), DEFAULT_MAIN_PATH);
            this.charset = charset;
        }

        @Override
        protected AOutput newOutput(Path path) {
            return new AOutput.String(charset);
        }

        /**
         * {@return the collection of strings}
         */
        public LinkedHashMap<Path, java.lang.String> getOutputStrings() {
            return ioMap.entrySet().stream().collect(
                    Maps.toMap(Map.Entry::getKey, e -> e.getValue().getOutputStream().toString()));
        }
    }

    /**
     * Maps virtual paths to a ZIP file output.
     */
    public static class ZIPFile extends AOutputMapper {
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
            ioMap.put(mainPath, new AOutput.ZIPEntry(mainPath, zipOutputStream, charset));
        }

        @Override
        protected AOutput newOutput(Path path) {
            return new AOutput.ZIPEntry(path, zipOutputStream, charset);
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
    public static class JARFile extends AOutputMapper {
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
            ioMap.put(mainPath, new AOutput.JAREntry(mainPath, jarOutputStream, charset));
        }

        @Override
        protected AOutput newOutput(Path path) {
            return new AOutput.JAREntry(path, jarOutputStream, charset);
        }

        @Override
        public void close() throws IOException {
            super.close();
            jarOutputStream.close();
        }
    }

    /**
     * {@return a file output mapper that optionally writes to a ZIP or JAR file}
     *
     * @param mainPath the main path
     * @param charset the charset
     * @param options the {@link AIOMapper} options
     * @throws IOException if an I/O exception occurs
     */
    public static AOutputMapper of(Path mainPath, Charset charset, Options... options) throws IOException {
        return Arrays.asList(options).contains(Options.OUTPUT_FILE_JAR)
                ? new JARFile(IIOObject.getPathWithExtension(mainPath, "jar"), mainPath.getFileName(), charset)
                : Arrays.asList(options).contains(Options.OUTPUT_FILE_ZIP)
                        ? new ZIPFile(IIOObject.getPathWithExtension(mainPath, "zip"), mainPath.getFileName(), charset)
                        : new AOutputMapper.File(mainPath, charset);
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
     * Useful to parse a {@link IFormat} recursively.
     *
     * @param newMainPath the new main path
     * @param ioRunnable the runnable
     * @throws IOException if an I/O exception occurs
     */
    @SuppressWarnings("resource")
    public void withMainPath(Path newMainPath, IORunnable ioRunnable) throws IOException {
        // TODO: are relative paths and subdirectories handled correctly?
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
    public AOutput create(Path path) throws IOException {
        Optional<AOutput> outputOptional = super.get(path);
        if (outputOptional.isPresent()) return outputOptional.get();
        AOutput output = newOutput(path);
        ioMap.put(path, output);
        return output;
    }
}
