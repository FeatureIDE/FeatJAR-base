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
import de.featjar.base.data.Result;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An option for an {@link ICommand}.
 * Parses a string value into an object.
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
     * {@return this option's argument name on the command-line interface}
     */
    public String getArgumentName() {
        return "--" + name;
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
     * Parses the value of this option from a given option list.
     *
     * @param optionList the option list
     * @return the parsed value
     */
    public Result<T> parseFrom(OptionList optionList) {
        Result<String> valueString = isRequired
                ? optionList.parseRequiredOption(getArgumentName())
                : optionList.parseOption(getArgumentName());
        if (valueString.isEmpty()) return valueString.merge(Result.ofNullable(defaultValue));
        Result<T> parseResult = parser.apply(valueString.get());
        if (parseResult.isEmpty()) {
            FeatJAR.log().warning("could not parse option " + getArgumentName() + ", using default value");
            return Result.ofNullable(defaultValue);
        }
        if (validator.test(parseResult.get())) return parseResult;
        throw new IllegalArgumentException(
                "value " + parseResult.get() + " for option " + getArgumentName() + " is invalid");
    }

    /**
     * Parses the value of this option from a given option file.
     *
     * @param optionFile the option file
     * @return the parsed value
     */
    public Result<T> parseFrom(OptionFile optionFile) {
        String value = optionFile.getProperties().getProperty(name);
        if (value == null) return new OptionList().get(this);
        return new OptionList(getArgumentName(), value).get(this);
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                defaultValue != null ? String.format(" (default: %s)", defaultValue) : "");
    }
}
