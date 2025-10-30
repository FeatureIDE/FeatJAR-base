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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.featjar.base.FeatJAR;

public class ClearShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, List<String> cmdParams) {
        String choice = Shell.readCommand("Clearing the entire session. Proceed ? (y)es (n)o")
                .orElse("")
                .toLowerCase()
                .trim();

        if (Objects.equals("y", choice)) {
            session.clear();
            FeatJAR.log().message("Clearing successful");
        } else if (Objects.equals("n", choice)) {
            FeatJAR.log().message("Clearing aborted");
        }
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("clear");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("delete the entire session");
    }
}
