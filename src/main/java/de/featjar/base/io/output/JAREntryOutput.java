/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * An entry in a JAR file.
 * Used to create a {@link JARFileOutputMapper}.
 *
 * @author Elias Kuiter
 */
public class JAREntryOutput extends AOutput {
    protected final Path path;

    /**
     * Creates an entry in a JAR file.
     *
     * @param path            the path
     * @param jarOutputStream the JAR output stream
     * @param charset         the charset
     */
    public JAREntryOutput(Path path, JarOutputStream jarOutputStream, Charset charset) {
        super(jarOutputStream, charset);
        this.path = path;
    }

    @Override
    public void write(java.lang.String string) throws IOException {
        JarEntry jarEntry = new JarEntry(path.toString());
        ((JarOutputStream) outputStream).putNextEntry(jarEntry);
        super.write(string);
        ((JarOutputStream) outputStream).closeEntry();
    }
}
