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

import java.util.function.Function;

/**
 *
 * @author Sebastian Krieter
 */
public class GenericType<T> extends Type<T> {

    private Function<T, T> copyValueFunction = t -> t;
    private Function<T, String> serializeValueFunction = String::valueOf;
    private Function<String, T> parseValueFunction = s -> {
        throw new UnsupportedOperationException();
    };

    public GenericType(Class<T> type) {
        super(type);
    }

    /**
     * {@return the copy value function}
     */
    public Result<Function<T, T>> getCopyValueFunction() {
        return Result.ofNullable(copyValueFunction);
    }

    /**
     * Sets the copy value function.
     *
     * @param copyValueFunction the function
     * @return this attribute
     */
    public GenericType<T> setCopyValueFunction(Function<T, T> copyValueFunction) {
        this.copyValueFunction = copyValueFunction;
        return this;
    }

    public Function<T, String> getSerializeValueFunction() {
        return serializeValueFunction;
    }

    public void setSerializeValueFunction(Function<T, String> serializeValueFunction) {
        this.serializeValueFunction = serializeValueFunction;
    }

    public Function<String, T> getParseValueFunction() {
        return parseValueFunction;
    }

    public void setParseValueFunction(Function<String, T> parseValueFunction) {
        this.parseValueFunction = parseValueFunction;
    }

    public String toString() {
        return String.format("GenericType{%s}", type.getTypeName());
    }

    @Override
    public T copy(T value) {
        return copyValueFunction.apply(value);
    }

    @Override
    public T parse(String text) {
        return parseValueFunction.apply(text);
    }

    @Override
    public String serialize(T value) {
        return serializeValueFunction.apply(value);
    }
}
