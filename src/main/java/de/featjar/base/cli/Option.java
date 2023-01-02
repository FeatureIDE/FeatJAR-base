package de.featjar.base.cli;

import de.featjar.base.Feat;
import de.featjar.base.data.Result;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An option on the command-line interface.
 * Parses a string value supplied on the command line into an object.
 * Allows to set a default value.
 *
 * @param <T> the type of the option's value
 * @author Elias Kuiter
 */
public class Option<T> {
    protected final String name;
    protected final Function<String, Result<T>> parser;
    protected String description;
    protected boolean isRequired = false;
    protected T defaultValue;
    protected Predicate<T> validator = t -> true;

    /**
     * Creates an option.
     *
     * @param name the name of the option
     * @param parser the parser for the option's value
     */
    public Option(String name, Function<String, Result<T>> parser) {
        this.name = name;
        this.parser = parser;
    }

    /**
     * {@return this option's name}
     */
    public String getName() {
        return name;
    }

    /**
     * {@return this option's parser}
     */
    public Function<String, Result<T>> getParser() {
        return parser;
    }

    /**
     * {@return whether this option is required}
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Sets whether this option is required.
     *
     * @param required whether this option is required
     * @return this option
     */
    public Option<T> setRequired(boolean required) {
        isRequired = required;
        return this;
    }

    /**
     * {@return this option's description}
     */
    public Result<String> getDescription() {
        return Result.ofNullable(description);
    }

    /**
     * Sets this option's description.
     *
     * @param description the description
     * @return this option
     */
    public Option<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * {@return this option's default value}
     */
    public Result<T> getDefaultValue() {
        return Result.ofNullable(defaultValue);
    }

    /**
     * Sets this option's default value.
     *
     * @param defaultValue the default value
     * @return this option
     */
    public Option<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * {@return this option's validator}
     */
    public Predicate<T> getValidator() {
        return validator;
    }

    /**
     * Sets this option's validator.
     *
     * @param validator the validator
     * @return this option
     */
    public Option<T> setValidator(Predicate<T> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Parses the value of this option from a given argument parser.
     *
     * @param argumentParser the argument parser
     * @return the parsed value
     * @throws AArgumentParser.ArgumentParseException when the value cannot be parsed
     */
    public T parseFrom(AArgumentParser argumentParser) throws AArgumentParser.ArgumentParseException {
        Result<String> valueString = isRequired
                ? Result.of(argumentParser.parseRequiredOption(name))
                : argumentParser.parseOption(name);
        if (valueString.isEmpty())
            return defaultValue;
        Result<T> parseResult = parser.apply(valueString.get());
        if (parseResult.isEmpty()) {
            Feat.log().warning("could not parse option " + name + ", using default value");
            return defaultValue;
        }
        if (validator.test(parseResult.get()))
            return parseResult.get();
        throw new IllegalArgumentException("value " + parseResult.get() + " for option " + name + " is invalid");
    }

    /**
     * Parses the value of this option from a given argument parser.
     * Handles exceptions by printing them and exiting.
     *
     * @param argumentParser the argument parser
     * @return the parsed value
     */
    public T parseFrom(ArgumentParser argumentParser) {
        try {
            return parseFrom((AArgumentParser) argumentParser);
        } catch (AArgumentParser.ArgumentParseException e) {
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

}
