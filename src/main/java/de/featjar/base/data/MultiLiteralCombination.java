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
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Combination object used {@link MultiLexicographicIterator}.
 *
 * @param <E> the type of the environment for the combination
 *
 * @author Sebastian Krieter
 */
public final class MultiLiteralCombination<E> extends ACombination<E, int[]> {

    private final int[][] items;
    private final int[] t;
    private final long maxIndex;

    private int[] selection;

    /**
     * Creates a new combination with the given item sets, combination sizes, and
     * environment.
     *
     * @param items       the integer sets
     * @param t           the combination size for each item set
     * @param environment the environment
     */
    public MultiLiteralCombination(int[][] items, int[] t, E environment) {
        super(IntStream.of(Objects.requireNonNull(t)).sum(), environment);
        Objects.requireNonNull(items);

        if (items.length != t.length) {
            throw new IllegalArgumentException(String.format(
                    "Number of item sets (%d) must be the same as index length (%d)", items.length, t.length));
        }
        this.t = t;
        this.items = items;
        selection = new int[IntStream.of(t).sum()];

        reset();

        if (elementIndices.length > 0) {
            BinomialCalculator binomialCalculator = new BinomialCalculator(
                    IntStream.of(t).max().getAsInt(),
                    Arrays.stream(items).mapToInt(a -> a.length).max().getAsInt());
            long product = 1;
            for (int k = 0; k < t.length; k++) {
                int tk = t[k];
                int nk = items[k].length;
                product *= binomialCalculator.binomial(nk, tk);
            }
            maxIndex = product;
        } else {
            maxIndex = 0;
        }
    }

    public void reset() {
        super.reset();
        int index = 0;
        for (int tk : t) {
            elementIndices[index++] = 0;
            for (int i = 1; i < tk; i++) {
                elementIndices[index++] = i;
            }
        }
    }

    @Override
    public int[] select() {
        return select(selection);
    }

    @Override
    public int[] createSelection() {
        return select(new int[selection.length]);
    }

    private int[] select(int[] selectionArray) {
        int i = 0;
        int tIndex = 0;
        for (int k = 0; k < t.length; k++) {
            int[] itemSet = items[k];
            tIndex += t[k];
            for (; i < tIndex; i++) {
                selectionArray[i] = itemSet[elementIndices[i]];
            }
        }
        return selectionArray;
    }

    @Override
    public boolean advance() {
        combinationIndex++;
        int offset = 0;
        for (int k = 0; k < t.length; k++) {
            int tk = t[k];
            int nk = items[k].length;

            int i = 0;
            for (; i < tk - 1; i++) {
                if (elementIndices[i + offset] + 1 < elementIndices[i + 1 + offset]) {
                    ++elementIndices[i + offset];
                    resetLowerElements(offset, k, i);
                    return true;
                }
            }
            int lastIndex = elementIndices[i + offset] + 1;
            if (lastIndex != nk) {
                elementIndices[i + offset] = lastIndex;
                resetLowerElements(offset, k, i);
                return true;
            }
            offset += tk;
        }
        return false;
    }

    private void resetLowerElements(int offset, int k, int i) {
        for (int j = i - 1; j >= 0; j--) {
            elementIndices[j + offset] = j;
        }

        if (0 < k) {
            int index = 0;
            for (int k2 = 0; k2 < k; k2++) {
                int tk2 = t[k2];
                elementIndices[index++] = 0;
                for (int i2 = 1; i2 < tk2; i2++) {
                    elementIndices[index++] = i2;
                }
            }
        }
    }

    @Override
    public void setIndex(long index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long maxIndex() {
        return maxIndex;
    }
}
