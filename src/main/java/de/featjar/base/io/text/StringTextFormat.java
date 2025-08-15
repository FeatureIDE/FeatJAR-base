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
package de.featjar.base.io.text;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;

/**
 * Parses and serializes a string from/to a text.
 *
 * @author Sebastian Krieter
 */
public class StringTextFormat implements IFormat<String> {

    @Override
    public String getName() {
        return "String";
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> parse(AInputMapper inputMapper) {
        return Result.of(inputMapper.get().text());
    }

    @Override
    public Result<String> serialize(String object) {
        return Result.of(object);
    }
}
