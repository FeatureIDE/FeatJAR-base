/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An entry in a ZIP file.
 * Used to create a {@link ZIPFileOutputMapper}.
 *
 * @author Elias Kuiter
 */
public class ZIPEntryOutput extends AOutput {
    protected final Path path;

    /**
     * Creates an entry in a ZIP file.
     *
     * @param path            the path
     * @param zipOutputStream the ZIP output stream
     * @param charset         the charset
     */
    public ZIPEntryOutput(Path path, ZipOutputStream zipOutputStream, Charset charset) {
        super(zipOutputStream, charset);
        this.path = path;
    }

    @Override
    public void write(String string) throws IOException {
        ZipEntry zipEntry = new ZipEntry(path.toString());
        ((ZipOutputStream) outputStream).putNextEntry(zipEntry);
        super.write(string);
        ((ZipOutputStream) outputStream).closeEntry();
    }
}
