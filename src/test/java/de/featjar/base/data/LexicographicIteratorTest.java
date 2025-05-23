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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class LexicographicIteratorTest {
    @Test
    void parallelAndSequenbtialContainSameTuples() {
        streamParallelAndSequential(1, 20);
        streamParallelAndSequential(2, 20);
        streamParallelAndSequential(3, 20);
    }

    @Test
    void parallelStreamContainsAllTuples() {
        streamParallel(1, 20);
        streamParallel(2, 20);
        streamParallel(3, 20);
    }

    private void streamParallel(int k, int n) {
        int size = (int) BinomialCalculator.computeBinomial(n, k);
        int[] counts = new int[size];
        Random random = new Random(1);
        LexicographicIterator.parallelStream(k, n).map(c -> c.combinationIndex).forEach(c -> {
            try {
                Thread.sleep((long) (20 * random.nextDouble()));
            } catch (Exception e) {
            }
            synchronized (counts) {
                counts[Math.toIntExact(c)]++;
            }
        });
        for (int i = 0; i < counts.length; i++) {
            assertEquals(1, counts[i], String.valueOf(i));
        }
    }

    private void streamParallelAndSequential(int t, int n) {
        List<String> pSet = LexicographicIterator.parallelStream(t, n)
                .map(Combination::toString)
                .collect(Collectors.toList());
        List<String> sSet = LexicographicIterator.stream(t, n) //
                .map(Combination::toString) //
                .collect(Collectors.toList());

        assertEquals(sSet.size(), pSet.size(), sSet + "\n!=\n" + pSet);
        assertTrue(new HashSet<>(pSet).containsAll(sSet), sSet + "\n!=\n" + pSet);
        assertTrue(new HashSet<>(sSet).containsAll(pSet), sSet + "\n!=\n" + pSet);
    }
}
