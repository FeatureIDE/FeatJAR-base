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
package de.featjar.base.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * An unordered list of integers.
 * Subclasses implement specific interpretations of these integers (e.g., as an index into a {@link RangeMap}).
 * Negative integers are allowed.
 * TODO: javadoc is mostly missing
 *
 * @param <T> the type of the implementing subclass
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class IntegerList<T extends IntegerList<?>> implements Comparable<T> {
    protected final int[] integers;
    protected int hashCode;

    public IntegerList(int... integers) {
        this.integers = integers;
        hashCode = Arrays.hashCode(this.integers);
    }


    public IntegerList(Collection<Integer> integers) {
        this(integers.stream().mapToInt(Integer::intValue).toArray());
    }

    public IntegerList(T sortedIntegerList) {
        integers = Arrays.copyOf(sortedIntegerList.integers, sortedIntegerList.integers.length);
        hashCode = sortedIntegerList.hashCode;
    }

    protected abstract T newIntegerList(int[] integers);

    public int[] getIntegers() {
        return integers;
    }

    public int[] getAbsoluteValuesOfIntegers() {
        return Arrays.stream(integers)
                .map(Math::abs)
                .toArray();
    }

    public int get(int index) {
        return integers[index];
    }

    public int[] get(int start, int end) {
        return Arrays.copyOfRange(integers, start, end);
    }

    public T getPositives() {
        final int countPositive = countPositives();
        int[] positiveIntegers = new int[countPositive];
        int i = 0;
        for (final int integer : integers) {
            if (integer > 0) {
                positiveIntegers[i++] = integer;
            }
        }
        return newIntegerList(positiveIntegers);
    }

    public T getNegatives() {
        final int countNegative = countNegatives();
        int[] negativeIntegers = new int[countNegative];
        int i = 0;
        for (final int integer : integers) {
            if (integer < 0) {
                negativeIntegers[i++] = integer;
            }
        }
        return newIntegerList(negativeIntegers);
    }

    public boolean containsAny(int... integers) {
        return Arrays.stream(integers)
                .anyMatch(integer -> indexOf(integer) >= 0);
    }

    public boolean containsAll(int... integers) {
        return Arrays.stream(integers)
                .noneMatch(integer -> indexOf(integer) < 0);
    }

    public boolean containsAny(T sortedIntegerList) {
        return Arrays.stream(sortedIntegerList.getIntegers())
                .anyMatch(integer -> indexOf(integer) >= 0);
    }

    public boolean containsAll(T sortedIntegerList) {
        return Arrays.stream(sortedIntegerList.getIntegers())
                .noneMatch(integer -> indexOf(integer) < 0);
    }

    public int indexOf(int integer) {
        return IntStream.range(0, integers.length)
                .filter(i -> integer == integers[i])
                .findFirst()
                .orElse(-1);
    }

    public int countNegatives() {
        return (int) Arrays.stream(integers)
                .filter(integer -> integer < 0)
                .count();
    }

    public int countPositives() {
        return (int) Arrays.stream(integers)
                .filter(integer -> integer > 0)
                .count();
    }

    public int size() {
        return integers.length;
    }

    public boolean isEmpty() {
        return integers.length == 0;
    }

    public T addAll(T sortedIntegerList) {
        final boolean[] marker = new boolean[integers.length];
        final int count = sizeOfIntersection(sortedIntegerList.integers, marker);

        final int[] newIntegers = new int[integers.length + sortedIntegerList.integers.length - count];
        int j = 0;
        for (int i = 0; i < integers.length; i++) {
            if (!marker[i]) {
                newIntegers[j++] = integers[i];
            }
        }
        System.arraycopy(sortedIntegerList.integers, 0, newIntegers, j, sortedIntegerList.integers.length);
        return newIntegerList(newIntegers);
    }

    public T removeAll(T sortedIntegerList) {
        final boolean[] removeMarker = new boolean[integers.length];
        final int count = sizeOfIntersection(sortedIntegerList.integers, removeMarker);

        final int[] newIntegers = new int[integers.length - count];
        int j = 0;
        for (int i = 0; i < integers.length; i++) {
            if (!removeMarker[i]) {
                newIntegers[j++] = integers[i];
            }
        }
        return newIntegerList(newIntegers);
    }

    public T retainAll(T sortedIntegerList) {
        final boolean[] marker = new boolean[integers.length];
        final int count = sizeOfIntersection(sortedIntegerList.integers, marker);

        final int[] newIntegers = new int[count];
        int j = 0;
        for (int i = 0; i < integers.length; i++) {
            if (marker[i]) {
                newIntegers[j++] = integers[i];
            }
        }
        return newIntegerList(newIntegers);
    }

    protected int sizeOfIntersection(int[] integers, final boolean[] removeMarker) {
        int count = 0;
        for (int integer : integers) {
            final int index = indexOf(integer);
            if (index >= 0) {
                count++;
                if (removeMarker != null) {
                    removeMarker[index] = true;
                }
            }
        }
        return count;
    }

    public int sizeOfIntersection(int[] integers) {
        return sizeOfIntersection(integers, null);
    }

    public int sizeOfIntersection(T sortedIntegerList) {
        return sizeOfIntersection(sortedIntegerList.getIntegers());
    }

    public boolean isDisjoint(T sortedIntegerList) {
        return sizeOfIntersection(sortedIntegerList) == 0;
    }

    @Override
    public int hashCode() {
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
        return Arrays.equals(integers, ((IntegerList<?>) obj).integers);
    }

    @Override
    public int compareTo(IntegerList other) {
        final int lengthDiff = integers.length - other.integers.length;
        if (lengthDiff != 0) {
            return lengthDiff;
        }
        for (int i = 0; i < integers.length; i++) {
            final int diff = integers[i] - other.integers[i];
            if (diff != 0) {
                return diff;
            }
        }
        return lengthDiff;
    }

    @Override
    public String toString() {
        return Arrays.toString(integers);
    }

    public static class DescendingLengthComparator implements Comparator<IntegerList<?>> {
        @Override
        public int compare(IntegerList o1, IntegerList o2) {
            return o2.getIntegers().length - o1.getIntegers().length;
        }
    }

    public static class AscendingLengthComparator implements Comparator<IntegerList<?>> {
        @Override
        public int compare(IntegerList o1, IntegerList o2) {
            return o1.getIntegers().length - o2.getIntegers().length;
        }
    }
}
