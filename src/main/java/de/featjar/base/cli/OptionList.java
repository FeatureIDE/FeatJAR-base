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

import de.featjar.base.FeatJAR;
import de.featjar.base.FeatJAR.Configuration;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.base.log.Log;
import de.featjar.base.log.Log.Verbosity;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Parses a list of strings.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class OptionList {

    static final String GENERAL_CONFIG_NAME = "general";

    /**
     * Option for setting the logger output.
     */
    static final Option<List<String>> CONFIGURATION_OPTION =
            new ListOption<>("config", Option.StringParser).setDescription("The names of configuration files");

    static final Option<Path> CONFIGURATION_DIR_OPTION =
            new Option<>("config_dir", Option.PathParser).setDescription("The path to the configuration files");

    /**
     * Option for printing usage information.
     */
    static final Option<ICommand> COMMAND_OPTION = new Option<>("command", s -> FeatJAR.extensionPoint(Commands.class)
                    .getMatchingExtension(s)
                    .get())
            .setRequired(true)
            .setDescription("Command to execute");

    /**
     * Option for printing usage information.
     */
    static final Option<Boolean> HELP_OPTION = new Flag("help").setDescription("Print usage information");

    /**
     * Option for printing version information.
     */
    static final Option<Boolean> VERSION_OPTION = new Flag("version").setDescription("Print version information");

    static final Option<Path> INFO_FILE_OPTION =
            new Option<>("info-file", Option.PathParser).setDescription("Path to info log file");

    static final Option<Path> ERROR_FILE_OPTION =
            new Option<>("error-file", Option.PathParser).setDescription("Path to error log file");

    static final Option<List<Log.Verbosity>> LOG_INFO_OPTION = new ListOption<>(
                    "log-info", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the info stream (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS));

    static final Option<List<Log.Verbosity>> LOG_ERROR_OPTION = new ListOption<>(
                    "log-error", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the error stream (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.ERROR));

    static final Option<List<Log.Verbosity>> LOG_INFO_FILE_OPTION = new ListOption<>(
                    "log-info-file", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the info file (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.DEBUG));

    static final Option<List<Log.Verbosity>> LOG_ERROR_FILE_OPTION = new ListOption<>(
                    "log-error-file", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the error file (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.ERROR, Log.Verbosity.WARNING));

    private final List<Option<?>> options = new ArrayList<>(List.of(
            CONFIGURATION_OPTION,
            COMMAND_OPTION,
            HELP_OPTION,
            VERSION_OPTION,
            INFO_FILE_OPTION,
            ERROR_FILE_OPTION,
            LOG_INFO_OPTION,
            LOG_ERROR_OPTION,
            LOG_INFO_FILE_OPTION,
            LOG_ERROR_FILE_OPTION));

    private final List<String> commandLineArguments;
    private final List<String> configFileArguments = new ArrayList<>();
    private LinkedHashMap<String, Object> properties;

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

        Path configDir = Paths.get("");
        int configurationDirIndex = commandLineArguments.indexOf("--" + CONFIGURATION_DIR_OPTION.getName());
        if (configurationDirIndex >= 0) {
            int argumentIndex = configurationDirIndex + 1;
            if (argumentIndex >= commandLineArguments.size()) {
                FeatJAR.log()
                        .error(
                                "option %s is supplied without value, but a value was expected",
                                CONFIGURATION_DIR_OPTION.getName());
                return this;
            }
            parseOption(CONFIGURATION_DIR_OPTION, commandLineArguments.get(argumentIndex));
            Result<Path> configDirValue = getResult(CONFIGURATION_DIR_OPTION);
            if (configDirValue.isEmpty()) {
                FeatJAR.log().problems(configDirValue.getProblems());
                return this;
            }
            configDir = configDirValue.get();
            if (!Files.isDirectory(configDir)) {
                FeatJAR.log().error("given config dir %s is not a directory", configDir.toString());
                return this;
            }
        }

        int configurationNamesIndex = commandLineArguments.indexOf("--" + CONFIGURATION_OPTION.getName());
        if (configurationNamesIndex >= 0) {
            int argumentIndex = configurationNamesIndex + 1;
            if (argumentIndex >= commandLineArguments.size()) {
                FeatJAR.log()
                        .error(
                                "option %s is supplied without value, but a value was expected",
                                CONFIGURATION_OPTION.getName());
                return this;
            }
            parseOption(CONFIGURATION_OPTION, commandLineArguments.get(argumentIndex));
            Result<List<String>> config = getResult(CONFIGURATION_OPTION);
            if (!config.isEmpty()) {
                configFileArguments.clear();

                List<String> configNameList = config.get();
                ArrayList<String> reverseNameList = new ArrayList<>(configNameList.size() + 1);
                reverseNameList.add(GENERAL_CONFIG_NAME);
                reverseNameList.addAll(configNameList);
                Collections.reverse(reverseNameList);

                for (String name : reverseNameList) {
                    Path configPath = configDir.resolve(name + ".properties");
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
        }

        ArrayList<String> arguments = new ArrayList<>(commandLineArguments.size() + configFileArguments.size());
        arguments.addAll(commandLineArguments);
        arguments.addAll(configFileArguments);
        ListIterator<String> listIterator = arguments.listIterator();
        while (listIterator.hasNext()) {
            String argument = listIterator.next();
            if (!argument.matches("--\\w[-\\w]*")) {
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
    public <T> Result<T> getResult(Option<T> option) {
        T optionValue = (T) properties.getOrDefault(option.getName(), option.defaultValue);
        return optionValue != null
                ? Result.of(optionValue)
                : Result.empty(
                        new Problem(String.format("Option <%s> was not set and has no default value", option.name)));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Option<T> option) {
        return Objects.requireNonNull((T) properties.getOrDefault(option.getName(), option.defaultValue));
    }

    /**
     * {@return the commands supplied in this option input}
     */
    public Result<ICommand> getCommand() {
        return getResult(COMMAND_OPTION);
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
                Collections.sort(generalOptions, Comparator.comparing(Option::getName));
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
        final Configuration configuration = FeatJAR.configure();
        getResult(INFO_FILE_OPTION).ifPresent(p -> logToFile(configuration, p, LOG_INFO_FILE_OPTION));
        getResult(ERROR_FILE_OPTION).ifPresent(p -> logToFile(configuration, p, LOG_ERROR_FILE_OPTION));
        configuration.logConfig.logToSystemOut(get(LOG_INFO_OPTION).toArray(new Log.Verbosity[0]));
        configuration.logConfig.logToSystemErr(get(LOG_ERROR_OPTION).toArray(new Log.Verbosity[0]));
        return configuration;
    }

    private void logToFile(Configuration configuration, Path path, Option<List<Verbosity>> verbosities) {
        try {
            configuration.logConfig.logToFile(path, get(verbosities).toArray(new Log.Verbosity[0]));
        } catch (FileNotFoundException e) {
            FeatJAR.log().error(e);
        }
    }

    /**
     * {@return whether this option input requests help information}
     */
    public boolean isHelp() {
        return getResult(HELP_OPTION).orElse(Boolean.FALSE);
    }

    /**
     * {@return whether this option input requests version information}
     */
    public boolean isVersion() {
        return getResult(VERSION_OPTION).orElse(Boolean.FALSE);
    }

    /**
     * {@return whether the given option has a custom value}
     */
    public boolean has(Option<?> opt) {
        return properties.get(opt.getName()) != null;
    }
}
