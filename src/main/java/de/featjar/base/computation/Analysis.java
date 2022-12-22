/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.base.computation;

import de.featjar.base.Feat;
import de.featjar.base.data.Result;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

/**
 * asynchronous function
 * Base class for an analysis performed by a {@link Solver solver}.
 * Contains several mixins to control exactly what capabilities a concrete implementation has.
 *
 * @param <T> the type of the (primary) analysis input
 * @param <U> the type of the analysis result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Analysis<T, U> extends Computable<U>, Computable.WithInput<T>, Function<Computable<T>, FutureResult<U>> {
    @Override
    default FutureResult<U> apply(Computable<T> tComputable) {
        setInput(tComputable);
        return get();
    }
}
