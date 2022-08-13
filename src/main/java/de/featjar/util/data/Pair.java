/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.data;

import java.util.Objects;

/**
 * A tuple consisting of any two elements.
 *
 * @param <A> class of first element
 * @param <B> class of second element
 *
 * @author Sebastian Krieter
 */
public class Pair<A, B> {

	private final A key;
	private final B value;

	public Pair(A key, B value) {
		this.key = key;
		this.value = value;
	}

	public A getKey() {
		return key;
	}

	public B getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final Pair<?, ?> other = (Pair<?, ?>) obj;
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Pair [" + key + " -> " + value + "]";
	}

}
