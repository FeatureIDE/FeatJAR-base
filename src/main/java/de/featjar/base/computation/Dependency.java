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
package de.featjar.base.computation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dependency of a computation. Describes the dependency without storing its
 * actual value, which is passed in a {@link DependencyList} to
 * {@link IComputation#compute(DependencyList, Progress)}.
 *
 * @param <U> the type of the dependency's computation result
 * @author Elias Kuiter
 */
public class Dependency<U> {

    private static Map<Class<?>, Integer> map = new HashMap<>();

    public static Dependency<Object> newDependency(Class<?> clazz) {
        return newDependency(clazz, Object.class);
    }

    public static <U> Dependency<U> newDependency(Class<?> clazz, Class<U> type) {
        final int count = getDependencyCount(clazz);
        map.put(clazz, count + 1);
        return new Dependency<>(type, count);
    }

    public static void deleteAllDependencies() {
        map.clear();
        map = null;
    }

    public static int getDependencyCount(Class<?> clazz) {
        final Integer integer = map.get(clazz);
        if (integer != null) {
            return integer;
        } else {
            final Class<?> p = clazz.getSuperclass();
            final int curIndex = (p == null) ? 0 : getDependencyCount(p);
            map.put(clazz, curIndex);
            return curIndex;
        }
    }

    private final Class<U> type;
    private final int index;

    private Dependency(Class<U> type, int index) {
        this.type = type;
        this.index = index;
    }

    public Class<U> getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public U getValue(List<?> values) {
        return type.cast(values.get(index));
    }

    public U get(List<?> values) {
        return getValue(values);
    }
}
