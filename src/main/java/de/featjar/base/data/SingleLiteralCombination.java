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

/**
 * Combination object used {@link SingleLexicographicIterator}.
 *
 * @param <E> the type of the environment for the combination
 *
 * @author Sebastian Krieter
 */
public final class SingleLiteralCombination<E> extends ACombination<E, int[]> {

    private final int[] items;
    private final int t;
    private final long maxIndex;

    private int[] selection;
    private int lastChangedIndex;

    private final BinomialCalculator binomialCalculator;

    /**
     * Creates a new combination with the given items, combination size, and environment supplier.
     * @param items the integer items
     * @param t the value for t
     * @param environment the environment
     */
    public SingleLiteralCombination(int[] items, int t, E environment) {
        super(t, environment);
        Objects.requireNonNull(items);

        this.t = t;
        this.items = items;
        binomialCalculator = new BinomialCalculator(t, items.length);
        maxIndex = binomialCalculator.binomial();
        selection = new int[t];

        elementIndices[0] = 0;
        for (int i = 1; i < t; i++) {
            elementIndices[i] = i;
            selection[i] = items[i];
        }
    }

    /**
     * Copy constructor.
     * @param other the combination to copy
     * @param environment a new environment object
     */
    public SingleLiteralCombination(SingleLiteralCombination<E> other, E environment) {
        super(other, environment);
        t = other.t;
        items = other.items;
        binomialCalculator = other.binomialCalculator;
        maxIndex = other.maxIndex;
        selection = Arrays.copyOf(other.selection, other.selection.length);
    }

    @Override
    public int[] select() {
        for (int i = 0; i <= lastChangedIndex; i++) {
            selection[i] = items[elementIndices[i]];
        }
        return selection;
    }

    @Override
    public int[] createSelection() {
        int[] newSelection = new int[selection.length];
        for (int i = 0; i < newSelection.length; i++) {
            newSelection[i] = items[elementIndices[i]];
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
        if (lastIndex == items.length) {
            resetLowerElements(i + 1);
        } else {
            elementIndices[i] = lastIndex;
            resetLowerElements(i);
        }
        return true;
    }

    private void resetLowerElements(int i) {
        lastChangedIndex = i;
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
                for (int j = p; j <= items.length; j++) {
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
