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
package de.featjar.base.io.input;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Maps physical paths to physical file inputs.
 *
 * @author Elias Kuiter
 */
public class ZIPFileInputMapper extends AInputMapper {

    /**
     * Creates a file input mapper for a single file or file hierarchy.
     *
     * @param mainPath the main path
     * @param charset  the charset
     */
    public ZIPFileInputMapper(Path mainPath, Charset charset) throws IOException {
        super(mainPath);
        ioMap.put(mainPath, new ZIPEntryInput(mainPath, charset));
    }
}
