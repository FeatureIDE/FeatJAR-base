/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A physical file output.
 *
 * @author Elias Kuiter
 */
public class FileOutput extends AOutput {
    /**
     * Creates a physical file output.
     *
     * @param path    the path
     * @param charset the charset
     * @throws IOException if an I/O error occurs
     */
    public FileOutput(Path path, Charset charset) throws IOException {
        super(newOutputStream(path), charset);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static OutputStream newOutputStream(Path path) throws IOException {
        // TODO: currently, we always allow creating new files. this could be weakened with a flag, if necessary.
        //  also, we always truncate files. we could consider allowing appending to files as well.
        final Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        return Files.newOutputStream(
                path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
