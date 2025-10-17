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

/**
 * Combination tuple of some items. Used by combination iterators (e.g. {@link SingleLexicographicIterator}).
 * Can store an arbitrary environment object to provide context to the class using the combination.
 *
 * @param <T> the type of items in the combination
 * @param <E> the type of the environment for the combination
 *
 * @author Sebastian Krieter
 */
public interface ICombination<E, T> {

    /**
     * Resets this combination.
     */
    void reset();

    /**
     * {@return the current combination of items given as instance of T}
     * Uses the internal selection array.
     * <b>Must not be changed by the calling class.
     * Does change after additional calls to {@link #advance()}.</b>
     */
    T select();

    /**
     * {@return a subset of the items that corresponds to the current combination}
     * Does not change the internal selection array, but creates a new one.
     */
    T createSelection();

    /**
     * Moves to the next combination.
     * @return {@code true} if there was a next combination and {@code false} otherwise
     */
    boolean advance();

    /**
     * {@return the environment object}
     */
    E environment();

    /**
     * {@return the current combination index}
     */
    long index();

    /**
     * {@return the maximum combination index}
     */
    long maxIndex();

    /**
     * Set the combination index to the given index and moves to the corresponding combination.
     * @param index the new index
     */
    void setIndex(long index);

    /**
     * {@return the current combination as indices of the items}
     */
    int[] indexElements();
}
