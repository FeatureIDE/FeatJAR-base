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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Key-Value map for storing arbitrary data.
 *
 * @author Sebastian Krieter
 */
public class KeyValueMap {

	private Map<Identifier<?>, Object> elements = new HashMap<>();

	public KeyValueMap() {
	}

	public KeyValueMap(KeyValueMap other) {
		elements.putAll(other.elements);
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> set(Identifier<T> key, T value) {
		try {
			return Result.of((T) elements.put(key, value));
		} catch (final ClassCastException e) {
			return Result.empty(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Identifier<T> key) {
		try {
			return Result.of((T) elements.get(key));
		} catch (final ClassCastException e) {
			return Result.empty(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> remove(Identifier<T> key) {
		try {
			return Result.of((T) elements.remove(key));
		} catch (final ClassCastException e) {
			return Result.empty(e);
		}
	}

	public void clear() {
		elements.clear();
	}

	public Set<Identifier<?>> getKeys() {
		return Collections.unmodifiableSet(elements.keySet());
	}

	public Set<Entry<Identifier<?>, Object>> getElements() {
		return Collections.unmodifiableSet(elements.entrySet());
	}

	@Override
	public int hashCode() {
		int result = elements.size();
		for (final Entry<Identifier<?>, Object> entry : elements.entrySet()) {
			result += 37 * entry.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if ((other == null) || (getClass() != other.getClass())) {
			return false;
		}
		return Objects.equals(elements, ((KeyValueMap) other).elements);
	}

}
