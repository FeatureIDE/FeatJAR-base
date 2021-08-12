/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util Lib.
 * 
 * Util Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.extension;

import java.util.*;
import java.util.concurrent.*;

import org.spldev.util.*;

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

	public Result<T> getExtension(String id) {
		Objects.requireNonNull(id, "ID must not be null!");
		final Integer index = indexMap.get(id);
		return index != null
			? Result.of(extensions.get(index))
			: Result.empty(new NoSuchExtensionException("No extension found for ID " + id));
	}

}
