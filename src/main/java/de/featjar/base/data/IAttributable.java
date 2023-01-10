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

/**
 * An object that can be annotated with {@link Attribute} values to store additional metadata.
 *
 * @author Elias Kuiter
 */
public interface IAttributable {
    LinkedHashMap<IAttribute, Object> getAttributeValues();

    default Result<Object> getAttributeValue(Attribute attribute) {
        return attribute.apply(this, getAttributeValues());
    }

    default boolean hasAttributeValue(Attribute attribute) {
        return getAttributeValue(attribute).isPresent();
    }

    // todo: this is weird
    static <T extends IAttributable & IMutable<T, ?>> Mutator<T> createMutator(IAttributable attributable) {
        return new Mutator<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T getMutable() {
                return (T) attributable;
            }
        };
    }

    interface Mutator<T extends IAttributable & IMutable<T, ?>> extends IMutator<T> {
        default void setAttributeValue(Attribute attribute, Object value) {
            if (value == null) {
                removeAttributeValue(attribute);
                return;
            }
            if (!attribute.getType().equals(value.getClass())) {
                throw new IllegalArgumentException("cannot set attribute of type " + attribute.getType()
                        + " to value of type " + value.getClass());
            }
            if (!attribute.getValidator().test(getMutable(), value)) {
                throw new IllegalArgumentException("failed to validate attribute " + attribute + " for value " + value);
            }
            getMutable().getAttributeValues().put(attribute, value);
        }

        default <U> Object removeAttributeValue(Attribute attribute) {
            return getMutable().getAttributeValues().remove(attribute);
        }

        default boolean toggleAttributeValue(Attribute attribute) {
            boolean value = (boolean) getMutable().getAttributeValue(attribute).get();
            setAttributeValue(attribute, !value);
            return !value;
        }
    }
}
