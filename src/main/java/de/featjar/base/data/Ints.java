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

import java.util.stream.IntStream;

public final class Ints {

    private Ints() {}

    public static int[] grayCode(int t) {
        final int[] gray = IntStream.rangeClosed(1, 1 << t)
                .map(Integer::numberOfTrailingZeros)
                .toArray();
        gray[gray.length - 1] = 0;
        return gray;
    }

    public static int[] filteredList(final int size, IntegerList filter) {
        int[] list = IntStream.rangeClosed(1, size).toArray();
        for (int e : filter.elements) {
            list[Math.abs(e) - 1] = 0;
        }
        return IntStream.of(list).filter(i -> i != 0).toArray();
    }
}
