package de.featjar.base.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.extension.IExtension;
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.base.log.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses a list of string arguments supplied on the command-line interface.
 * Parses the first argument as a command name and the remaining arguments as options.
 * Parse errors are treated as unrecoverable:
 * Whenever an error occurs, it is printed alongside the correct usage and the process is exited.
 *
 * @author Elias Kuiter
 */
public class ArgumentParser extends AArgumentParser {
    /**
     * Option for printing usage information.
     */
    public static final Option<Boolean> HELP_OPTION =
            new Flag("--help")
                    .setDescription("Print usage information");

    /**
     * Option for printing version information.
     */
    public static final Option<Boolean> VERSION_OPTION =
            new Flag("--version")
                    .setDescription("Print version information");

    /**
     * Option for setting the logger verbosity.
     */
    public static final Option<Log.Verbosity> VERBOSITY_OPTION =
            new Option<>("--verbosity", Log.Verbosity::of)
                    .setDescription("The logger verbosity, one of none, " + // todo: make none an explicit value
                            Arrays.stream(Log.Verbosity.values())
                                    .map(Objects::toString)
                                    .map(String::toLowerCase)
                                    .collect(Collectors.joining(", ")))
                    .setDefaultValue(CommandLineInterface.DEFAULT_MAXIMUM_VERBOSITY);

    protected static final int COMMAND_NAME_POSITION = 0;
    protected final String commandNameRegex;

    /**
     * Creates a new argument parser for the command-line interface.
     *
     * @param args the arguments to parse
     */
    public ArgumentParser(String[] args) {
        super(args);
        if (args.length > 0)
            commandNameRegex = parsePositionalArguments(COMMAND_NAME_POSITION).get(COMMAND_NAME_POSITION);
        else
            commandNameRegex = null;
    }

    /**
     * {@return the commands supplied in the given arguments}
     */
    public LinkedHashSet<ICommand> getCommands() {
        return commandNameRegex != null
                ? getMatchingCommands(commandNameRegex)
                : Sets.empty();
    }

    /**
     * {@return the verbosity supplied in the given arguments}
     */
    public Log.Verbosity getVerbosity() {
        return VERBOSITY_OPTION.parseFrom(this);
    }

    /**
     * {@return the general options of the command-line interface}
     */
    public List<Option<?>> getOptions() {
        return List.of(HELP_OPTION, VERSION_OPTION, VERBOSITY_OPTION);
    }

    /**
     * Appends the command-line interface help to a string.
     *
     * @param sb the indent string builder
     */
    public void appendHelp(IndentStringBuilder sb) {
        List<ICommand> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        sb.appendLine("Usage: java -jar " + FeatJAR.LIBRARY_NAME + " <command> [--<flag> | --<option> <value>]...").appendLine();
        if (commands.size() == 0) {
            sb.append("No commands are available. You can register commands in an extensions.xml file when building " + FeatJAR.LIBRARY_NAME + ".\n");
        }
        sb.append("The following commands are available:\n").addIndent();
        for (final ICommand command : commands) {
            sb.appendLine(String.format("%s: %s", command.getIdentifier(), Result.ofNullable(command.getDescription()).orElse("")));
        }
        sb.removeIndent();
        sb.appendLine();
        sb.appendLine("General options:").addIndent();
        sb.appendLine(getOptions());
        sb.removeIndent();
        if (commandNameRegex != null) {
            for (ICommand command : getMatchingCommands(commandNameRegex)) {
                if (!command.getOptions().isEmpty()) {
                    sb.appendLine();
                    sb.appendLine(String.format("Options of command %s:", command.getIdentifier()));
                    sb.addIndent();
                    sb.appendLine(command.getOptions());
                    sb.removeIndent();
                }
            }
        }
    }

    /**
     * {@return the command-line interface help}
     */
    public String getHelp() {
        IndentStringBuilder sb = new IndentStringBuilder();
        appendHelp(sb);
        return sb.toString();
    }

    /**
     * Handles argument parse exceptions by printing them and exiting.
     *
     * @param argumentParseException the argument parse exception
     */
    protected void handleException(ArgumentParseException argumentParseException) {
        System.err.println("Invalid usage: " + argumentParseException.getMessage());
        System.err.println();
        System.err.println(getHelp());
        System.exit(1);
    }

    /**
     * {@return the extensions at the given extension point matching the given regular expression}
     * If no extension cannot be found, prints an error and exits.
     *
     * @param extensionPoint the extension point
     * @param regex          the regex
     * @param <T>            the type of the extension
     */
    public <T extends IExtension> LinkedHashSet<T> getMatchingExtensions(AExtensionPoint<T> extensionPoint, String regex) {
        return extensionPoint.getMatchingExtensions(regex);
    }

    /**
     * {@return the commands matching the given regular expression}
     * If no command cannot be found, prints an error and exits.
     *
     * @param regex the regex
     */
    public LinkedHashSet<ICommand> getMatchingCommands(String regex) {
        return getMatchingExtensions(FeatJAR.extensionPoint(Commands.class), regex);
    }

    @Override
    public LinkedHashMap<Integer, String> parsePositionalArguments(List<Integer> positions) {
        try {
            return super.parsePositionalArguments(positions);
        } catch (ArgumentParseException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public LinkedHashMap<Integer, String> parsePositionalArguments(Integer... positions) {
        try {
            return super.parsePositionalArguments(positions);
        } catch (ArgumentParseException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void ensureAllowedValue(String option, String value, String... allowedValues) {
        try {
            super.ensureAllowedValue(option, value, allowedValues);
        } catch (ArgumentParseException e) {
            handleException(e);
        }
    }

    @Override
    public boolean parseFlag(String flag) {
        try {
            return super.parseFlag(flag);
        } catch (ArgumentParseException e) {
            handleException(e);
            return false;
        }
    }

    @Override
    public List<String> parseOptions(String option) {
        try {
            return super.parseOptions(option);
        } catch (ArgumentParseException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Result<String> parseOption(String option) {
        try {
            return super.parseOption(option);
        } catch (ArgumentParseException e) {
            handleException(e);
            return Result.empty();
        }
    }

    @Override
    public String parseRequiredOption(String option) {
        try {
            return super.parseRequiredOption(option);
        } catch (ArgumentParseException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public void ensureAllArgumentsUsed() {
        try {
            super.ensureAllArgumentsUsed();
        } catch (ArgumentParseException e) {
            handleException(e);
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (ArgumentParseException e) {
            handleException(e);
        }
    }
}
