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

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;

/**
 * A computation that computes two computations.
 * If any dependency fails to compute, the entire computation fails.
 *
 * @author Elias Kuiter
 */
public class ComputePair<T, U> extends AComputation<Pair<T, U>> {
    protected static Dependency<?> KEY_COMPUTATION = newRequiredDependency();
    protected static Dependency<?> VALUE_COMPUTATION = newRequiredDependency();

    public ComputePair(IComputation<T> key, IComputation<U> value) {
        dependOn(KEY_COMPUTATION, VALUE_COMPUTATION);
        setKeyComputation(key);
        setValueComputation(value);
    }

    @SuppressWarnings("unchecked")
    public IComputation<T> getKeyComputation() {
        return getDependency((Dependency<T>) KEY_COMPUTATION);
    }

    @SuppressWarnings("unchecked")
    public void setKeyComputation(IComputation<T> key) {
        setDependency((Dependency<T>) KEY_COMPUTATION, key);
    }

    @SuppressWarnings("unchecked")
    public IComputation<U> getValueComputation() {
        return getDependency((Dependency<U>) VALUE_COMPUTATION);
    }

    @SuppressWarnings("unchecked")
    public void setValueComputation(IComputation<U> value) {
        setDependency((Dependency<U>) VALUE_COMPUTATION, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<Pair<T, U>> compute(DependencyList dependencyList, Progress progress) {
        return Result.of(
                new Pair<>((T) dependencyList.get(KEY_COMPUTATION), (U) dependencyList.get(VALUE_COMPUTATION)));
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputePair<>(getKeyComputation(), getValueComputation());
    }
}
