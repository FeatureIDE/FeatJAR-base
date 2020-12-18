package org.spldev.util;

import java.util.*;

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

}
