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

import java.util.*;

import org.spldev.util.*;
import org.spldev.util.job.*;

/**
 * Holds arbitrary elements that can be derived from each other.
 *
 * @author Sebastian Krieter
 */
public class CacheHolder {

	private final HashMap<Identifier<?>, Map<Object, Object>> map = new HashMap<>();

	/**
	 * Get an arbitrary element that can be derived from any element in the cache.<br>
	 * This methods first checks whether there is a cached instance and only
	 * computes the requested object otherwise.
	 *
	 * @param <T>      the type of the element
	 * @param provider the provider that is used in case the element is not already
	 *                 contained in the cache.
	 * @return a {@link Result} with a suitable element.
	 */
	public <T> Result<T> get(Provider<T> provider) {
		return get(provider, null);
	}

	/**
	 * Get an arbitrary element that can be derived from the associated feature
	 * model.<br>
	 * This methods first checks whether there is a cached instance and only
	 * computes the requested object otherwise.
	 *
	 * @param <T>      the type of the element
	 * @param provider the provider that is used in case the element is not already
	 *                 contained in the cache.
	 * @param monitor  a monitor for keep track of progress and canceling the
	 *                 computation of the requested element.
	 * @return a {@link Result} with a suitable element.
	 */
	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Provider<T> provider, InternalMonitor monitor) {
		monitor = monitor != null ? monitor : new NullMonitor();
		try {
			final Map<Object, Object> cachedElements = getCachedElement(provider.getIdentifier());
			synchronized (cachedElements) {
				T element = (T) cachedElements.get(provider.getParameters());
				if (element == null) {
					final Result<T> computedElement = computeElement(provider, monitor);
					if (computedElement.isPresent()) {
						element = computedElement.get();
						cachedElements.put(provider.getParameters(), element);
					} else {
						return computedElement;
					}
				}
				return Result.of(element);
			}
		} finally {
			monitor.done();
		}
	}

	public <T> Result<T> set(Provider<T> builder) {
		return set(builder, null);
	}

	public <T> Result<T> set(Provider<T> builder, InternalMonitor monitor) {
		monitor = monitor != null ? monitor : new NullMonitor();
		try {
			final Map<Object, Object> cachedElements = getCachedElement(builder.getIdentifier());
			synchronized (cachedElements) {
				final Result<T> computedElement = computeElement(builder, monitor);
				if (computedElement.isPresent()) {
					final T element = computedElement.get();
					cachedElements.put(builder.getParameters(), element);
					return Result.of(element);
				} else {
					return computedElement;
				}
			}
		} finally {
			monitor.done();
		}
	}

	public <T> Result<T> get(Identifier<T> identifier) {
		return get(identifier, Provider.defaultParameters);
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Identifier<T> identifier, Object parameters) {
		final Map<Object, Object> cachedElements = getCachedElement(identifier);
		synchronized (cachedElements) {
			return Result.of((T) cachedElements.get(parameters));
		}
	}

	public <T> void reset(Provider<T> builder) {
		reset(builder, null);
	}

	public <T> void reset(Provider<T> builder, InternalMonitor monitor) {
		Map<Object, Object> cachedElement;
		monitor = monitor != null ? monitor : new NullMonitor();
		try {
			synchronized (map) {
				map.clear();
				cachedElement = getCachedElement(builder.getIdentifier());
			}
			synchronized (cachedElement) {
				final Result<T> computedElement = computeElement(builder, monitor);
				if (computedElement.isPresent()) {
					final T element = computedElement.get();
					cachedElement.put(builder.getParameters(), element);
				}
			}
		} finally {
			monitor.done();
		}
	}

	public void reset() {
		synchronized (map) {
			map.clear();
		}
	}

	public <T> void reset(Object identifier) {
		synchronized (map) {
			map.remove(identifier);
		}
	}

	public <T> void reset(Object identifier, Object parameters) {
		synchronized (map) {
			final Map<Object, Object> cachedElements = map.get(identifier);
			if (cachedElements != null) {
				cachedElements.remove(parameters);
			}
		}
	}

	private <T> Result<T> computeElement(Provider<T> builder, InternalMonitor monitor) {
		try {
			return builder.apply(this, monitor);
		} catch (final Exception e) {
			return Result.empty(e);
		}
	}

	private Map<Object, Object> getCachedElement(Identifier<?> identifier) {
		synchronized (map) {
			Map<Object, Object> cachedElement = map.get(identifier);
			if (cachedElement == null) {
				cachedElement = new HashMap<>(3);
				map.put(identifier, cachedElement);
			}
			return cachedElement;
		}
	}

	public void execute(Operation operation) {
		ArrayList<Identifier<?>> identifiers;
		synchronized (map) {
			identifiers = new ArrayList<>(map.keySet());
		}
		for (final Identifier<?> identifier : identifiers) {
			Map<Object, Object> cachedElements;
			synchronized (map) {
				cachedElements = map.get(identifier);
			}
			if (cachedElements != null) {
				synchronized (cachedElements) {
					cachedElements.replaceAll((k, v) -> operation.apply(identifier, v, k));
				}
			}
		}
	}

}
