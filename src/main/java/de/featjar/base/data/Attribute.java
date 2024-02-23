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
package de.featjar.base.data;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Primary implementation of {@link IAttribute}.
 *
 * @author Elias Kuiter
 */
public class Attribute<T> implements IAttribute<T> {
    public static final String DEFAULT_NAMESPACE = Attribute.class.getPackageName();

    protected final String namespace;
    protected final String name;
    protected final Class<T> type;
    protected BiPredicate<IAttributable, T> validator;
    protected Function<IAttributable, T> defaultValueFunction;
    protected Function<T, T> copyValueFunction = t -> t;

    public Attribute(String namespace, String name, Class<T> type) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    /**
     * {@return this attribute's default value function}
     */
    public Result<Function<IAttributable, T>> getDefaultValueFunction() {
        return Result.ofNullable(defaultValueFunction);
    }

    /**
     * {@return this attribute's copy value function}
     */
    public Result<Function<T, T>> getCopyValueFunction() {
        return Result.ofNullable(copyValueFunction);
    }

    /**
     * Sets this attribute's default value function.
     *
     * @param defaultValueFunction the function
     * @return this attribute
     */
    public Attribute<T> setDefaultValueFunction(Function<IAttributable, T> defaultValueFunction) {
        this.defaultValueFunction = defaultValueFunction;
        return this;
    }

    /**
     * Sets this attribute's copy value function.
     *
     * @param copyValueFunction the function
     * @return this attribute
     */
    public Attribute<T> setCopyValueFunction(Function<T, T> copyValueFunction) {
        this.copyValueFunction = copyValueFunction;
        return this;
    }

    /**
     * {@return this attribute's default value for a given object}
     */
    @Override
    public Result<T> getDefaultValue(IAttributable attributable) {
        return getDefaultValueFunction().map(f -> f.apply(attributable));
    }

    /**
     * Sets this attribute's default value.
     *
     * @param defaultValue the default value
     * @return this attribute
     */
    public Attribute<T> setDefaultValue(T defaultValue) {
        return setDefaultValueFunction(o -> defaultValue);
    }

    /**
     * {@return a copy of this attribute's value for a given {@link IAttributable}}
     */
    @Override
    public Result<T> copyValue(IAttributable attributable) {
        return getCopyValueFunction()
                .flatMap(f -> attributable.getAttributeValue(this).map(o -> f.apply((T) o)));
    }

    /**
     * {@return this attribute's validator}
     */
    @Override
    public BiPredicate<IAttributable, T> getValidator() {
        return Result.ofNullable(validator).orElse(IAttribute.super.getValidator());
    }

    /**
     * Sets this attribute's validator.
     *
     * @param validator the validator
     * @return this attribute
     */
    public Attribute<T> setValidator(BiPredicate<IAttributable, T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Attribute{namespace='%s', name='%s'}", namespace, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute<?> attribute = (Attribute<?>) o;
        return namespace.equals(attribute.namespace) && name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }
}
