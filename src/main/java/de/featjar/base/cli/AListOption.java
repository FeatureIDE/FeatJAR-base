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
package de.featjar.base.cli;

import java.util.List;
import java.util.function.Function;

/**
 * A list option, which is parsed as a list of values.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 * @param <T> the type of the option value
 */
public abstract class AListOption<T> extends Option<List<T>> {

    /**
     * Creates a list option with an empty default list.
     *
     * @param name the name
     * @param parser the parser
     */
    protected AListOption(String name, Function<String, List<T>> parser) {
        this(name, parser, List.of());
    }
    /**
     * Creates a list option with a single-element default list.
     *
     * @param name the name
     * @param parser the parser
     * @param defaultValue the value for the default list
     */
    protected AListOption(String name, Function<String, List<T>> parser, T defaultValue) {
        this(name, parser, List.of(defaultValue));
    }

    /**
     * Creates a list option with a given default list.
     *
     * @param name the name
     * @param parser the parser
     * @param defaultValue a default list
     */
    protected AListOption(String name, Function<String, List<T>> parser, List<T> defaultValue) {
        super(name, parser, defaultValue);
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value1,value2,...>%s%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }
}
