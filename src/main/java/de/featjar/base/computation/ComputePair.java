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
package de.featjar.base.computation;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import java.util.List;

/**
 * A computation that computes two computations.
 * If any dependency fails to compute, the entire computation fails.
 *
 * @author Elias Kuiter
 */
public class ComputePair<T, U> extends AComputation<Pair<T, U>> {
    protected static final Dependency<?> KEY_COMPUTATION = Dependency.newDependency();
    protected static final Dependency<?> VALUE_COMPUTATION = Dependency.newDependency();

    public ComputePair(IComputation<T> key, IComputation<U> value) {
        super(key, value);
    }

    protected ComputePair(ComputePair<T, U> other) {
        super(other);
    }

    @SuppressWarnings("unchecked")
    public IComputation<T> getKeyComputation() {
        return getDependencyComputation((Dependency<T>) KEY_COMPUTATION).get();
    }

    @SuppressWarnings("unchecked")
    public void setKeyComputation(IComputation<T> key) {
        setDependencyComputation((Dependency<T>) KEY_COMPUTATION, key);
    }

    @SuppressWarnings("unchecked")
    public IComputation<U> getValueComputation() {
        return getDependencyComputation((Dependency<U>) VALUE_COMPUTATION).get();
    }

    @SuppressWarnings("unchecked")
    public void setValueComputation(IComputation<U> value) {
        setDependencyComputation((Dependency<U>) VALUE_COMPUTATION, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<Pair<T, U>> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(new Pair<>(
                (T) KEY_COMPUTATION.getValue(dependencyList), (U) VALUE_COMPUTATION.getValue(dependencyList)));
    }
}
