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
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.base.log.Log;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Parses a list of strings.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class OptionList {

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
     * Option for printing version information.
     */
    Option<Path> LOG_FILE_OPTION =
            new Option<>("logfile", Option.PathParser).setDescription("Set the output file for the log");

    /**
     * Option for printing version information.
     */
    Option<Path> ERROR_LOG_FILE_OPTION =
            new Option<>("errorfile", Option.PathParser).setDescription("Set the output file for the error log");

    /**
     * Option for setting the logger verbosity.
     */
    Option<Log.Verbosity> VERBOSITY_OPTION = new Option<>("verbosity", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format("The logger verbosity (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(Commands.DEFAULT_VERBOSITY);

    private final List<Option<?>> options = new ArrayList<>(List.of(
            CONFIGURATION_OPTION,
            COMMAND_OPTION,
            HELP_OPTION,
            VERSION_OPTION,
            VERBOSITY_OPTION,
            LOG_FILE_OPTION,
            ERROR_LOG_FILE_OPTION));

    private final List<String> commandLineArguments;
    private final List<String> configFileArguments = new ArrayList<>();
    private LinkedHashMap<String, Object> properties;

    /**
     * Option for setting the logger output.
     */
    public static Option<List<Path>> CONFIGURATION_OPTION =
            new ListOption<>("config", Option.PathParser).setDescription("The path to a configuration file");

    /**
     * Creates a new option list.
     *
     * @param arguments the arguments
     */
    public OptionList(String... arguments) {
        this(List.of(arguments));
    }

    /**
     * Creates a new option list.
     *
     * @param arguments the arguments
     */
    public OptionList(List<String> arguments) {
        this.commandLineArguments = new ArrayList<>(arguments);
    }

    public OptionList parseArguments() {
        return parseArguments(true);
    }

    public OptionList parseArguments(boolean logWarnings) {
        properties = new LinkedHashMap<>();

        int indexOf = commandLineArguments.indexOf("--" + CONFIGURATION_OPTION.getName());
        if (indexOf >= 0) {
            if (commandLineArguments.size() > indexOf + 1) {
                parseOption(CONFIGURATION_OPTION, commandLineArguments.get(indexOf + 1));
                Result<List<Path>> config = get(CONFIGURATION_OPTION);
                if (!config.isEmpty()) {
                    configFileArguments.clear();
                    for (Path configPath : config.get()) {
                        final Properties properties = new Properties();
                        try {
                            properties.load(Files.newInputStream(configPath));
                            for (Entry<Object, Object> propertyEntry : properties.entrySet()) {
                                configFileArguments.add(
                                        "--" + propertyEntry.getKey().toString());
                                configFileArguments.add(propertyEntry.getValue().toString());
                            }
                        } catch (final IOException e) {
                            FeatJAR.log().error(e);
                            return this;
                        }
                    }
                }
            } else {
                FeatJAR.log()
                        .error(
                                "option %s is supplied without value, but a value was expected",
                                CONFIGURATION_OPTION.getName());
                return this;
            }
        }

        ArrayList<String> arguments = new ArrayList<>(commandLineArguments.size() + configFileArguments.size());
        arguments.addAll(commandLineArguments);
        arguments.addAll(configFileArguments);
        ListIterator<String> listIterator = arguments.listIterator();
        while (listIterator.hasNext()) {
            String argument = listIterator.next();
            if (!argument.matches("--\\w+")) {
                if (logWarnings) FeatJAR.log().warning("ignoring unrecognized argument %s", argument);
                continue;
            }

            String optionName = argument.substring(2);
            Optional<Option<?>> optionalOption =
                    options.stream().filter(o -> o.getName().equals(optionName)).findFirst();
            if (!optionalOption.isPresent()) {
                if (logWarnings) FeatJAR.log().warning("ignoring unrecognized option %s", argument);
                continue;
            }

            Option<?> option = optionalOption.get();
            if (option.equals(CONFIGURATION_OPTION)) {
                listIterator.next();
                continue;
            }

            if (properties.containsKey(optionName)) {
                if (logWarnings) FeatJAR.log().warning("ignoring multiple occurences of argument %s", optionName);
                if (!(option instanceof Flag) && listIterator.hasNext()) {
                    listIterator.next();
                }
                continue;
            }

            if (option instanceof Flag) {
                properties.put(optionName, Boolean.TRUE);
            } else {
                if (!listIterator.hasNext()) {
                    FeatJAR.log()
                            .error(
                                    "option %s is supplied without value, but a value was expected, using default value (%s)",
                                    option.getName(), String.valueOf(option.defaultValue));
                    return this;
                }
                String nextArgument = listIterator.next();
                if (nextArgument.matches("--\\w+")) {
                    listIterator.previous();
                    FeatJAR.log()
                            .error(
                                    "option %s is supplied without value, but a value was expected, using default value (%s)",
                                    option.getName(), String.valueOf(option.defaultValue));
                    return this;
                }
                parseOption(option, nextArgument);
            }
        }
        return this;
    }

    private <T> void parseOption(Option<T> option, String nextArgument) {
        Result<T> parseResult = option.parse(nextArgument);
        if (parseResult.isEmpty()) {
            FeatJAR.log().problems(parseResult.getProblems());
            FeatJAR.log()
                    .error(
                            "could not parse argument %s for option %s, using default value (%s)",
                            nextArgument, option.getName(), String.valueOf(option.defaultValue));
            return;
        }

        if (!option.validator.test(parseResult.get())) {
            FeatJAR.log()
                    .error(
                            "invalid argument %s for option %s, using default value (%s)",
                            nextArgument, option.getName(), String.valueOf(option.defaultValue));
            return;
        }
        properties.put(option.getName(), parseResult.get());
    }

    @SuppressWarnings("unchecked")
    public <T> Result<T> get(Option<T> option) {
        return Result.ofNullable((T) properties.getOrDefault(option.getName(), option.defaultValue));
    }

    /**
     * {@return the commands supplied in this option input}
     */
    public Result<ICommand> getCommand() {
        return get(COMMAND_OPTION);
    }

    /**
     * {@return the general options of this option input}
     */
    public List<Option<?>> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /**
     * Add options to this list to allow parsing arguments.
     *
     * @param options the options to add
     */
    public OptionList addOptions(List<Option<?>> options) {
        this.options.addAll(options);
        return this;
    }

    /**
     * {@return the command-line interface help}
     */
    public String getHelp() {
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
     * {@return a @link Configuration} instance build from the provided options}
     */
    public Configuration getConfiguration() {
        final Configuration configuration = FeatJAR.createDefaultConfiguration();
        try {
            configuration.logConfig.logAtMost(
                    get(VERBOSITY_OPTION).get(),
                    get(LOG_FILE_OPTION).orElse(null),
                    get(ERROR_LOG_FILE_OPTION).orElse(null));
        } catch (FileNotFoundException e) {
            FeatJAR.log().error(e);
            configuration.logConfig.logAtMost(get(VERBOSITY_OPTION).get());
        }
        return configuration;
    }

    /**
     * {@return whether this option input requests help information}
     */
    public boolean isHelp() {
        return get(HELP_OPTION).orElse(Boolean.FALSE);
    }

    /**
     * {@return whether this option input requests version information}
     */
    public boolean isVersion() {
        return get(VERSION_OPTION).orElse(Boolean.FALSE);
    }

    /**
     * {@return the verbosity supplied in this option input}
     */
    public Log.Verbosity getVerbosity() {
        return get(VERBOSITY_OPTION).get();
    }
}
