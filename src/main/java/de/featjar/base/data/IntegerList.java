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
package de.featjar.base.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;



/**
 * An unordered list of integers.
 * Subclasses implement specific interpretations of these integers (e.g., as an index into a {@link RangeMap}).
 * Negative and zero integers are allowed.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class IntegerList implements IIntegerList {
    protected final int[] array;
    protected boolean hashCodeValid;
    protected int hashCode;

    /**
     * Creates a new integer list from a given array of integers.
     * To ensure performance, the array is not copied, so it must not be modified.
     *
     * @param array the array
     */
    public IntegerList(int... array) {
        this.array = array;
    }

    /**
     * Creates a new integer list from a given collection of integers.
     *
     * @param collection the collection
     */
    public IntegerList(Collection<Integer> collection) {
        this(collection.stream().mapToInt(Integer::intValue).toArray());
    }

    /**
     * Creates a new integer list by copying a given integer list.
     *
     * @param integerList the integer list
     */
    public IntegerList(IntegerList integerList) {
        array = Arrays.copyOf(integerList.array, integerList.array.length);
        hashCodeValid = integerList.hashCodeValid;
        hashCode = integerList.hashCode;
    }

    public static IntegerList merge(Collection<IIntegerList> integerLists) {
        return IIntegerList.merge(integerLists, IntegerList::new);
    }

    @Override
    public int[] get() {
        return array;
    }

    @Override
    public int hashCode() {
        if (hashCodeValid)
            return hashCode;
        hashCode = Arrays.hashCode(array);
        hashCodeValid = true;
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        return Arrays.equals(array, ((IntegerList) obj).array);
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
