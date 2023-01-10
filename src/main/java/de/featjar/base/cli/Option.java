/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
import de.featjar.base.data.Result;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    protected Supplier<String> descriptionSupplier = () -> null;
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
        return Result.ofNullable(descriptionSupplier.get());
    }

    /**
     * Sets this option's description.
     *
     * @param description the description
     * @return this option
     */
    public Option<T> setDescription(String description) {
        return setDescription(() -> description);
    }

    /**
     * Sets this option's description supplier.
     * Should be used when the description is complicated or only known after initialization.
     *
     * @param descriptionSupplier the description supplier
     * @return this option
     */
    public Option<T> setDescription(Supplier<String> descriptionSupplier) {
        this.descriptionSupplier = descriptionSupplier;
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
    public Result<T> parseFrom(AArgumentParser argumentParser) throws AArgumentParser.ArgumentParseException {
        Result<String> valueString =
                isRequired ? Result.of(argumentParser.parseRequiredOption(name)) : argumentParser.parseOption(name);
        if (valueString.isEmpty()) return Result.ofNullable(defaultValue);
        Result<T> parseResult = parser.apply(valueString.get());
        if (parseResult.isEmpty()) {
            FeatJAR.log().warning("could not parse option " + name + ", using default value");
            return Result.ofNullable(defaultValue);
        }
        if (validator.test(parseResult.get())) return parseResult;
        throw new IllegalArgumentException("value " + parseResult.get() + " for option " + name + " is invalid");
    }

    /**
     * Parses the value of this option from a given argument parser.
     * Handles exceptions by printing them and exiting.
     *
     * @param argumentParser the argument parser
     * @return the parsed value
     */
    public Result<T> parseFrom(ArgumentParser argumentParser) {
        try {
            return parseFrom((AArgumentParser) argumentParser);
        } catch (AArgumentParser.ArgumentParseException e) {
            argumentParser.handleException(e);
            return Result.empty(e);
        }
    }

    // todo: parse options from configuration files (key=value style)

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s%s",
                name,
                getDescription().map(d -> ": " + d).orElse(""),
                defaultValue != null ? String.format(" (default: %s)", defaultValue) : "");
    }
}
