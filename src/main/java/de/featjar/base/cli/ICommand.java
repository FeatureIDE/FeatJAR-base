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

import de.featjar.base.extension.IExtension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A command run within a {@link Commands}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface ICommand extends IExtension {
    /**
     * Input option for loading files.
     */
    Option<Path> INPUT_OPTION = new Option<>("input", Option.PathParser)
            .setDescription("Path to input file(s)")
            .setValidator(Option.PathValidator);

    /**
     * Output option for saving files.
     */
    Option<Path> OUTPUT_OPTION = new Option<>("output", Option.PathParser).setDescription("Path to output file(s)");

    /**
     * {@return this command's description, if any}
     */
    default String getDescription() {
        return null;
    }

    /**
     * {@return this command's options}
     */
    default List<Option<?>> getOptions() {
        return new ArrayList<>();
    }

    /**
     * {@return adds new options to a given list of options}
     *
     * @param options the options
     * @param newOptions the new options
     */
    static List<Option<?>> addOptions(List<Option<?>> options, Option<?>... newOptions) {
        options = new ArrayList<>(options);
        options.addAll(List.of(newOptions));
        return options;
    }

    /**
     * Runs this command with some given options.
     *
     * @param optionParser the option parser
     */
    void run(OptionList optionParser);
}
