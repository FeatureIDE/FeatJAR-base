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
		} catch (ClassCastException e) {
			return Result.empty(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Identifier<T> identifier) {
		try {
			return Result.of((T) properties.get(identifier));
		} catch (ClassCastException e) {
			return Result.empty(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> remove(Identifier<T> identifier) {
		try {
			return Result.of((T) properties.remove(identifier));
		} catch (ClassCastException e) {
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
