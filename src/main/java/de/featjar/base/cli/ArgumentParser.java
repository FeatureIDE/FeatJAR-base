package de.featjar.base.cli;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses a list of string arguments.
 * Adapted from
 * <a href="https://github.com/ekuiter/PCLocator/blob/master/src/de/ovgu/spldev/pclocator/Arguments.java">PCLocator</a>.
 *
 * @author Elias Kuiter
 */
public class ArgumentParser {
    static class ArgumentParseException extends Exception {
        public ArgumentParseException(String message) {
            super(message);
        }
    }

    List<String> arguments;
    List<String> unusedArguments;

    ArgumentParser(String[] args) {
        this(List.of(args));
    }

    ArgumentParser(List<String> arguments) {
        this.arguments = new ArrayList<>(arguments);
        this.unusedArguments = new ArrayList<>(arguments);
    }

    public Map<Integer, String> parsePositionalArguments(List<Integer> positions) throws ArgumentParseException {
        Map<Integer, String> positionalArguments = new HashMap<>();
        for (Integer position : positions) {
            int actualPosition = position;
            int expected = position;
            if (position < 0) {
                actualPosition = arguments.size() + position;
                expected = arguments.size() - actualPosition;
            }
            expected++;
            if (actualPosition < 0 || actualPosition >= arguments.size())
                throw new ArgumentParseException(String.format(
                        "Only %d arguments supplied, but at least %s expected.",
                        arguments.size(), expected == 1 ? "one was" : expected + " were"));
            positionalArguments.put(position, arguments.remove(actualPosition));
            unusedArguments.remove(actualPosition);
        }
        return positionalArguments;
    }

    public Map<Integer, String> parsePositionalArguments(Integer... positions) throws ArgumentParseException {
        return parsePositionalArguments(List.of(positions));
    }

    public void ensureAllowedValue(String option, String value, String... allowedValues) throws ArgumentParseException {
        for (String allowedValue : allowedValues)
            if (value.equals(allowedValue))
                return;
        throw new ArgumentParseException(
                String.format("Value %s supplied for option %s, but one of the following was expected: %s",
                        value, option, String.join(", ", allowedValues)));
    }

    public boolean parseFlag(String flag) throws ArgumentParseException {
        boolean found = false;
        for (int i = 0; i < arguments.size(); i++) {
            String currentArgument = arguments.get(i);
            if (flag.equals(currentArgument) && !found) {
                unusedArguments.set(i, null);
                found = true;
            } else if (flag.equals(currentArgument))
                throw new ArgumentParseException(
                        String.format("Flag %s supplied several times, but may only be supplied once.",
                                flag));
        }
        return found;
    }

    public List<String> parseOptions(String option) throws ArgumentParseException {
        ArrayList<String> values = new ArrayList<>();

        for (int i = 0; i < arguments.size() - 1; i++)
            if (option.equals(arguments.get(i))) {
                unusedArguments.set(i, null);
                unusedArguments.set(i + 1, null);
                values.add(arguments.get(i + 1));
                i++;
            }

        if (!arguments.isEmpty() && arguments.get(arguments.size() - 1).equals(option))
            throw new ArgumentParseException(
                    String.format("Option %s supplied without value, but a value was expected.", option));

        return values;
    }

    public Optional<String> parseOption(String option) throws ArgumentParseException {
        List<String> values = parseOptions(option);
        if (values.size() > 2)
            throw new ArgumentParseException(
                    String.format("Option %s supplied with several values, but only one value was expected.",
                            option));
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    public String parseRequiredOption(String option) throws ArgumentParseException {
        Optional<String> value = parseOption(option);
        if (value.isEmpty())
            throw new ArgumentParseException(
                    String.format("Option %s not supplied, but was expected.", option));
        return value.get();
    }

    public void ensureAllArgumentsUsed() throws ArgumentParseException {
        String unusedString = unusedArguments.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        if (!unusedString.isBlank())
            throw new ArgumentParseException(
                    String.format("Option arguments %s supplied, but could not be recognized.", unusedString));
    }

    public void close() throws ArgumentParseException {
        ensureAllArgumentsUsed();
    }
}
