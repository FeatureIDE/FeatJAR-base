package de.featjar.base.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.extension.Extension;
import de.featjar.base.extension.ExtensionPoint;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.base.log.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parses a list of string arguments supplied on the command-line interface.
 * Parses the first argument as a command name and the remaining arguments as flags and options.
 * Parse errors are treated as unrecoverable:
 * Whenever an error occurs, it is printed alongside the correct usage and FeatJAR is exited.
 *
 * @author Elias Kuiter
 */
public class CLIArgumentParser extends ArgumentParser {
    public static final int COMMAND_NAME_POSITION = 0;
    private final String commandName;

    /**
     * Creates a new argument parser for the command-line interface.
     *
     * @param args the arguments to parse
     */
    public CLIArgumentParser(String[] args) {
        super(args);
        commandName = parsePositionalArguments(COMMAND_NAME_POSITION).get(COMMAND_NAME_POSITION);
    }

    /**
     * {@return the command supplied in the given arguments}
     */
    public Command getCommand() {
        return getRequiredExtension(FeatJAR.extensionPoint(Commands.class), commandName);
    }

    /**
     * {@return the verbosity supplied in the given arguments}
     */
    public Log.Verbosity getVerbosity() {
        return Log.Verbosity.of(parseOption("--verbosity").orElse(CommandLineInterface.DEFAULT_MAXIMUM_VERBOSITY));
    }

    /**
     * Appends the command-line interface usage to a string.
     *
     * @param sb the indent string builder
     */
    public void appendUsage(IndentStringBuilder sb) {
        List<Command> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        sb.appendLine("Usage: java -jar feat.jar <command> [--<flag> | --<option> <value>]...").appendLine();
        if (commands.size() == 0) {
            sb.append("No commands are available. You can register commands in an extensions.xml file when building feat.jar.\n");
        }
        sb.append("The following commands are available:\n").addIndent();
        for (final Command command : commands) {
            sb.appendLine(String.format("%s: %s", command.getIdentifier(), Optional.ofNullable(command.getDescription()).orElse("")));
        }
        sb.removeIndent();
        sb.appendLine();
        sb.appendLine("General flags and options:").addIndent();
        sb.appendLine("--verbosity <level>: The logger verbosity. One of:").addIndent();
        Arrays.stream(Log.Verbosity.values()).forEach(verbosity -> sb.appendLine(verbosity.toString().toLowerCase()));
        sb.removeIndent();
        sb.removeIndent();
        if (commandName != null) {
            Result<Command> commandResult = getExtension(FeatJAR.extensionPoint(Commands.class), commandName);
            if (commandResult.isPresent() && commandResult.get().getUsage() != null) {
                sb.appendLine();
                sb.appendLine(String.format("Command %s has following flags and options:", commandResult.get().getIdentifier()));
                sb.addIndent();
                commandResult.get().appendUsage(sb);
                sb.removeIndent();
            }
        }
    }

    /**
     * {@return the command-line interface usage}
     */
    public String getUsage() {
        IndentStringBuilder sb = new IndentStringBuilder();
        appendUsage(sb);
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
        System.err.println(getUsage());
        System.exit(1);
    }

    /**
     * {@return the extension at the given extension point identified by the given identifier, if any}
     *
     * @param extensionPoint the extension point
     * @param identifier the identifier
     * @param <T> the type of the extension
     */
    public <T extends Extension> Result<T> getExtension(ExtensionPoint<T> extensionPoint, String identifier) {
        return extensionPoint.getExtension(identifier);
    }

    /**
     * {@return the extension at the given extension point identified by the given identifier}
     * If the extension cannot be found, prints an error and exits.
     *
     * @param extensionPoint the extension point
     * @param identifier the identifier
     * @param <T> the type of the extension
     */
    public <T extends Extension> T getRequiredExtension(ExtensionPoint<T> extensionPoint, String identifier) {
        Result<T> extensionResult = getExtension(extensionPoint, identifier);
        if (extensionResult.isEmpty())
            handleException(new ArgumentParseException(
                    String.format("Requested extension %s is not available.", identifier)));
        return extensionResult.get();
    }

    @Override
    public Map<Integer, String> parsePositionalArguments(List<Integer> positions) {
        try {
            return super.parsePositionalArguments(positions);
        } catch (ArgumentParseException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Map<Integer, String> parsePositionalArguments(Integer... positions) {
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
    public Optional<String> parseOption(String option) {
        try {
            return super.parseOption(option);
        } catch (ArgumentParseException e) {
            handleException(e);
            return Optional.empty();
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
