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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.Commands;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.data.Result;

public class RunShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, List<String> cmdParams) {
        if (cmdParams.isEmpty()) {
            FeatJAR.log().info(String.format("Usage: %s", getDescription().orElse("")));
            return;
        }
        try {
            Result<ICommand> cliCommand = Commands.getInstance().getExtension(cmdParams.get(0));

            if (cliCommand.isEmpty()) {
                FeatJAR.log().error(String.format("Command '%s' not found", cmdParams.get(0)));
                return;
            }
            OptionList shellOptions = cliCommand.get().getShellOptions(session, cmdParams.subList(1, cmdParams.size()));

            shellOptions = alterOptions(cliCommand, shellOptions);

            int runResult = cliCommand.get().run(shellOptions);

            if (runResult == 0) {
                FeatJAR.log().message("Successfull");
            } else {
                FeatJAR.log()
                        .error(
                                "Errorcode '%d' occured in command '%s'",
                                runResult,
                                cliCommand.get().getIdentifier());
            }

        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            FeatJAR.log().error(iae.getMessage());
            FeatJAR.log().info(String.format("Usage %s", getDescription().get()));
        }
    }

    private OptionList alterOptions(Result<ICommand> cliCommand, OptionList shellOptions) {
        String choice;

        while (true) {
            AtomicInteger i = new AtomicInteger(1);
            List<Option<?>> options = cliCommand.get().getOptions();
            int numberChoice;

            options.forEach(o -> {
                FeatJAR.log()
                        .message(i.getAndIncrement() + ". " + o + "="
                                + shellOptions.getResult(o).map(String::valueOf).orElse(""));
            });
            choice = String.valueOf(Shell.readCommand("Alter options ?\nSelect a number or leave blank to proceed:\n")
                            .orElse(""))
                    .toLowerCase();

            if (choice.isBlank()) {
                break;
            }
            try {
                numberChoice = Integer.parseInt(choice) - 1;
            } catch (NumberFormatException e) {
                FeatJAR.log().error("Only decimal numbers are a valid choice");
                continue;
            }

            if (options.size() - 1 < numberChoice || numberChoice < 1) {
                FeatJAR.log().error("Number does not exist");
                continue;
            }

            choice = String.valueOf(Shell.readCommand("Enter the new value:\n").orElse(""));
            shellOptions.parseProperties(options.get(numberChoice), choice);
        }
        return shellOptions;
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("run");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("<path var> <cmd> - launch non shellcommands");
    }
}
