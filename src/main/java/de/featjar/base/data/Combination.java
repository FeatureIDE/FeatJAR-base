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
import java.util.function.Supplier;

/**
 * Combination iterator that uses the combinatorial number system to process
 * combinations in parallel.
 *
 * @author Sebastian Krieter
 */
public final class Combination<E> extends ACombination<E, int[]> {

    private int[] selection;

    private final int t, n;
    private final long maxIndex;

    private final BinomialCalculator binomialCalculator;

    public Combination(int t, int n, Supplier<E> environmentCreator) {
        super(t, environmentCreator);
        this.t = t;
        this.n = n;
        binomialCalculator = new BinomialCalculator(t, n);
        maxIndex = binomialCalculator.binomial();
        selection = new int[t];

        elementIndices[0] = 0;
        for (int i = 1; i < t; i++) {
            elementIndices[i] = i;
        }
    }

    public Combination(Combination<E> other, Supplier<E> environmentCreator) {
        super(other, environmentCreator);
        t = other.t;
        n = other.n;
        binomialCalculator = other.binomialCalculator;
        maxIndex = other.maxIndex;
        selection = Arrays.copyOf(other.selection, other.selection.length);
    }

    @Override
    public int[] select() {
        throw new UnsupportedOperationException();
    }

    public int[] getSelection(int[] items) {
        for (int i = 0; i < elementIndices.length; i++) {
            selection[i] = items[elementIndices[i]];
        }
        return selection;
    }

    public int[] getSelection(int[][] items) {
        for (int i = 0; i < elementIndices.length; i++) {
            selection[i] = items[i][elementIndices[i]];
        }
        return selection;
    }

    public int[] createSelection(int[] items) {
        int[] newSelection = new int[elementIndices.length];
        for (int i = 0; i < elementIndices.length; i++) {
            newSelection[i] = items[elementIndices[i]];
        }
        return newSelection;
    }

    public int[] createSelection(int[][] items) {
        int[] newSelection = new int[elementIndices.length];
        for (int i = 0; i < elementIndices.length; i++) {
            newSelection[i] = items[i][elementIndices[i]];
        }
        return newSelection;
    }

    @Override
    public boolean advance() {
        combinationIndex++;

        int i = 0;
        for (; i < t - 1; i++) {
            if (elementIndices[i] + 1 < elementIndices[i + 1]) {
                ++elementIndices[i];
                resetLowerElements(i);
                return true;
            }
        }
        int lastIndex = elementIndices[i] + 1;
        if (lastIndex == n) {
            resetLowerElements(i + 1);
        } else {
            elementIndices[i] = lastIndex;
            resetLowerElements(i);
        }
        return true;
    }

    private void resetLowerElements(int i) {
        for (int j = i - 1; j >= 0; j--) {
            elementIndices[j] = j;
        }
    }

    @Override
    public void setIndex(long start) {
        combinationIndex = start;
        long tempIndex = start;
        for (int i = t; i > 0; i--) {
            if (tempIndex <= 0) {
                elementIndices[i - 1] = i - 1;
            } else {
                final double root = 1.0 / i;
                final int p = (int) Math.ceil(Math.pow(tempIndex * binomialCalculator.factorial(i), root));
                for (int j = p; j <= n; j++) {
                    if (binomialCalculator.binomial(j, i) > tempIndex) {
                        elementIndices[i - 1] = j - 1;
                        tempIndex -= binomialCalculator.binomial(j - 1, i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public long maxIndex() {
        return maxIndex;
    }
}
