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
public final class SingleLexicographicIterator<E> implements Spliterator<ICombination<E, int[]>> {

    public static Stream<ICombination<Void, int[]>> stream(int[] items, int t) {
        return StreamSupport.stream(new SingleLexicographicIterator<>(items, t, null), false);
    }

    public static Stream<ICombination<Void, int[]>> parallelStream(int[] items, int t) {
        return StreamSupport.stream(new SingleLexicographicIterator<Void>(items, t, null), true);
    }

    public static <V> Stream<ICombination<V, int[]>> stream(int[] items, int t, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new SingleLexicographicIterator<>(items, t, environmentCreator), false);
    }

    public static <V> Stream<ICombination<V, int[]>> parallelStream(
            int[] items, int t, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new SingleLexicographicIterator<>(items, t, environmentCreator), true);
    }

    public static void main(String[] args) {
        int[] i2 = new int[] {1, 2, 3, 4, 5};
        stream(i2, 2).forEach(System.out::println);
    }

    private static final int MINIMUM_SPLIT_SIZE = 10;

    private final SingleLiteralCombination<E> combination;
    private long end;
    private final Supplier<E> environmentCreator;

    public SingleLexicographicIterator(int[] items, int t, Supplier<E> environmentCreator) {
        this.environmentCreator = environmentCreator;
        combination = new SingleLiteralCombination<>(items, t, environmentCreator);
        end = combination.maxIndex();
    }

    private SingleLexicographicIterator(SingleLexicographicIterator<E> other) {
        this.environmentCreator = other.environmentCreator;
        combination = new SingleLiteralCombination<E>(other.combination, other.environmentCreator);
        end = other.end;

        long currentIndex = other.combination.index();
        long start = currentIndex + ((other.end - currentIndex) / 2);
        combination.setIndex(start);
        other.end = start - 1;
    }

    @Override
    public int characteristics() {
        return ORDERED | DISTINCT | SIZED | NONNULL | IMMUTABLE | SUBSIZED;
    }

    @Override
    public long estimateSize() {
        return end - combination.index();
    }

    @Override
    public Spliterator<ICombination<E, int[]>> trySplit() {
        return (estimateSize() < MINIMUM_SPLIT_SIZE) ? null : new SingleLexicographicIterator<>(this);
    }

    @Override
    public boolean tryAdvance(Consumer<? super ICombination<E, int[]>> action) {
        if (combination.maxIndex() == combination.index()) {
            return false;
        }
        action.accept(combination);
        combination.advance();

        return true;
    }
}
