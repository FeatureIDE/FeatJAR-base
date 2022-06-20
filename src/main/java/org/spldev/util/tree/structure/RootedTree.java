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

import java.util.*;

/**
 * A rooted tree where each node has an optional parent and any number of
 * children. Weak parents are used to model containment hierarchies.
 *
 * @author Elias Kuiter
 */
public abstract class RootedTree<T extends RootedTree<T>> extends AbstractNonTerminal<T> {
	protected T parent = null;
	protected final Set<T> weakParents = new HashSet<>();

	// inv: tree.getChildren().stream().allMatch(t -> !t.hasParent() || t.getParent().get() == this)
	// inv: !t.hasParent() || tree.getParent().get().getChildren().contains(tree)
	public Optional<T> getParent() {
		return Optional.ofNullable(parent);
	}

	// inv: tree.getChildren().stream().allMatch(t -> t.hasParent() || t.getWeakParents().contains(this))
	// inv: tree.getWeakParents().stream().allMatch(t -> t.getChildren().contains(t))
	public Set<T> getWeakParents() {
		return weakParents;
	}

	protected void setParent(T newParent) {
		if (newParent == parent) {
			return;
		}
		parent = newParent;
		if (newParent != null) {
			weakParents.clear();
		}
	}

	public boolean hasParent() {
		return parent != null;
	}

	public boolean hasWeakParents() {
		return !getWeakParents().isEmpty();
	}

	@Override
	public void setChildren(List<? extends T> children) {
		for (final T child : getChildren()) {
			child.setParent(null);
		}
		super.setChildren(children);
		for (final T child : children) {
			child.setParent((T) this);
		}
	}

	@Override
	public void addChild(T newChild) {
		super.addChild(newChild);
		newChild.setParent((T) this);
	}

	public void addWeakChild(T newChild) {
		if (newChild.hasParent()) {
			throw new IllegalArgumentException("weak child must be a root");
		}
		if (newChild.getWeakParents().contains((T) this)) {
			throw new IllegalArgumentException("weak child can only be added once");
		}
		super.addChild(newChild);
		newChild.getWeakParents().add((T) this);
	}

	@Override
	public void addChild(int index, T newChild) {
		super.addChild(index, newChild);
		newChild.setParent((T) this);
	}

	public void addWeakChild(int index, T newChild) {
		if (newChild.hasParent()) {
			throw new IllegalArgumentException("weak child must be a root");
		}
		super.addChild(index, newChild);
		newChild.getWeakParents().add((T) this);
	}

	@Override
	public void removeChild(T child) {
		super.removeChild(child);
		child.setParent(null);
	}

	public void removeWeakChild(T child) {
		if (!child.getWeakParents().contains((T) this)) {
			throw new IllegalArgumentException("not a weak child");
		}
		super.removeChild(child);
		child.getWeakParents().remove((T) this);
	}

	@Override
	public T removeChild(int index) {
		T child = super.removeChild(index);
		child.setParent(null);
		return child;
	}

	public void removeWeakChild(int index) {
		removeWeakChild(getChildren().get(index));
	}

	@Override
	public void replaceChild(T oldChild, T newChild) {
		super.replaceChild(oldChild, newChild);
		oldChild.setParent(null);
		newChild.setParent((T) this);
	}

	public boolean isAncestor(RootedTree<T> parent) {
		Optional<T> currentParent = getParent();
		while (currentParent.isPresent()) {
			if (parent == currentParent.get()) {
				return true;
			}
			currentParent = currentParent.get().getParent();
		}
		return false;
	}

	public T getRoot() {
		T currentTree = (T) this;
		while (currentTree.getParent().isPresent()) {
			currentTree = currentTree.getParent().get();
		}
		return currentTree;
	}

	public Optional<Integer> getIndex() {
		return getParent().flatMap(parent -> parent.getChildIndex((T) this));
	}
}
