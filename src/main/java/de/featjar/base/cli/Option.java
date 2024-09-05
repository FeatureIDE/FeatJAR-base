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
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An option for an {@link ICommand}. Parses a string value into an object.
 * Allows to set a default value.
 *
 * @param <T> the type of the option's value
 * @author Elias Kuiter
 */
public class Option<T> {

    public static final Function<String, Boolean> BooleanParser = Boolean::parseBoolean;
    public static final Function<String, Integer> IntegerParser = Integer::parseInt;
    public static final Function<String, Double> DoubleParser = Double::parseDouble;
    public static final Function<String, Long> LongParser = Long::parseLong;
    public static final Function<String, String> StringParser = s -> s;
    public static final Function<String, Path> PathParser = Path::of;

    public static final Predicate<Path> PathValidator = Files::exists;

    private static List<Pair<Class<?>, Option<?>>> list = new ArrayList<>();

    public static <U> Option<U> newOption(String name, Function<String, U> parser, U defaultValue) {
        Option<U> option = new Option<>(name, parser, defaultValue);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    public static <U> Option<U> newOption(String name, Function<String, U> parser) {
        Option<U> option = new Option<>(name, parser);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    public static <U> ListOption<U> newListOption(String name, Function<String, U> parser) {
        ListOption<U> option = new ListOption<>(name, parser);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    public static <U> RangeOption newRangeOption(String name) {
        RangeOption option = new RangeOption(name);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    public static <U> Flag newFlag(String name) {
        Flag option = new Flag(name);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    public static List<Option<?>> getAllOptions(Class<?> clazz) {
        return list.stream()
                .filter(e -> e.getKey().isAssignableFrom(clazz))
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    public static void deleteAllDependencies() {
        list.clear();
        list = null;
    }

    private static Class<?> getCallingClass() {
        try {
            return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName());
        } catch (ClassNotFoundException e) {
            FeatJAR.log().error(e);
            throw new RuntimeException(e);
        }
    }

    protected final String name;
    protected final Function<String, T> parser;
    protected Supplier<String> descriptionSupplier = () -> null;
    protected boolean isRequired = false;
    protected T defaultValue;
    protected Predicate<T> validator = t -> true;

    public static String possibleValues(Class<?> enumClass) {
        final Object[] enumConstants = enumClass.getEnumConstants();
        return enumConstants == null //
                ? "" //
                : Arrays.stream(enumConstants)
                        .map(Objects::toString)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(", "));
    }

    public static <E extends Enum<E>> Function<String, E> valueOf(Class<E> enumClass) {
        return s -> Enum.valueOf(enumClass, s.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     */
    protected Option(String name, Function<String, T> parser) {
        this.name = name;
        this.parser = parser;
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected Option(String name, Function<String, T> parser, T defaultValue) {
        this.name = name;
        this.parser = parser;
        this.defaultValue = defaultValue;
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
    public Function<String, T> getParser() {
        return parser;
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
     * {@return whether this option is required}
     */
    public boolean isRequired() {
        return isRequired;
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
     * Sets this option's description supplier. Should be used when the description
     * is complicated or only known after initialization.
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

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }

    Result<T> parse(String s) {
        try {
            return Result.of(parser.apply(s));
        } catch (Exception e) {
            return Result.empty(e);
        }
    }
}
