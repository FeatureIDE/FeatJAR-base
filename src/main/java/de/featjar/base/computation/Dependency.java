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

import static de.featjar.base.computation.Computations.async;

import de.featjar.base.tree.structure.ATree;
import java.util.List;
import java.util.Objects;

/**
 * A dependency of a computation.
 * Describes the dependency without storing its actual value, which is passed in a {@link DependencyList} to {@link IComputation#compute(DependencyList, Progress)}.
 *
 * @param <U> the type of the dependency's computation result
 * @author Elias Kuiter
 */
public class Dependency<U> extends ATree.Entry<IComputation<?>, IComputation<U>> { // todo: U extends Serializable?
    /**
     * Creates a new required dependency.
     */
    public Dependency() {}

    /**
     * Creates a new optional dependency with a given default value.
     *
     * @param defaultValue the default value
     */
    public Dependency(U defaultValue) {
        super(async(Objects.requireNonNull(defaultValue, "default value must not be null")));
    }

    @SuppressWarnings("unchecked")
    public U get(List<?> list) {
        if (index < 0 || index >= list.size()) throw new IllegalArgumentException();
        return (U) list.get(index);
    }
}
