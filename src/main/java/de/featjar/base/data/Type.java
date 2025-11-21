/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

/**
 * Represents a type.
 *
 * @param <T> the type class
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class Type<T> {

    protected final Class<T> type;

    /**
     * Constructs a new type.
     * @param type the class object of the type
     */
    public Type(Class<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    public Class<T> getClassType() {
        return type;
    }

    public abstract T copy(T value);

    public abstract T parse(String text);

    public abstract String serialize(T value);

    @Override
    public String toString() {
        return String.format("Type{%s}", type.getTypeName());
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
