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

import de.featjar.base.extension.IExtension;
import java.util.ArrayList;
import java.util.List;

/**
 * A command run within a {@link CommandLineInterface}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface ICommand extends IExtension {
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
        // todo: automatically try to parse these options (and no other),
        //  which would make calling ArgumentParser#close obsolete
    }

    static List<Option<?>> addOptions(List<Option<?>> options, Option<?>... newOptions) {
        options = new ArrayList<>(options);
        options.addAll(List.of(newOptions));
        return options;
    }

    /**
     * Runs this command with some given arguments.
     *
     * @param argumentParser the argument parser
     */
    void run(ArgumentParser argumentParser);
}
