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

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses a list of strings.
 *
 * @author Elias Kuiter
 */
public class OptionList implements IOptionInput {
    protected List<String> arguments;
    protected List<String> unusedArguments;

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
        this.arguments = new ArrayList<>(arguments);
        this.unusedArguments = new ArrayList<>(arguments);
    }

    /**
     * {@return whether a given flag (i.e., a Boolean option) was supplied}
     *
     * @param flag the flag
     */
    public Result<Boolean> parseFlag(String flag) {
        boolean found = false;
        for (int i = 0; i < arguments.size(); i++) {
            String currentArgument = arguments.get(i);
            if (flag.equals(currentArgument) && !found) {
                unusedArguments.set(i, null);
                found = true;
            } else if (flag.equals(currentArgument))
                return Result.empty(new Problem(
                        String.format("Flag %s supplied several times, but may only be supplied once.", flag)));
        }
        return Result.of(found);
    }

    /**
     * {@return the value supplied for a given option, if any}
     *
     * @param option the option
     */
    public Result<String> parseOption(String option) {
        ArrayList<String> values = new ArrayList<>();
        for (int i = 0; i < arguments.size() - 1; i++)
            if (option.equals(arguments.get(i))) {
                unusedArguments.set(i, null);
                unusedArguments.set(i + 1, null);
                values.add(arguments.get(i + 1).trim());
                i++;
            }
        if (!arguments.isEmpty() && arguments.get(arguments.size() - 1).equals(option))
            return Result.empty(
                    new Problem(String.format("Option %s supplied without value, but a value was expected.", option)));
        if (values.size() > 2)
            return Result.of(
                    values.get(values.size() - 1),
                    new Problem(String.format(
                            "Option %s supplied with several values, but only one value was expected.", option)));
        return values.isEmpty() ? Result.empty() : Result.of(values.get(0));
    }

    /**
     * {@return the value supplied for a given option}
     *
     * @param option the option
     */
    public Result<String> parseRequiredOption(String option) {
        Result<String> value = parseOption(option);
        if (value.isEmpty())
            return value.merge(
                    Result.empty(new Problem(String.format("Option %s not supplied, but was expected.", option))));
        return value;
    }

    /**
     * Ensures that a given option has one of the given values.
     *
     * @param option        the option
     * @param value         the value
     * @param allowedValues the allowed values
     */
    public Result<Void> ensureAllowedValue(String option, String value, String... allowedValues) {
        for (String allowedValue : allowedValues) if (value.equals(allowedValue)) return Result.ofVoid();
        return Result.empty(new Problem(String.format(
                "Value %s supplied for option %s, but one of the following was expected: %s",
                value, option, String.join(", ", allowedValues))));
    }

    /**
     * Ensures that all given options have been parsed.
     *
     */
    public Result<Void> ensureAllArgumentsUsed() {
        String unusedString = unusedArguments.stream().filter(Objects::nonNull).collect(Collectors.joining(" "));
        if (!unusedString.isBlank())
            return Result.empty(
                    new Problem(String.format("Arguments %s supplied, but could not be recognized.", unusedString)));
        return Result.ofVoid();
    }

    @Override
    public Result<Void> validate(List<Option<?>> options) {
        options.forEach(this::get);
        return ensureAllArgumentsUsed();
    }

    @Override
    public <T> Result<T> get(Option<T> option) {
        return option.parseFrom(this);
    }
}
