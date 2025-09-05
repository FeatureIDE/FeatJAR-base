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
import java.util.function.Supplier;

/**
 * Combination iterator that uses the combinatorial number system to process
 * combinations in parallel.
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
     * Creates a new combination instance
     * @param t the value for t
     * @param environmentCreator the environment supplier
     */
    public ACombination(int t, Supplier<E> environmentCreator) {
        combinationIndex = 0;
        elementIndices = new int[t];
        environment = (environmentCreator != null) ? environmentCreator.get() : null;
    }

    /**
     * Copy constructor.
     * @param other the combination to copy
     * @param environmentCreator the environment supplier
     */
    public ACombination(ACombination<E, T> other, Supplier<E> environmentCreator) {
        combinationIndex = other.combinationIndex;
        elementIndices = Arrays.copyOf(other.elementIndices, other.elementIndices.length);
        environment = (environmentCreator != null) ? environmentCreator.get() : null;
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
