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

import de.featjar.base.data.Result;
import java.util.function.Function;

/**
 * A computation that analyzes its input.
 * Can be used to compute a long-running {@link java.util.function.Function}.
 *
 * @param <T> the type of the input
 * @param <U> the type of the result
 * @author Elias Kuiter
 */
public interface IAnalysis<T, U> extends IComputation<U>, IInputDependency<T>, Function<IComputation<T>, Result<U>> {
    @Override
    default Result<U> apply(IComputation<T> tComputation) {
        setInput(tComputation);
        return get();
    }
}
