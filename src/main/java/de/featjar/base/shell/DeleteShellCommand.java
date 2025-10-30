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
package de.featjar.base.shell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.featjar.base.FeatJAR;

public class DeleteShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, List<String> cmdParams) {

        if (cmdParams.isEmpty()) {
            session.printVariables();
            cmdParams = Shell.readCommand("Enter the variable names you want to delete or leave blank to abort:")
                    .map(c -> Arrays.stream(c.split("\\s+")).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        }

        cmdParams.forEach(e -> {
            session.remove(e)
                    .ifPresentOrElse(a -> FeatJAR.log().message("Removing of " + e + " successful"), () -> FeatJAR.log()
                            .error("Could not find a variable named " + e));
        });
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("delete");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("- <name> ... - delete session variables");
    }
}
