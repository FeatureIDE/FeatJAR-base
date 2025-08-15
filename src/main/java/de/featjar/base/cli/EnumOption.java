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
package de.featjar.base.cli;

import java.util.Arrays;

/**
 * A Boolean flag option, which can either be present or not.
 *
 * @author Sebastian Krieter
 */
public class EnumOption extends Option<String> {

    private String[] possibleValues;
    /**
     * Creates a flag option.
     *
     * @param name the name of the flag option
     */
    protected EnumOption(String name, String... possibleValues) {
        super(name, StringParser);
        this.possibleValues = Arrays.copyOf(possibleValues, possibleValues.length);
        Arrays.sort(this.possibleValues);
        validator = s -> Arrays.binarySearch(this.possibleValues, s) >= 0;
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s (one of %s)%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                Arrays.toString(possibleValues),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }
}
