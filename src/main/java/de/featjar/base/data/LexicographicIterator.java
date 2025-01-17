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
public final class LexicographicIterator<E> implements Spliterator<Combination<E>> {

    public static Stream<Combination<Void>> stream(int t, int size) {
        return StreamSupport.stream(new LexicographicIterator<>(t, size, () -> null), false);
    }

    public static Stream<Combination<Void>> parallelStream(int t, int size) {
        return StreamSupport.stream(new LexicographicIterator<>(t, size, () -> null), true);
    }

    public static <V> Stream<Combination<V>> stream(int t, int size, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new LexicographicIterator<>(t, size, environmentCreator), false);
    }

    public static <V> Stream<Combination<V>> parallelStream(int t, int size, Supplier<V> environmentCreator) {
        return StreamSupport.stream(new LexicographicIterator<>(t, size, environmentCreator), true);
    }

    private static final int MINIMUM_SPLIT_SIZE = 10;

    private final Combination<E> combination;
    private long end;
    private final Supplier<E> environmentCreator;

    public LexicographicIterator(int t, int n, Supplier<E> environmentCreator) {
        this.environmentCreator = environmentCreator;
        combination = new Combination<>(t, n, environmentCreator);
        end = combination.maxIndex();
    }

    private LexicographicIterator(LexicographicIterator<E> other) {
        this.environmentCreator = other.environmentCreator;
        combination = new Combination<E>(other.combination, other.environmentCreator);

        long currentIndex = other.combination.index();
        //        end = other.end;
        long start = currentIndex + ((other.end - currentIndex) / 2);
        other.combination.setIndex(start);
        end = start;
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
    public Spliterator<Combination<E>> trySplit() {
        return (estimateSize() < MINIMUM_SPLIT_SIZE) ? null : new LexicographicIterator<>(this);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Combination<E>> action) {
        if (end == combination.index()) {
            return false;
        }
        action.accept(combination);
        combination.advance();

        return true;
    }
}
