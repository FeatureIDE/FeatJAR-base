package org.spldev.util.extension;

import java.util.*;

/**
 * Manages all extension of a certain extension point.
 *
 * @author Sebastian Krieter
 */
public abstract class ExtensionManager<T extends Extension> {

	public static class NoSuchExtensionException extends Exception {

		private static final long serialVersionUID = -8143277745205866068L;

		public NoSuchExtensionException(String message) {
			super(message);
		}
	}

	private final List<T> extensions = new ArrayList<>();

	public synchronized boolean addExtension(T extension) {
		if (extension != null) {
			for (final T t : extensions) {
				if (t.getId().equals(extension.getId())) {
					return false;
				}
			}
			if (extension.initExtension()) {
				extensions.add(extension);
				return true;
			}
		}
		return false;
	}

	public synchronized List<T> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public T getExtension(String id) throws NoSuchExtensionException {
		java.util.Objects.requireNonNull(id, "ID must not be null!");

		for (final T extension : getExtensions()) {
			if (id.equals(extension.getId())) {
				return extension;
			}
		}
		throw new NoSuchExtensionException("No extension found for ID " + id);
	}

}
