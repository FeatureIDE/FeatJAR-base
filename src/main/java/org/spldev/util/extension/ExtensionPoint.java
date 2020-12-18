package org.spldev.util.extension;

import java.util.*;
import java.util.concurrent.*;

/**
 * Manages all extensions of this extension point.
 *
 * @author Sebastian Krieter
 */
public abstract class ExtensionPoint<T extends Extension> {

	public static class NoSuchExtensionException extends Exception {

		private static final long serialVersionUID = -8143277745205866068L;

		public NoSuchExtensionException(String message) {
			super(message);
		}
	}

	private final HashMap<String, Integer> indexMap = new HashMap<>();
	private final List<T> extensions = new CopyOnWriteArrayList<>();

	public synchronized boolean addExtension(T extension) {
		if ((extension != null)
			&& !indexMap.containsKey(extension.getId())
			&& extension.initExtension()) {
			indexMap.put(extension.getId(), extensions.size());
			extensions.add(extension);
			return true;
		}
		return false;
	}

	public synchronized List<T> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public Optional<T> getExtension(String id) {
		Objects.requireNonNull(id, "ID must not be null!");
		final Integer index = indexMap.get(id);
		return index != null
			? Optional.of(extensions.get(index))
			: Optional.empty();
	}

}
