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

import java.util.Map;
import java.util.Optional;

/**
 * An object that can be annotated with {@link Attribute} values to store additional metadata.
 *
 * @author Elias Kuiter
 * @deprecated planned to be used for formula and feature-model analysis
 */
@Deprecated
public interface Attributable {
    Map<Attribute, Object> getAttributeToValueMap();

    default Optional<Object> getAttributeValue(Attribute attribute) {
        return attribute.apply(getAttributeToValueMap());
    }

    default Object getAttributeValue(Attribute.WithDefaultValue attribute) {
        return attribute.applyWithDefaultValue(getAttributeToValueMap(), this);
    }

    default boolean hasAttributeValue(Attribute attribute) {
        return getAttributeValue(attribute).isPresent();
    }

    interface Mutator<T extends Attributable> extends de.featjar.base.data.Mutator<T> {
        default void setAttributeValue(Attribute attribute, Object value) {
            if (value == null) {
                removeAttributeValue(attribute);
                return;
            }
            if (!attribute.getType().equals(value.getClass())) {
                throw new IllegalArgumentException("cannot set attribute of type " + attribute.getType()
                        + " to value of type " + value.getClass());
            }
            if (!attribute.getValueValidator().test(getMutable(), value)) {
                throw new IllegalArgumentException("failed to validate attribute " + attribute + " for value "+ value);
            }
            getMutable().getAttributeToValueMap().put(attribute, value);
        }

        default <U> Object removeAttributeValue(Attribute attribute) {
            return getMutable().getAttributeToValueMap().remove(attribute);
        }

        default boolean toggleAttributeValue(Attribute.WithDefaultValue attribute) {
            boolean value = (boolean) getMutable().getAttributeValue(attribute);
            setAttributeValue(attribute, !value);
            return !value;
        }
    }
}
