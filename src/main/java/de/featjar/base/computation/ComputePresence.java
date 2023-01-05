/*
 * Copyright (C) 2022 Sebastian Krieter
 *
 * This file is part of formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;

import java.util.List;
import java.util.function.Function;

/**
 * Computes whether the supplied input is present.
 *
 * @param <T> the type of the input
 * @author Elias Kuiter
 */
public class ComputePresence<T> extends AComputation<Boolean> implements IInputDependency<T> {
    protected static Dependency<?> INPUT = newRequiredDependency();

    public ComputePresence(IComputation<T> input) {
        dependOn(INPUT);
        setInput(input);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dependency<T> getInputDependency() {
        return (Dependency<T>) INPUT;
    }

    @Override
    public Function<List<? extends Result<?>>, Result<List<?>>> getResultMerger() {
        return Result::mergeAllNullable;
    }

    @Override
    public Result<Boolean> computeResult(List<?> results, Progress progress) {
        return Result.of(INPUT.get(results) != null);
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputePresence<>(getInput());
    }
}
