package de.featjar.base.cli;

import de.featjar.base.data.Result;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * TODO
 *
 * @param <T>
 * @author Elias Kuiter
 */
public class Option<T> {
    protected final String name;
    protected final Function<String, Result<T>> parser;
    protected String description;
    protected boolean isRequired = false;
    protected T defaultValue;
    protected Predicate<T> validator = t -> true;

    public Option(String name, Function<String, Result<T>> parser) {
        this.name = name;
        this.parser = parser;
    }

    public String getName() {
        return name;
    }

    public Function<String, Result<T>> getParser() {
        return parser;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public Option<T> setRequired(boolean required) {
        isRequired = required;
        return this;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Option<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public Optional<T> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public Option<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Predicate<T> getValidator() {
        return validator;
    }

    public Option<T> setValidator(Predicate<T> validator) {
        this.validator = validator;
        return this;
    }

    public T parseFrom(ArgumentParser argumentParser) throws ArgumentParser.ArgumentParseException {
        Optional<String> valueString = isRequired
                ? Optional.of(argumentParser.parseRequiredOption(name))
                : argumentParser.parseOption(name);
        return Result.ofOptional(valueString)
                .flatMap(parser)
                .flatMap(v -> {
                    if (validator.test(v))
                        return Result.of(v);
                    throw new IllegalArgumentException("value " + v + " for option " + name + " is invalid");
                })
                .orElse(defaultValue);
    }

    public T parseFrom(CLIArgumentParser argumentParser) {
        try {
            return parseFrom((ArgumentParser) argumentParser);
        } catch (ArgumentParser.ArgumentParseException e) {
            argumentParser.handleException(e);
            return null;
        }
    }

    // todo: parse from configuration file

    @Override
    public String toString() {
        return String.format("%s <value>: %s%s", name, description,
                defaultValue != null
                        ? String.format(" (default: %s)", defaultValue)
                        : "");
    }

    public static class StringOption extends Option<String> {
        public StringOption(String name) {
            super(name, Result.wrapInResult(String::valueOf));
        }
    }

    public static class Flag extends Option<Boolean> {
        public Flag(String name) {
            super(name, null);
        }

        @Override
        public Boolean parseFrom(ArgumentParser argumentParser) throws ArgumentParser.ArgumentParseException {
            boolean value = argumentParser.parseFlag(name);
            if (defaultValue != null || isRequired)
                throw new IllegalArgumentException();
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", name, description);
        }
    }
}
