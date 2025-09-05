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
 * Combination of some items.
 * Can store an arbitrary environment object to provide context.
 *
 * @param <T> the type of the item in the combination
 * @param <E> the type of the environment for the combination
 *
 * @author Sebastian Krieter
 */
public interface ICombination<E, T> {

    /**
     * {@return the current combination of items given as instance of T}
     */
    T select();

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
