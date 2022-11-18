/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.cli;

import de.featjar.base.extension.Extension;
import de.featjar.base.log.IndentStringBuilder;

/**
 * A command run within a {@link CommandLine}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Command extends Extension {
    /**
     * {@return this command's description, if any}
     */
    default String getDescription() {
        return null;
    }

    /**
     * {@return appends this command's usage to a string, if any}
     *
     * @param sb the indent string builder
     */
    default boolean appendUsage(IndentStringBuilder sb) {
        return false;
    }

    /**
     * {@return this command's usage, if any}
     */
    default String getUsage() {
        IndentStringBuilder sb = new IndentStringBuilder();
        if (!appendUsage(sb))
            return null;
        return sb.toString();
    }

    /**
     * Runs this command with some given arguments.
     *
     * @param argumentParser the argument parser
     */
    void run(CLIArgumentParser argumentParser);
}
