/*
 * Copyright (C) 2023 FeatJAR-Development-Team
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
import de.featjar.base.data.Void;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Parses options from a file.
 *
 * @author Elias Kuiter
 */
public class OptionFile implements IOptionInput {
    Properties properties;
    Properties unusedProperties;

    /**
     * Creates an option file.
     *
     * @param inputStream the input stream
     */
    public OptionFile(InputStream inputStream) {
        this.properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.unusedProperties = (Properties) properties.clone();
    }

    /**
     * {@return the properties of this option file}
     */
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Result<Void> validate(List<Option<?>> options) {
        options.forEach(this::get);
        return unusedProperties.size() == 0 ? Result.ofVoid() : Result.empty();
    }

    @Override
    public <T> Result<T> get(Option<T> option) {
        unusedProperties.remove(option.getName());
        return option.parseFrom(this);
    }
}
