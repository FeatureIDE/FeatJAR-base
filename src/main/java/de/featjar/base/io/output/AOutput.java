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

import de.featjar.base.io.IIOObject;
import de.featjar.base.io.format.IFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Writable output target of a {@link IFormat}.
 * Can be a physical file or arbitrary output stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AOutput implements IIOObject {
    protected final OutputStream outputStream;
    protected final Charset charset;

    protected AOutput(OutputStream outputStream, Charset charset) {
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(charset);
        this.outputStream = outputStream;
        this.charset = charset;
    }

    /**
     * {@return this output's charset}
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Writes a string to this output.
     *
     * @param string the string
     * @throws IOException if an I/O error occurs
     */
    public void write(java.lang.String string) throws IOException {
        outputStream.write(string.getBytes(charset));
        outputStream.flush();
    }

    /**
     * {@return this output's stream}
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
