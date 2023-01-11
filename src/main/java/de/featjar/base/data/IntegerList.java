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
public class IntegerList implements Supplier<int[]> {
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

    public static IntegerList merge(Collection<IntegerList> integerLists) {
        return merge(integerLists, IntegerList::new);
    }

    public static <T> T merge(Collection<IntegerList> integerLists, Function<int[], T> integerListFactory) {
        return integerListFactory.apply(integerLists.stream()
                .flatMapToInt(l -> Arrays.stream(l.get()))
                .distinct()
                .toArray());
    }

    /**
     * {@return this integer list's integers}
     * The returned array must not be modified.
     */
    public int[] get() {
        return array;
    }

    /**
     * {@return the value at the given index of this integer list}
     * To ensure performance, no {@link Result} is created, so the index should be checked for validity beforehand.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException when the index is invalid
     */
    public int get(int index) {
        return array[index];
    }

    /**
     * {@return a copy of this integer list's integers}
     * The returned array may be modified.
     */
    public int[] copy() {
        return copyOfRange(0, array.length);
    }

    /**
     * {@return a copy of this integer list's integers in a given range}
     * The returned array may be modified.
     *
     * @param range the range
     */
    public int[] copyOfRange(Range range) {
        return copyOfRange(range.getLowerBound().orElse(0), range.getUpperBound().orElse(array.length));
    }

    /**
     * {@return a copy of this integer list's integers in a given range}
     * The returned array may be modified.
     *
     * @param start the start index
     * @param end the end index
     */
    public int[] copyOfRange(int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * {@return the absolute values of this integer list's integers}
     * The returned array may be modified.
     */
    public int[] getAbsoluteValues() {
        return Arrays.stream(array).map(Math::abs).toArray();
    }

    /**
     * {@return the positive values in this integer list's integers}
     * The returned array may be modified.
     */
    public int[] getPositiveValues() {
        final int countPositive = countPositives();
        int[] positiveIntegers = new int[countPositive];
        int i = 0;
        for (final int integer : array) {
            if (integer > 0) {
                positiveIntegers[i++] = integer;
            }
        }
        return positiveIntegers;
    }

    /**
     * {@return the negative values in this integer list's integers}
     * The returned array may be modified.
     */
    public int[] getNegativeValues() {
        final int countNegative = countNegatives();
        int[] negativeIntegers = new int[countNegative];
        int i = 0;
        for (final int integer : array) {
            if (integer < 0) {
                negativeIntegers[i++] = integer;
            }
        }
        return negativeIntegers;
    }

    /**
     * {@return whether this integer list contains any of the given integers}
     *
     * @param integers the integers
     */
    public boolean containsAny(int... integers) {
        return Arrays.stream(integers).anyMatch(integer -> indexOf(integer) >= 0);
    }

    /**
     * {@return whether this integer list contains all of the given integers}
     *
     * @param integers the integers
     */
    public boolean containsAll(int... integers) {
        return Arrays.stream(integers).noneMatch(integer -> indexOf(integer) < 0);
    }

    /**
     * {@return the index of the given integer in this integer list}
     * To ensure performance, no {@link Result} is created.
     * Instead, a negative number is returned when the integer is not contained.
     *
     * @param integer the integer
     */
    public int indexOf(int integer) {
        return IntStream.range(0, array.length)
                .filter(i -> integer == array[i])
                .findFirst()
                .orElse(-1);
    }

    /**
     * {@return the number of positive values in this integer list's integers}
     */
    public int countPositives() {
        return (int) Arrays.stream(array).filter(integer -> integer > 0).count();
    }

    /**
     * {@return the number of negative values in this integer list's integers}
     */
    public int countNegatives() {
        return (int) Arrays.stream(array).filter(integer -> integer < 0).count();
    }

    /**
     * {@return the number of integers in this integer list}
     */
    public int size() {
        return array.length;
    }

    /**
     * {@return whether this integer list is empty}
     */
    public boolean isEmpty() {
        return array.length == 0;
    }

    /**
     * {@return the union of this integer list with the given integers}
     * No duplicated are created.
     *
     * @param integers the integers
     */
    public int[] addAll(int... integers) {
        boolean[] intersectionMarker = new boolean[array.length];
        int count = sizeOfIntersection(integers, intersectionMarker);

        int[] newArray = new int[array.length + integers.length - count];
        int j = 0;
        for (int i = 0; i < array.length; i++) {
            if (!intersectionMarker[i]) {
                newArray[j++] = array[i];
            }
        }
        System.arraycopy(integers, 0, newArray, j, integers.length);
        return newArray;
    }

    /**
     * {@return the intersection of this integer list with the given integers}
     *
     * @param integers the integers
     */
    public int[] retainAll(int... integers) {
        boolean[] intersectionMarker = new boolean[array.length];
        int count = sizeOfIntersection(integers, intersectionMarker);

        int[] newArray = new int[count];
        int j = 0;
        for (int i = 0; i < array.length; i++) {
            if (intersectionMarker[i]) {
                newArray[j++] = array[i];
            }
        }
        return newArray;
    }

    /**
     * {@return the difference of this integer list and the given integers}
     *
     * @param integers the integers
     */
    public int[] removeAll(int... integers) {
        boolean[] intersectionMarker = new boolean[array.length];
        int count = sizeOfIntersection(integers, intersectionMarker);

        int[] newArray = new int[array.length - count];
        int j = 0;
        for (int i = 0; i < array.length; i++) {
            if (!intersectionMarker[i]) {
                newArray[j++] = array[i];
            }
        }
        return newArray;
    }

    /**
     * {@return the size of the intersection of this integer list with the given integers}
     * The integers that are in the intersection are marked.
     *
     * @param integers the integers
     * @param intersectionMarker the intersection marker
     */
    protected int sizeOfIntersection(int[] integers, boolean[] intersectionMarker) {
        int count = 0;
        for (int integer : integers) {
            final int index = indexOf(integer);
            if (index >= 0) {
                count++;
                if (intersectionMarker != null) {
                    intersectionMarker[index] = true;
                }
            }
        }
        return count;
    }

    /**
     * {@return the size of the intersection of this integer list with the given integers}
     *
     * @param integers the integers
     */
    public int sizeOfIntersection(int... integers) {
        return sizeOfIntersection(integers, null);
    }

    /**
     * {@return whether this integer list and the given integers are disjoint}
     *
     * @param integers the integers
     */
    public boolean isDisjoint(int... integers) {
        return sizeOfIntersection(integers) == 0;
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

    public static class DescendingLengthComparator implements Comparator<IntegerList> {
        @Override
        public int compare(IntegerList o1, IntegerList o2) {
            return o2.get().length - o1.get().length;
        }
    }

    public static class AscendingLengthComparator implements Comparator<IntegerList> {
        @Override
        public int compare(IntegerList o1, IntegerList o2) {
            return o1.get().length - o2.get().length;
        }
    }
}
