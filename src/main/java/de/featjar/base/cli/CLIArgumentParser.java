package de.featjar.base.cli;

import de.featjar.base.Feat;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.extension.Extension;
import de.featjar.base.extension.ExtensionPoint;
import de.featjar.base.log.IndentStringBuilder;

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

    CLIArgumentParser(String[] args) {
        super(args);
        Feat.log().debug("parsing command");
        commandName = parsePositionalArguments(COMMAND_NAME_POSITION).get(COMMAND_NAME_POSITION);
    }

    public Command getCommand() {
        return parseRequiredExtension(FeatJAR.extensionPoint(Commands.class), commandName);
    }

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
        if (commandName != null) {
            Result<Command> commandResult = parseExtension(FeatJAR.extensionPoint(Commands.class), commandName);
            if (commandResult.isPresent() && commandResult.get().getUsage() != null) {
                sb.appendLine(String.format("\nCommand %s has following flags and options:", commandResult.get().getIdentifier()));
                sb.addIndent();
                commandResult.get().appendUsage(sb);
                sb.removeIndent();
            }
        }
    }

    public String getUsage() {
        IndentStringBuilder sb = new IndentStringBuilder();
        appendUsage(sb);
        return sb.toString();
    }

    protected void handleException(ArgumentParseException e) {
        System.err.println("Invalid usage: " + e.getMessage());
        System.err.println();
        System.err.println(getUsage());
        System.exit(1);
    }

    public <T extends Extension> Result<T> parseExtension(ExtensionPoint<T> extensionPoint, String identifier) {
        return extensionPoint.getExtension(identifier);
    }

    public <T extends Extension> T parseRequiredExtension(ExtensionPoint<T> extensionPoint, String identifier) {
        Result<T> extensionResult = parseExtension(extensionPoint, identifier);
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
    public List<String> parseValues(String option) {
        try {
            return super.parseValues(option);
        } catch (ArgumentParseException e) {
            handleException(e);
            return null;
        }
    }

    @Override
    public Optional<String> parseValue(String option) {
        try {
            return super.parseValue(option);
        } catch (ArgumentParseException e) {
            handleException(e);
            return Optional.empty();
        }
    }

    @Override
    public String parseRequiredValue(String option) {
        try {
            return super.parseRequiredValue(option);
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
