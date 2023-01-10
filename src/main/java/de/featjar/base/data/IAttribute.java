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
package de.featjar.base.data;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Describes metadata that can be attached to an object.
 * an {@link IAttribute} does not store any attribute values, it only acts as a key or descriptor.
 *
 * @author Elias Kuiter
 */
public interface IAttribute extends BiFunction<IAttributable, LinkedHashMap<IAttribute, Object>, Result<Object>> {
    String getNamespace();

    String getName();

    Class<?> getType();

    default Result<Object> getDefaultValue(IAttributable attributable) {
        return Result.empty();
    }

    default BiPredicate<IAttributable, Object> getValidator() {
        return (a, o) -> true;
    }

    @Override
    default Result<Object> apply(IAttributable attributable, LinkedHashMap<IAttribute, Object> attributeToValueMap) {
        Result<Object> defaultValue = getDefaultValue(attributable);
        if (defaultValue.isPresent()) return Result.of(attributeToValueMap.getOrDefault(this, defaultValue.get()));
        else return Result.ofNullable(attributeToValueMap.get(this));
    }
}
