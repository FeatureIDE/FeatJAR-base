/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.spldev.util.Result;

/**
 * Key-Value map for storing arbitrary data.
 *
 * @author Sebastian Krieter
 */
public class KeyValueMap {

	private Map<Identifier<?>, Object> properties = new HashMap<>();

	public KeyValueMap() {
	}

	public KeyValueMap(KeyValueMap other) {
		properties.putAll(other.properties);
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> set(Identifier<T> identifier, T value) {
		try {
			return Result.of((T) properties.put(identifier, value));
		} catch (final ClassCastException e) {
			return Result.empty(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Identifier<T> identifier) {
		try {
			return Result.of((T) properties.get(identifier));
		} catch (final ClassCastException e) {
			return Result.empty(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> remove(Identifier<T> identifier) {
		try {
			return Result.of((T) properties.remove(identifier));
		} catch (final ClassCastException e) {
			return Result.empty(e);
		}
	}

	public void clear() {
		properties.clear();
	}

	public Set<Identifier<?>> getPropertyIdentifiers() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	public Set<Entry<Identifier<?>, Object>> getProperties() {
		return Collections.unmodifiableSet(properties.entrySet());
	}

	@Override
	public int hashCode() {
		int result = properties.size();
		for (final Entry<Identifier<?>, Object> entry : properties.entrySet()) {
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
		return Objects.equals(properties, ((KeyValueMap) other).properties);
	}

}
