package org.spldev.util;

import java.util.*;

/**
 * Holds arbitrary elements that can be derived from each other.
 *
 * @author Sebastian Krieter
 */
public class Cache {

	private final HashMap<Identifier<?>, Map<Object, Object>> map = new HashMap<>();

	/**
	 * Get an arbitrary element that can be derived from the associated feature
	 * model.<br>
	 * This methods first checks whether there is a cached instance and only
	 * computes the requested object otherwise.
	 *
	 * @param <T>      the type of the element
	 * @param provider the provider that is used in cas the element is not already
	 *                 contained in the cache.
	 * @return a {@link Result} with a suitable element.
	 */
	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Provider<T> provider) {
		final Map<Object, Object> cachedElements = getCachedElements(provider.getIdentifier());
		synchronized (cachedElements) {
			T element = (T) cachedElements.get(provider.getParameters());
			if (element == null) {
				final Result<T> computedElement = computeElement(provider);
				if (computedElement.isPresent()) {
					element = computedElement.get();
					cachedElements.put(provider.getParameters(), element);
				} else {
					return computedElement;
				}
			}
			return Result.of(element);
		}
	}

	public <T> Result<T> compute(Provider<T> builder) {
		final Map<Object, Object> cachedElements = getCachedElements(builder.getIdentifier());
		synchronized (cachedElements) {
			final Result<T> computedElement = computeElement(builder);
			if (computedElement.isPresent()) {
				final T element = computedElement.get();
				cachedElements.put(builder.getParameters(), element);
				return Result.of(element);
			} else {
				return computedElement;
			}
		}
	}

	public <T> Result<T> get(Identifier<T> identifier) {
		return get(identifier, Provider.defaultParameters);
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> get(Identifier<T> identifier, Object parameters) {
		final Map<Object, Object> cachedElements = getCachedElements(identifier);
		synchronized (cachedElements) {
			return Result.of((T) cachedElements.get(parameters));
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

	private <T> Result<T> computeElement(Provider<T> builder) {
		try {
			return builder.apply(this);
		} catch (final Exception e) {
			return Result.empty(e);
		}
	}

	private Map<Object, Object> getCachedElements(Identifier<?> identifier) {
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
