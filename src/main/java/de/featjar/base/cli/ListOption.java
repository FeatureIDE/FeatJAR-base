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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A list option, which is parsed as a list of values.
 *
 * @author Elias Kuiter
 * @param <T> the type of the option value
 */
public class ListOption<T> extends Option<List<T>> {
    /**
     * Creates a list option.
     *
     * @param name the name
     * @param parser the parser
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ListOption(String name, Function<String, Result<T>> parser) {
        super(name, s -> (Result)
                Result.mergeAll(Arrays.stream(String.join(",", s.split("\n")).split(","))
                        .map(parser)
                        .collect(Collectors.toList())));
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value,...>%s", name, getDescription().map(d -> ": " + d).orElse(""));
    }
}
