/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021-2022  Sebastian Krieter
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
package org.spldev.util.tree.structure;

import org.spldev.util.tree.Trees;
import org.spldev.util.tree.visitor.DfsVisitor;
import org.spldev.util.tree.visitor.TreePrinter;
import org.spldev.util.tree.visitor.TreeVisitor;

import java.util.*;
import java.util.function.*;

/**
 * Interface for a tree node.
 *
 * @author Sebastian Krieter
 */
public interface Tree<T extends Tree<T>> extends Cloneable {

	Tree<T> cloneNode();

	default boolean equalsNode(Object other) {
		return getClass() == other.getClass();
	}

	default boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	List<? extends T> getChildren();

	void setChildren(List<? extends T> children);

	default int getNumberOfChildren() {
		return getChildren().size();
	}

	default void flatMapChildren(Function<T, List<? extends T>> mapper) {
		Objects.requireNonNull(mapper);
		final List<? extends T> oldChildren = getChildren();
		if (!oldChildren.isEmpty()) {
			final ArrayList<T> newChildren = new ArrayList<>(oldChildren.size());
			boolean modified = false;
			for (final T child : oldChildren) {
				final List<? extends T> replacement = mapper.apply(child);
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
		final List<? extends T> oldChildren = getChildren();
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

	default Optional<T> getFirstChild() {
		if (getChildren().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(getChildren().get(0));
	}

	default Optional<T> getLastChild() {
		if (getChildren().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(getChildren().get(getNumberOfChildren() - 1));
	}

	default <R> Optional<R> traverse(DfsVisitor<R, Tree<T>> visitor) {
		return Trees.traverse(this, visitor);
	}

	default <R> Optional<R> traverse(TreeVisitor<R, Tree<T>> visitor) {
		return Trees.traverse(this, visitor);
	}

	default String print() {
		return Trees.print(this);
	}

	default boolean equals(T other) {
		return false; // todo Trees.equals(this, other);
	}
}
