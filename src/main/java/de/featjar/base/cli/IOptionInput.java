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

import de.featjar.base.FeatJAR;
import de.featjar.base.FeatJAR.Configuration;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.base.log.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Parses and validates options.
 *
 * @author Elias Kuiter
 */
public interface IOptionInput {
    /**
     * Option for printing usage information.
     */
    Option<ICommand> COMMAND_OPTION = new Option<>("command", s -> FeatJAR.extensionPoint(Commands.class)
                    .getMatchingExtension(s)
                    .get())
            .setRequired(true)
            .setDescription("Command to execute");

    /**
     * Option for printing usage information.
     */
    Option<Boolean> HELP_OPTION = new Flag("help").setDescription("Print usage information");

    /**
     * Option for printing version information.
     */
    Option<Boolean> VERSION_OPTION = new Flag("version").setDescription("Print version information");

    /**
     * Option for setting the logger verbosity.
     */
    Option<Log.Verbosity> VERBOSITY_OPTION = new Option<>("verbosity", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format("The logger verbosity (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(Commands.DEFAULT_VERBOSITY);

    /**
     * Option for setting the logger output.
     */
    Option<Path> CONFIGURATION_OPTION =
            new Option<>("config", Option.PathParser).setDescription("The path to a configuration file");

    /**
     * {@return a void result when the given options are valid in this option input}
     * In particular, returns an empty result when there are unused options in this
     * option input.
     *
     * @param options the options
     */
    Result<Void> validate(List<Option<?>> options);

    /**
     * {@return the value of the given option in this option input}
     *
     * @param option the option
     * @param <T>    the type of the option value
     */
    <T> Result<T> get(Option<T> option);

    /**
     * {@return the commands supplied in this option input}
     */
    default Result<ICommand> getCommand() {
        return get(COMMAND_OPTION);
    }

    /**
     * {@return the general options of this option input}
     */
    default List<Option<?>> getOptions() {
        return List.of(COMMAND_OPTION, HELP_OPTION, VERSION_OPTION, VERBOSITY_OPTION);
    }

    /**
     * {@return the command-line interface help}
     */
    default String getHelp() {
        IndentStringBuilder sb = new IndentStringBuilder();
        List<ICommand> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        sb.appendLine(String.format(
                "Usage: java -jar %s --command <command> [--<flag> | --<option> <value>]...", FeatJAR.LIBRARY_NAME));
        sb.appendLine();
        if (commands.size() == 0) {
            sb.append(String.format(
                    "No commands are available. You can register commands in an extensions.xml file when building %s.",
                    FeatJAR.LIBRARY_NAME));
            sb.appendLine();
        } else {
            if (getCommand().isEmpty()) {
                sb.append("The following commands are available:").appendLine().addIndent();
                for (final ICommand command : commands) {
                    sb.appendLine(String.format(
                            "%s: %s", //
                            command.getIdentifier(), //
                            Result.ofNullable(command.getDescription()).orElse("")));
                }
            } else {
                final ICommand command = getCommand().get();
                sb.appendLine(String.format("Help for %s", command.getIdentifier()))
                        .addIndent();
                sb.appendLine(String.format(command.getDescription()));

                sb.appendLine();
                sb.appendLine("General options:").addIndent();
                List<Option<?>> generalOptions = new ArrayList<>(getOptions());
                Collections.sort(generalOptions, Comparator.comparing(Option::getArgumentName));
                sb.appendLine(generalOptions).removeIndent();

                List<Option<?>> options = new ArrayList<>(command.getOptions());
                if (!options.isEmpty()) {
                    Collections.sort(options, Comparator.comparing(Option::getArgumentName));
                    sb.appendLine();
                    sb.appendLine(String.format("Options of command %s:", command.getIdentifier()));
                    sb.addIndent();
                    sb.appendLine(options);
                    sb.removeIndent();
                }
            }
        }
        return sb.toString();
    }

    /**
     * {@return whether this option input requests help information}
     */
    default boolean isHelp() {
        return get(HELP_OPTION).get();
    }

    /**
     * {@return whether this option input requests version information}
     */
    default boolean isVersion() {
        return get(VERSION_OPTION).get();
    }

    /**
     * {@return the verbosity supplied in this option input}
     */
    default Log.Verbosity getVerbosity() {
        return get(VERBOSITY_OPTION).get();
    }

    /**
     * {@return a @link Configuration} instance build from the provided options}
     */
    default Result<Configuration> getConfiguration() {
        Result<Path> configPath = get(CONFIGURATION_OPTION);
        if (configPath.isEmpty()) {
            return Result.empty();
        }

        final Configuration configuration = new Configuration();
        final Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(configPath.get()));
            // TODO implement property options for logs and cache
            // for (final Property<?> prop : propertyList) {
            // final String value = properties.getProperty(prop.getKey());
            // if (value != null) {
            // prop.setValue(value);
            // }
            // }
            return Result.of(configuration);
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }
}
