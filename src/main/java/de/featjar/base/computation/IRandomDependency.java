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

import java.util.Random;

/**
 * An analysis that may need to generate pseudorandom numbers.
 * Assumes that the implementing class can be cast to {@link IComputation}.
 *
 * @author Elias Kuiter
 */
public interface IRandomDependency {
    /**
     * The default seed for the pseudorandom number generator returned by {@link #getRandom()}, if not specified otherwise.
     */
    long DEFAULT_RANDOM_SEED = 0;

    /**
     * {@return the random dependency of this computation}
     */
    Dependency<Random> getRandomDependency();

    /**
     * {@return the pseudorandom number generator computation of this computation}
     */
    default IComputation<Random> getRandom() {
        return getRandomDependency().get((IComputation<?>) this);
    }

    /**
     * Sets the pseudorandom number generator computation of this computation.
     *
     * @param random the pseudorandom number generator computation
     */
    default void setRandom(IComputation<Random> random) {
        getRandomDependency().set((IComputation<?>) this, random);
    }
}
