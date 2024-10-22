/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Combination iterator that uses the combinatorial number system to process
 * combinations in parallel.
 *
 * @author Sebastian Krieter
 */
public final class MultiLiteralCombination<E> extends ACombination<E, int[]> {

    public final int[][] items;
    private int[] selection;

    private final int[] t;
    private final long maxIndex;

    public MultiLiteralCombination(int[][] items, int[] t, Supplier<E> environmentCreator) {
        super(IntStream.of(Objects.requireNonNull(t)).sum(), environmentCreator);
        Objects.requireNonNull(items);

        if (items.length != t.length) {
            throw new IllegalArgumentException(String.format(
                    "Number of item sets (%d) must be the same as index length (%d)", items.length, t.length));
        }
        this.t = t;
        this.items = new int[elementIndices.length][];
        selection = new int[IntStream.of(t).sum()];

        int index = 0;
        for (int k = 0; k < t.length; k++) {
            int tk = t[k];
            int[] item = items[k];
            for (int i = 0; i < tk; i++) {
                this.items[index++] = item;
            }
        }

        index = 0;
        for (int tk : t) {
            elementIndices[index++] = 0;
            for (int i = 1; i < tk; i++) {
                elementIndices[index++] = i;
            }
        }

        if (elementIndices.length > 0) {
            BinomialCalculator binomialCalculator = new BinomialCalculator(
                    IntStream.of(t).max().getAsInt(),
                    Arrays.stream(items).mapToInt(a -> a.length).max().getAsInt());
            long sum = 1;
            for (int k = 0; k < t.length; k++) {
                int tk = t[k];
                int nk = items[k].length;
                sum *= binomialCalculator.binomial(nk, tk);
            }
            maxIndex = sum;
        } else {
            maxIndex = 0;
        }
    }

    @Override
    public int[] select() {
        for (int i = 0; i < elementIndices.length; i++) {
            selection[i] = items[i][elementIndices[i]];
        }
        return selection;
    }

    @Override
    public boolean advance() {
        int offset = 0;
        for (int k = 0; k < t.length; k++) {
            int tk = t[k];
            int nk = items[k].length;

            int i = 0;
            for (; i < tk - 1; i++) {
                if (elementIndices[i + offset] + 1 < elementIndices[i + 1 + offset]) {
                    ++elementIndices[i + offset];
                    resetLowerElements(offset, k, i);
                    combinationIndex++;
                    return true;
                }
            }
            int lastIndex = elementIndices[i + offset] + 1;
            if (lastIndex != nk) {
                elementIndices[i + offset] = lastIndex;
                resetLowerElements(offset, k, i);
                combinationIndex++;
                return true;
            }
            offset += tk;
        }

        resetLowerElements(0, t.length, 0);
        combinationIndex = 0;
        return true;
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
