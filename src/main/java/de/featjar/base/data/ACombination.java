/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.base.data;

import java.util.Arrays;

/**
 * Abstract implementation of {@link ICombination}.
 *
 * @param <T> the type of the element in the combination
 * @param <E> the type of the environment for the combination
 *
 * @author Sebastian Krieter
 */
public abstract class ACombination<E, T> implements ICombination<E, T> {

    /**
     * THe environment object.
     */
    public final E environment;

    /**
     * The indices of the elements.
     */
    public final int[] elementIndices;
    /**
     * The index of current internal combination
     */
    public long combinationIndex;

    /**
     * Creates a new combination instance.
     * @param t the value for t
     * @param environment the environment
     */
    public ACombination(int t, E environment) {
        elementIndices = new int[t];
        this.environment = (environment != null) ? environment : null;
    }

    /**
     * Copy constructor.
     * @param other the combination to copy
     * @param environment the environment
     */
    public ACombination(ACombination<E, T> other, E environment) {
        combinationIndex = other.combinationIndex;
        elementIndices = Arrays.copyOf(other.elementIndices, other.elementIndices.length);
        this.environment = (environment != null) ? environment : null;
    }

    public void reset() {
        combinationIndex = 0;
    }

    @Override
    public String toString() {
        return "Combination [elementIndices=" + Arrays.toString(elementIndices) + ", combinationIndex="
                + combinationIndex + "]";
    }

    @Override
    public E environment() {
        return environment;
    }

    @Override
    public long index() {
        return combinationIndex;
    }

    @Override
    public int[] indexElements() {
        return elementIndices;
    }
}
