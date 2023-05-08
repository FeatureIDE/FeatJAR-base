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
package de.featjar.base.cli;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;

/**
 * A format that parses options from a properties file.
 *
 * @author Elias Kuiter
 */
public class OptionFileFormat implements IFormat<OptionFile> {
    @Override
    public Result<OptionFile> parse(AInputMapper inputMapper) {
        return Result.of(new OptionFile(inputMapper.get().getInputStream()));
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsSerialize() {
        return false;
    }

    @Override
    public String getFileExtension() {
        return "properties";
    }

    @Override
    public String getName() {
        return "Configuration File";
    }
}
