/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of model.
 *
 * model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-model> for further information.
 */
package de.featjar.base.data;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * An attribute describes metadata that can be attached to an object.
 * This class does not store any attribute values, it acts as a key or descriptor.
 *
 * @author Elias Kuiter
 * @deprecated planned to be used for formula and feature-model analysis
 */
@Deprecated
public class Attribute implements Function<LinkedHashMap<Attribute, Object>, Result<Object>> {
    public static final String DEFAULT_NAMESPACE = Attribute.class.getCanonicalName();

    protected final String namespace;
    protected final String name;
    protected final Class<?> type;

    protected final BiPredicate<IAttributable, Object> valueValidator;

    public Attribute(String namespace, String name, Class<?> type) {
        this(namespace, name, type, (a, o) -> true);
    }

    public Attribute(String namespace, String name, Class<?> type, BiPredicate<IAttributable, Object> valueValidator) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        Objects.requireNonNull(valueValidator);
        this.namespace = namespace;
        this.name = name;
        this.type = type;
        this.valueValidator = valueValidator;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public BiPredicate<IAttributable, Object> getValueValidator() {
        return valueValidator;
    }

    @Override
    public Result<Object> apply(LinkedHashMap<Attribute, Object> attributeToValueMap) {
        return Result.ofNullable(attributeToValueMap.get(this));
    }

    @Override
    public String toString() {
        return String.format("Attribute{namespace='%s', name='%s'}", namespace, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return namespace.equals(attribute.namespace) && name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    public static class WithDefaultValue extends Attribute {
        protected final Function<IAttributable, Object> defaultValueFunction;

        public WithDefaultValue(
                String namespace, String name, Class<?> type, BiPredicate<IAttributable, Object> valueValidator, Function<IAttributable, Object> defaultValueFunction) {
            super(namespace, name, type, valueValidator);
            Objects.requireNonNull(defaultValueFunction);
            this.defaultValueFunction = defaultValueFunction;
        }

        public WithDefaultValue(
                String namespace, String name, Class<?> type, Function<IAttributable, Object> defaultValueFunction) {
            super(namespace, name, type);
            Objects.requireNonNull(defaultValueFunction);
            this.defaultValueFunction = defaultValueFunction;
        }

        public WithDefaultValue(String namespace, String name, Class<?> type, Object defaultValue) {
            this(namespace, name, type, attributable -> defaultValue);
            Objects.requireNonNull(defaultValue);
        }

        public Function<IAttributable, Object> getDefaultValueFunction() {
            return defaultValueFunction;
        }

        public Object getDefaultValue(IAttributable attributable) {
            return defaultValueFunction.apply(attributable);
        }

        public Object applyWithDefaultValue(LinkedHashMap<Attribute, Object> attributeToValueMap, IAttributable attributable) {
            return attributeToValueMap.getOrDefault(this, defaultValueFunction.apply(attributable));
        }
    }
}
