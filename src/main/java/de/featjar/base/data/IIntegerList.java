/*
 * Copyright (C) 2023 FeatJAR-Development-Team
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
 * Implemented with specific interpretations of these integers (e.g., as an index into a {@link RangeMap}).
 * Negative and zero integers are allowed.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
// TODO is this interface necessary?
public interface IIntegerList extends Supplier<int[]> {
    static <T> T merge(Collection<IIntegerList> integerLists, Function<int[], T> integerListFactory) {
        return integerListFactory.apply(integerLists.stream()
                .flatMapToInt(l -> Arrays.stream(l.get()))
                .distinct()
                .toArray());
    }

    /**
     * {@return this integer list's integers}
     * The returned get() must not be modified.
     */
    int[] get();

    /**
     * {@return this integer list's integers as an {@code IntStream}}
     */
    IntStream stream();

    /**
     * {@return the value at the given index of this integer list}
     * To ensure performance, no {@link Result} is created, so the index should be checked for validity beforehand.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException when the index is invalid
     */
    default int get(int index) {
        return get()[index];
    }

    /**
     * {@return a new integer list containing the negated values of this integer list}
     */
    int[] negate();

    /**
     * {@return a copy of this integer list's integers}
     * The returned get() may be modified.
     */
    default int[] copy() {
        return copyOfRange(0, get().length);
    }

    /**
     * {@return a copy of this integer list's integers in a given range}
     * The returned get() may be modified.
     *
     * @param range the range
     */
    default int[] copyOfRange(Range range) {
        return copyOfRange(
                range.getLowerBound().orElse(0), range.getUpperBound().orElse(get().length));
    }

    /**
     * {@return a copy of this integer list's integers in a given range}
     * The returned get() may be modified.
     *
     * @param start the start index
     * @param end the end index
     */
    default int[] copyOfRange(int start, int end) {
        return Arrays.copyOfRange(get(), start, end);
    }

    /**
     * {@return the absolute values of this integer list's integers}
     * The returned get() may be modified.
     */
    default int[] getAbsoluteValues() {
        return Arrays.stream(get()).map(Math::abs).toArray();
    }

    /**
     * {@return the positive values in this integer list's integers}
     * The returned get() may be modified.
     */
    default int[] getPositiveValues() {
        final int countPositive = countPositives();
        int[] positiveIntegers = new int[countPositive];
        int i = 0;
        for (final int integer : get()) {
            if (integer > 0) {
                positiveIntegers[i++] = integer;
            }
        }
        return positiveIntegers;
    }

    /**
     * {@return the negative values in this integer list's integers}
     * The returned get() may be modified.
     */
    default int[] getNegativeValues() {
        final int countNegative = countNegatives();
        int[] negativeIntegers = new int[countNegative];
        int i = 0;
        for (final int integer : get()) {
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
    default boolean containsAny(int... integers) {
        return Arrays.stream(integers).anyMatch(integer -> indexOf(integer) >= 0);
    }

    /**
     * {@return whether this integer list contains any of the given integers in negated form}
     *
     * @param integers the integers
     */
    default boolean containsAnyNegated(int... integers) {
        return Arrays.stream(integers).anyMatch(integer -> indexOf(-integer) >= 0);
    }

    /**
     * {@return whether this integer list contains all of the given integers}
     *
     * @param integers the integers
     */
    default boolean containsAll(int... integers) {
        return Arrays.stream(integers).allMatch(integer -> indexOf(integer) >= 0);
    }

    /**
     * {@return whether this integer list contains all of the given integers in negated form}
     *
     * @param integers the integers
     */
    default boolean containsAllNegated(int... integers) {
        return Arrays.stream(integers).allMatch(integer -> indexOf(-integer) >= 0);
    }

    /**
     * {@return whether this integer list contains any integer in the given integer list}
     *
     * @param integers another integer list
     */
    default boolean containsAny(IIntegerList integers) {
        return containsAny(integers.get());
    }

    /**
     * {@return whether this integer list contains any negated integer in the given integer list}
     *
     * @param integers another integer list
     */
    default boolean containsAnyNegated(IIntegerList integers) {
        return containsAnyNegated(integers.get());
    }

    /**
     * {@return whether this integer list contains all integers in the given integer list}
     *
     * @param integers another integer list
     */
    default boolean containsAll(IIntegerList integers) {
        return containsAll(integers.get());
    }

    /**
     * {@return whether this integer list contains all negated integers in the given integer list}
     *
     * @param integers another integer list
     */
    default boolean containsAllNegated(IIntegerList integers) {
        return containsAllNegated(integers.get());
    }

    /**
     * {@return the index of the given integer in this integer list}
     * To ensure performance, no {@link Result} is created.
     * Instead, a negative number is returned when the integer is not contained.
     *
     * @param integer the integer
     */
    default int indexOf(int integer) {
        return IntStream.range(0, get().length)
                .filter(i -> integer == get()[i])
                .findFirst()
                .orElse(-1);
    }

    /**
     * {@return the number of positive values in this integer list's integers}
     */
    default int countPositives() {
        return (int) Arrays.stream(get()).filter(integer -> integer > 0).count();
    }

    /**
     * {@return the number of negative values in this integer list's integers}
     */
    default int countNegatives() {
        return (int) Arrays.stream(get()).filter(integer -> integer < 0).count();
    }

    /**
     * {@return the number of non-zero values in this integer list's integers}
     */
    default int countNonZero() {
        return countPositives() + countNegatives();
    }

    /**
     * {@return the number of integers in this integer list}
     */
    default int size() {
        return get().length;
    }

    /**
     * {@return whether this integer list is empty}
     */
    default boolean isEmpty() {
        return get().length == 0;
    }

    /**
     * {@return the union of this integer list with the given integers}
     * No duplicated are created.
     *
     * @param integers the integers
     */
    default int[] addAll(int... integers) {
        boolean[] intersectionMarker = new boolean[get().length];
        int count = sizeOfIntersection(integers, intersectionMarker);

        int[] newArray = new int[get().length + integers.length - count];
        int j = 0;
        for (int i = 0; i < get().length; i++) {
            if (!intersectionMarker[i]) {
                newArray[j++] = get()[i];
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
    default int[] retainAll(int... integers) {
        boolean[] intersectionMarker = new boolean[get().length];
        int count = sizeOfIntersection(integers, intersectionMarker);

        int[] newArray = new int[count];
        int j = 0;
        for (int i = 0; i < get().length; i++) {
            if (intersectionMarker[i]) {
                newArray[j++] = get()[i];
            }
        }
        return newArray;
    }

    /**
     * {@return the difference of this integer list and the given integers}
     *
     * @param integers the integers
     */
    default int[] removeAll(int... integers) {
        boolean[] intersectionMarker = new boolean[get().length];
        int count = sizeOfIntersection(integers, intersectionMarker);

        int[] newArray = new int[get().length - count];
        int j = 0;
        for (int i = 0; i < get().length; i++) {
            if (!intersectionMarker[i]) {
                newArray[j++] = get()[i];
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
    private int sizeOfIntersection(int[] integers, boolean[] intersectionMarker) {
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
    default int sizeOfIntersection(int... integers) {
        return sizeOfIntersection(integers, null);
    }

    /**
     * {@return whether this integer list and the given integers are disjoint}
     *
     * @param integers the integers
     */
    default boolean isDisjoint(int... integers) {
        return sizeOfIntersection(integers) == 0;
    }

    class DescendingLengthComparator implements Comparator<IIntegerList> {
        @Override
        public int compare(IIntegerList o1, IIntegerList o2) {
            return o2.get().length - o1.get().length;
        }
    }

    class AscendingLengthComparator implements Comparator<IIntegerList> {
        @Override
        public int compare(IIntegerList o1, IIntegerList o2) {
            return o1.get().length - o2.get().length;
        }
    }
}
