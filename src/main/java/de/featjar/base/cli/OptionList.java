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
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.base.log.Log;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.log.TimeStampFormatter;
import de.featjar.base.log.VerbosityFormatter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

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
            Option.newListOption("config", Option.StringParser).setDescription("The names of configuration files");

    static final Option<Path> CONFIGURATION_DIR_OPTION =
            Option.newOption("config_dir", Option.PathParser).setDescription("The path to the configuration files");

    /**
     * Option for printing usage information.
     */
    static final Option<ICommand> COMMAND_OPTION = Option.newOption(
                    "command", s -> FeatJAR.extensionPoint(Commands.class)
                            .getMatchingExtension(s)
                            .orElseThrow())
            .setRequired(true)
            .setDescription("Classpath from command to execute");

    /**
     * Option for printing usage information.
     */
    static final Option<Boolean> HELP_OPTION = Option.newFlag("help").setDescription("Print usage information");

    /**
     * Option for printing version information.
     */
    static final Option<Boolean> VERSION_OPTION = Option.newFlag("version").setDescription("Print version information");

    /**
     * Option for printing version information.
     */
    static final Option<Boolean> STACKTRACE_OPTION =
            Option.newFlag("print-stacktrace").setDescription("Print a stacktrace for all logged exceptions");

    /**
     * Option for printing version information.
     */
    static final Option<Boolean> QUIET_OPTION = Option.newFlag("quiet")
            .setDescription("Suppress all unnecessary output. (Overwrites --log-info and --log-error options)");

    static final Option<Path> INFO_FILE_OPTION =
            Option.newOption("info-file", Option.PathParser).setDescription("Path to info log file");

    static final Option<Path> ERROR_FILE_OPTION =
            Option.newOption("error-file", Option.PathParser).setDescription("Path to error log file");

    static final Option<List<Log.Verbosity>> LOG_INFO_OPTION = Option.newListOption(
                    "log-info", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the info stream (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS));

    static final Option<List<Log.Verbosity>> LOG_ERROR_OPTION = Option.newListOption(
                    "log-error", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the error stream (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.ERROR));

    static final Option<List<Log.Verbosity>> LOG_INFO_FILE_OPTION = Option.newListOption(
                    "log-info-file", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the info file (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.DEBUG));

    static final Option<List<Log.Verbosity>> LOG_ERROR_FILE_OPTION = Option.newListOption(
                    "log-error-file", Option.valueOf(Log.Verbosity.class))
            .setDescription(String.format(
                    "Message types printed to the error file (%s)", Option.possibleValues(Log.Verbosity.class)))
            .setDefaultValue(List.of(Log.Verbosity.ERROR, Log.Verbosity.WARNING));

    private final List<Option<?>> options = new ArrayList<>(Option.getAllOptions(getClass()));

    private final List<String> commandLineArguments, configFileArguments;

    private LinkedList<String> arguments;
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
        configFileArguments = new ArrayList<>();
    }

    public List<Problem> parseArguments() {
        properties = new LinkedHashMap<>();
        arguments = new LinkedList<>();
        List<Problem> problemList = new ArrayList<>();

        parseCommand(problemList);
        parseConfigurationFiles(problemList);

        arguments.addAll(commandLineArguments);
        arguments.addAll(configFileArguments);

        parseRemainingArguments(problemList, false);

        return problemList;
    }

    public List<Problem> parseRemainingArguments() {
        List<Problem> problemList = new ArrayList<>();

        parseRemainingArguments(problemList, true);

        return problemList;
    }

    private void parseCommand(List<Problem> problemList) {
        if (!commandLineArguments.isEmpty() && !commandLineArguments.get(0).startsWith("--")) {
            String commandString = commandLineArguments.get(0);
            Commands commandsExentionsPoint = FeatJAR.extensionPoint(Commands.class);
            List<ICommand> commands = commandsExentionsPoint.getExtensions().stream()
                    .filter(command -> command.getShortName()
                            .map(name -> Objects.equals(name, commandString))
                            .orElse(Boolean.FALSE))
                    .collect(Collectors.toList());

            if (commands.size() > 1) {
                addProblem(
                        problemList,
                        Severity.ERROR,
                        "Command name '%s' is ambiguous! It matches the following commands: \n%s",
                        commandString,
                        commands.stream().map(ICommand::getIdentifier).collect(Collectors.joining("\n")));
                return;
            }

            final ICommand command;
            if (commands.isEmpty()) {
                Result<ICommand> matchingExtension = commandsExentionsPoint.getMatchingExtension(commandString);
                if (matchingExtension.isEmpty()) {
                    problemList.addAll(matchingExtension.getProblems());
                    addProblem(problemList, Severity.ERROR, "No command matched the name '%s'!", commandString);
                    return;
                }
                command = matchingExtension.get();
            } else {
                command = commands.get(0);
            }
            commandLineArguments.remove(0);
            properties.put(COMMAND_OPTION.getName(), command);
        }
    }

    private void parseConfigurationFiles(List<Problem> problemList) {
        final Path configDir;
        int configurationDirIndex = commandLineArguments.indexOf("--" + CONFIGURATION_DIR_OPTION.getName());
        if (configurationDirIndex < 0) {
            configDir = Path.of("");
        } else {
            int argumentIndex = configurationDirIndex + 1;
            if (argumentIndex >= commandLineArguments.size()) {
                addProblem(
                        problemList,
                        Severity.ERROR,
                        "Option %s is supplied without value, but a value is required",
                        CONFIGURATION_DIR_OPTION.getName());
                return;
            }
            parseOption(CONFIGURATION_DIR_OPTION, commandLineArguments.get(argumentIndex), problemList);
            Result<Path> configDirValue = getResult(CONFIGURATION_DIR_OPTION);
            if (configDirValue.isEmpty()) {
                problemList.addAll(configDirValue.getProblems());
                return;
            }
            commandLineArguments
                    .subList(configurationDirIndex, configurationDirIndex + 2)
                    .clear();
            configDir = configDirValue.get();
            if (!Files.isDirectory(configDir)) {
                addProblem(
                        problemList,
                        Severity.ERROR,
                        "Specified configuration directory %s is not a directory",
                        configDir.toString());
                return;
            }
        }

        int configurationNamesIndex = commandLineArguments.indexOf("--" + CONFIGURATION_OPTION.getName());
        if (configurationNamesIndex >= 0) {
            int argumentIndex = configurationNamesIndex + 1;
            if (argumentIndex >= commandLineArguments.size()) {
                addProblem(
                        problemList,
                        Severity.ERROR,
                        "Option %s is supplied without value, but a value is required",
                        CONFIGURATION_OPTION.getName());
                return;
            }
            parseOption(CONFIGURATION_OPTION, commandLineArguments.get(argumentIndex), problemList);
            Result<List<String>> config = getResult(CONFIGURATION_OPTION);
            if (config.isEmpty()) {
                problemList.addAll(config.getProblems());
                return;
            }
            commandLineArguments
                    .subList(configurationNamesIndex, configurationNamesIndex + 2)
                    .clear();
            configFileArguments.clear();

            List<String> configNameList = config.get();
            ArrayList<String> reverseNameList = new ArrayList<>(configNameList.size() + 1);
            reverseNameList.add(GENERAL_CONFIG_NAME);
            reverseNameList.addAll(configNameList);
            Collections.reverse(reverseNameList);

            for (String name : reverseNameList) {
                Path configPath = configDir.resolve(name + ".properties");
                final Properties properties = new Properties();
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                } catch (IOException e) {
                    addProblem(
                            problemList, Severity.ERROR, "Could not load configuration file %s", configPath.toString());
                    continue;
                }
                try {
                    for (Entry<Object, Object> propertyEntry : properties.entrySet()) {
                        configFileArguments.add("--" + propertyEntry.getKey().toString());
                        configFileArguments.add(propertyEntry.getValue().toString());
                    }
                } catch (final Exception e) {
                    problemList.add(new Problem(e));
                    continue;
                }
            }
        }
    }

    private void parseRemainingArguments(List<Problem> problemList, boolean logUnrecognized) {
        ListIterator<String> listIterator = arguments.listIterator();
        while (listIterator.hasNext()) {
            String argument = listIterator.next();
            if (!argument.matches("--\\w[-\\w]*")) {
                if (logUnrecognized)
                    addProblem(problemList, Severity.WARNING, "Ignoring unrecognized argument %s", argument);
                continue;
            }

            String optionName = argument.substring(2);
            Optional<Option<?>> optionalOption =
                    options.stream().filter(o -> o.getName().equals(optionName)).findFirst();
            if (!optionalOption.isPresent()) {
                if (logUnrecognized)
                    addProblem(problemList, Severity.WARNING, "Ignoring unrecognized option %s", argument);
                continue;
            }

            Option<?> option = optionalOption.get();
            if (option.equals(CONFIGURATION_OPTION)) {
                listIterator.remove();
                listIterator.next();
                listIterator.remove();
                continue;
            }

            if (properties.containsKey(optionName)) {
                addProblem(problemList, Severity.WARNING, "Ignoring multiple occurences of argument %s", optionName);
                listIterator.remove();
                if (listIterator.hasNext()) {
                    String next = listIterator.next();
                    if (option instanceof Flag) {
                        if (option.parse(next).isPresent()) {
                            listIterator.remove();
                        } else {
                            listIterator.previous();
                        }
                    } else {
                        listIterator.remove();
                    }
                }
                continue;
            }

            if (option instanceof Flag) {
                listIterator.remove();
                if (listIterator.hasNext()) {
                    Result<Boolean> parse = ((Flag) option).parse(listIterator.next());
                    if (parse.isPresent()) {
                        properties.put(optionName, parse.get());
                        listIterator.remove();
                    } else {
                        properties.put(optionName, Boolean.TRUE);
                        listIterator.previous();
                    }
                } else {
                    properties.put(optionName, Boolean.TRUE);
                }
                continue;
            }

            listIterator.remove();
            if (!listIterator.hasNext()) {
                addProblem(
                        problemList,
                        Severity.WARNING,
                        "Option %s is supplied without value, but a value is required, using default value (%s)",
                        option.getName(),
                        String.valueOf(option.defaultValue));
                continue;
            }
            String nextArgument = listIterator.next();
            if (nextArgument.matches("--\\w+")) {
                listIterator.previous();
                addProblem(
                        problemList,
                        Severity.WARNING,
                        "Option %s is supplied without value, but a value is required, using default value (%s)",
                        option.getName(),
                        String.valueOf(option.defaultValue));
                continue;
            }
            listIterator.remove();
            parseOption(option, nextArgument, problemList);
        }
    }

    private boolean addProblem(List<Problem> problemList, Severity severity, String message, Object... arguments) {
        return problemList.add(new Problem(String.format(message, arguments), severity));
    }

    private <T> void parseOption(Option<T> option, String nextArgument, List<Problem> problemList) {
        Result<T> parseResult = option.parse(nextArgument);
        if (parseResult.isEmpty()) {
            problemList.addAll(parseResult.getProblems());
            addProblem(
                    problemList,
                    Severity.WARNING,
                    "Could not parse argument %s for option %s, using default value (%s)",
                    nextArgument,
                    option.getName(),
                    String.valueOf(option.defaultValue));
            return;
        }

        if (!option.validator.test(parseResult.get())) {
            addProblem(
                    problemList,
                    Severity.WARNING,
                    "Invalid argument %s for option %s, using default value (%s)",
                    nextArgument,
                    option.getName(),
                    String.valueOf(option.defaultValue));
            return;
        }
        properties.put(option.getName(), parseResult.get());
    }

    @SuppressWarnings("unchecked")
    public <T> Result<T> getResult(Option<T> option) {
        T optionValue = (T) properties.getOrDefault(option.getName(), option.defaultValue);
        return optionValue != null
                ? Result.of(optionValue)
                : Result.empty(new IllegalArgumentException(
                        String.format("Argument <%s> is required, but was not set", option.name)));
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
     * {@return the general command-line interface help}
     *
     * @see #getHelp(ICommand)
     */
    public static String getHelp() {
        return getHelp(null);
    }

    /**
     * {@return the command-line interface help}
     * @param command print options specific to this command
     */
    public static String getHelp(ICommand command) {
        IndentStringBuilder sb = new IndentStringBuilder();
        printGeneralOptions(sb);

        List<ICommand> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        sb.appendLine();
        if (commands.isEmpty()) {
            printNoCommandsAvailable(sb);
        } else {
            if (command == null) {
                printAvailableCommands(sb, commands);
            } else {
                printCommandHelp(sb, command);
            }
        }
        return sb.toString();
    }

    private static void printNoCommandsAvailable(IndentStringBuilder sb) {
        sb.append(String.format(
                "No commands are available. You can register commands in an extensions.xml file when building %s.",
                FeatJAR.LIBRARY_NAME));
        sb.appendLine();
    }

    private static void printGeneralOptions(IndentStringBuilder sb) {
        sb.appendLine(String.format(
                "Usage: java -jar %s [<command> | --command <classpath>] [--<flag> | --<option> <value>]...",
                FeatJAR.LIBRARY_NAME));
        sb.appendLine();
        sb.appendLine("General options:").addIndent();
        sb.appendLine(Option.getAllOptions(OptionList.class)).removeIndent();
    }

    private static void printCommandHelp(IndentStringBuilder sb, ICommand command) {
        sb.appendLine(String.format("Help for %s", command.getIdentifier())).addIndent();
        sb.appendLine(command.getDescription().orElse(""));

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

    private static void printAvailableCommands(IndentStringBuilder sb, List<ICommand> commands) {
        sb.append("The following commands are available:").appendLine().addIndent();
        ArrayList<ICommand> commandList = new ArrayList<>(commands);
        Collections.sort(commandList, Comparator.comparing(c -> c.getShortName().orElse("") + c.getIdentifier()));
        for (final ICommand c : commandList) {
            sb.appendLine(String.format(
                            "%s: %s", //
                            c.getShortName().orElse(c.getIdentifier()), //
                            c.getDescription().orElse("")))
                    .addIndent()
                    .appendLine(String.format("(Classpath: %s)", c.getIdentifier()))
                    .removeIndent();
        }
    }

    /**
     * {@return a @link Configuration} instance build from the provided options}
     */
    public Configuration getConfiguration() {
        final Configuration configuration = FeatJAR.configure();
        getResult(INFO_FILE_OPTION).ifPresent(p -> logToFile(configuration, p, LOG_INFO_FILE_OPTION));
        getResult(ERROR_FILE_OPTION).ifPresent(p -> logToFile(configuration, p, LOG_ERROR_FILE_OPTION));
        if (get(QUIET_OPTION)) {
            configuration.logConfig.logToSystemOut(Log.Verbosity.MESSAGE);
        } else {
            configuration
                    .logConfig
                    .logToSystemOut(get(LOG_INFO_OPTION).toArray(new Log.Verbosity[0]))
                    .logToSystemErr(get(LOG_ERROR_OPTION).toArray(new Log.Verbosity[0]))
                    .setPrintStacktrace(get(STACKTRACE_OPTION))
                    .addFormatter(new TimeStampFormatter())
                    .addFormatter(new VerbosityFormatter());
        }
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
