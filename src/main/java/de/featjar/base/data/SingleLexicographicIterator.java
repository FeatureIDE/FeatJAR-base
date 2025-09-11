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
 * This iterator provides all t-wise combinations of a given set of integer items without permutations.
 * It uses the combinatorial number system to enumerate combinations and enable parallel processing.
 *
 * @param <E> the type of the environment object within a combination
 *
 * @author Sebastian Krieter
 */
public final class SingleLexicographicIterator<E> implements Spliterator<ICombination<E, int[]>> {

    /**
     * {@return a sequential stream using a new instance of the iterator with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     */
    public static Stream<ICombination<Void, int[]>> stream(int[] items, int t) {
        return StreamSupport.stream(new SingleLexicographicIterator<>(items, t, null), false);
    }

    /**
     * {@return a parallel stream using a new instance of the iterator with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     */
    public static Stream<ICombination<Void, int[]>> parallelStream(int[] items, int t) {
        return StreamSupport.stream(new SingleLexicographicIterator<Void>(items, t, null), true);
    }

    /**
     * {@return a sequential stream using a new instance of the iterator with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     * @param <V> the type of the environment object for the combinations
     * @param environmentCreator a supplier for the environment object
     */
    public static <V> Stream<ICombination<V, int[]>> stream(int[] items, int t, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new SingleLexicographicIterator<>(items, t, environmentCreator), false);
    }

    /**
     * {@return a parallel stream using a new instance of the iterator with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     * @param <V> the type of the environment object for the combinations
     * @param environmentCreator a supplier for the environment object
     */
    public static <V> Stream<ICombination<V, int[]>> parallelStream(
            int[] items, int t, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new SingleLexicographicIterator<>(items, t, environmentCreator), true);
    }

    private static final int MINIMUM_SPLIT_SIZE = 10;

    private final SingleLiteralCombination<E> combination;
    private long end;
    private final Supplier<E> environmentCreator;

    /**
     * Constructs a new instance of the iterator with the given items and combination size.
     *
     * @param t the combination size
     * @param items the items
     * @param environmentCreator a supplier for the environment object
     */
    public SingleLexicographicIterator(int[] items, int t, Supplier<E> environmentCreator) {
        this.environmentCreator = environmentCreator;
        combination =
                new SingleLiteralCombination<>(items, t, environmentCreator == null ? null : environmentCreator.get());
        end = combination.maxIndex();
    }

    /**
     * Copy constructor. Used by {@link #trySplit()}.
     * @param other the iterator to copy
     */
    private SingleLexicographicIterator(SingleLexicographicIterator<E> other) {
        environmentCreator = other.environmentCreator;
        combination = new SingleLiteralCombination<E>(
                other.combination, environmentCreator == null ? null : environmentCreator.get());

        long currentIndex = other.combination.index();
        long newStart = currentIndex + ((other.end - currentIndex) / 2);
        other.combination.setIndex(newStart);
        end = newStart;
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
        if (end == combination.index()) {
            return false;
        }
        action.accept(combination);
        combination.advance();

        return true;
    }
}
