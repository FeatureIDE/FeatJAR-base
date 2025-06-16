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

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Combination iterator that uses the combinatorial number system to process
 * combinations in parallel.
 *
 * @author Sebastian Krieter
 */
public final class MultiLexicographicIterator<E> implements Spliterator<ICombination<E, int[]>> {

    private final ICombination<E, int[]> combination;

    public static Stream<ICombination<Void, int[]>> stream(int[][] items, int[] t) {
        return StreamSupport.stream(new MultiLexicographicIterator<>(items, t, null), false);
    }

    public static <V> Stream<ICombination<V, int[]>> stream(int[][] items, int[] t, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new MultiLexicographicIterator<>(items, t, environmentCreator), false);
    }

    public static <V> Stream<ICombination<V, int[]>> parallelStream(
            int[][] items, int[] t, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new MultiLexicographicIterator<>(items, t, environmentCreator), true);
    }

    public MultiLexicographicIterator(int[][] items, int[] t, Supplier<E> environmentCreator) {
        combination = new MultiLiteralCombination<>(items, t, environmentCreator);
    }

    @Override
    public int characteristics() {
        return ORDERED | DISTINCT | SIZED | NONNULL | IMMUTABLE | SUBSIZED;
    }

    @Override
    public long estimateSize() {
        return combination.maxIndex() - combination.index();
    }

    @Override
    public Spliterator<ICombination<E, int[]>> trySplit() {
        return null;
    }

    @Override
    public boolean tryAdvance(Consumer<? super ICombination<E, int[]>> action) {
        if (combination.index() == combination.maxIndex()) {
            return false;
        }
        action.accept(combination);
        return combination.advance();
    }
}
