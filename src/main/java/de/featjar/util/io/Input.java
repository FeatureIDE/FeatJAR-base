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
package de.featjar.util.io;

import de.featjar.util.data.Result;
import de.featjar.util.io.format.Format;
import de.featjar.util.log.Logger;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Input for a {@link Format}, which can be read from. Can be a physical file,
 * string, or arbitrary input stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class Input implements IOObject {
    protected final InputStream inputStream;
    protected final Charset charset;
    protected final java.lang.String fileExtension;

    protected Input(InputStream inputStream, Charset charset, java.lang.String fileExtension) {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(charset);
        Objects.requireNonNull(fileExtension);
        this.inputStream = new BufferedInputStream(inputStream);
        this.charset = charset;
        this.fileExtension = fileExtension;
    }

    public static class Stream extends Input {
        public Stream(InputStream inputStream, Charset charset, java.lang.String fileExtension) {
            super(inputStream, charset, fileExtension);
        }
    }

    public static class File extends Input {
        public File(Path path, Charset charset) throws IOException {
            super(Files.newInputStream(path, StandardOpenOption.READ), charset, IOObject.getFileExtension(path));
        }
    }

    public static class String extends Input {
        public String(java.lang.String text, Charset charset, java.lang.String fileExtension) {
            super(new ByteArrayInputStream(text.getBytes(charset)), charset, fileExtension);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public Result<java.lang.String> readText() {
        try {
            return Result.of(new java.lang.String(inputStream.readAllBytes(), charset));
        } catch (final IOException e) {
            Logger.logError(e);
            return Result.empty(e);
        }
    }

    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    public java.util.stream.Stream<java.lang.String> getLineStream() {
        return getReader().lines();
    }

    public NonEmptyLineIterator getNonEmptyLineIterator() {
        return new NonEmptyLineIterator(getReader());
    }

    public List<java.lang.String> readLines() {
        return getLineStream().collect(Collectors.toList());
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Result<InputHeader> getInputHeader() {
        final byte[] bytes = new byte[InputHeader.MAX_HEADER_SIZE];
        try {
            try {
                inputStream.mark(InputHeader.MAX_HEADER_SIZE);
                final int byteCount = inputStream.read(bytes, 0, InputHeader.MAX_HEADER_SIZE);
                return Result.of(new InputHeader(
                        fileExtension, //
                        byteCount == InputHeader.MAX_HEADER_SIZE ? bytes : Arrays.copyOf(bytes, byteCount), //
                        charset));
            } finally {
                inputStream.reset();
            }
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
