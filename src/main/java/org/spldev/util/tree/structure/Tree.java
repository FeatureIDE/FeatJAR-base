/* -----------------------------------------------------------------------------
 * Tree-Lib - Simple Java framework for creating and traversing tree structures.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Tree-Lib.
 * 
 * Tree-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Tree-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tree-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/trees> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.tree.structure;

import java.util.*;
import java.util.function.*;

/**
 * Interface for a tree node.
 *
 * @author Sebastian Krieter
 */
public interface Tree<T extends Tree<T>> {

	Tree<T> cloneNode();

	default boolean equalsNode(Object other) {
		return getClass() == other.getClass();
	}

	default boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	Collection<? extends T> getChildren();

	void setChildren(Collection<? extends T> children);

	default void flatMapChildren(Function<T, Collection<? extends T>> mapper) {
		Objects.requireNonNull(mapper);
		final Collection<? extends T> oldChildren = getChildren();
		if (!oldChildren.isEmpty()) {
			final ArrayList<T> newChildren = new ArrayList<>(oldChildren.size());
			boolean modified = false;
			for (final T child : oldChildren) {
				final Collection<? extends T> replacement = mapper.apply(child);
				if (replacement != null) {
					newChildren.addAll(replacement);
					modified = true;
				} else {
					newChildren.add(child);
				}
			}
			if (modified) {
				setChildren(newChildren);
			}
		}
	}

	default void mapChildren(Function<T, ? extends T> mapper) {
		Objects.requireNonNull(mapper);
		final Collection<? extends T> oldChildren = getChildren();
		if (!oldChildren.isEmpty()) {
			final List<T> newChildren = new ArrayList<>(oldChildren.size());
			boolean modified = false;
			for (final T child : oldChildren) {
				final T replacement = mapper.apply(child);
				if ((replacement != null) && (replacement != child)) {
					newChildren.add(replacement);
					modified = true;
				} else {
					newChildren.add(child);
				}
			}
			if (modified) {
				setChildren(newChildren);
			}
		}
	}

}
