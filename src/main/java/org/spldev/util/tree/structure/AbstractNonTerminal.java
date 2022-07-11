/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
 * 
 * This file is part of util.
 * 
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 * 
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/FeatJAR/util> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.tree.structure;

import org.spldev.util.data.Result;

import java.util.*;
import java.util.function.*;

public abstract class AbstractNonTerminal<T extends Tree<T>> implements Tree<T> {

	protected final List<T> children = new ArrayList<>();

	@Override
	public List<? extends T> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public Optional<Integer> getChildIndex(T child) {
		return Result.indexToOptional(children.indexOf(child));
	}

	public boolean hasChild(T child) {
		return getChildIndex(child).isPresent();
	}

	public boolean isFirstChild(T child) {
		return getChildIndex(child).filter(index -> index == 0).isPresent();
	}

	@Override
	public void setChildren(List<? extends T> children) {
		Objects.requireNonNull(children);
		this.children.clear();
		this.children.addAll(children);
	}

	public void addChild(int index, T newChild) {
		if (index > getNumberOfChildren()) {
			children.add(newChild);
		} else {
			children.add(index, newChild);
		}
	}

	public void addChild(T newChild) {
		children.add(newChild);
	}

	public void removeChild(T child) {
		if (!children.remove(child)) {
			throw new NoSuchElementException();
		}
	}

	public T removeChild(int index) {
		return children.remove(index);
	}

	public void replaceChild(T oldChild, T newChild) {
		final int index = children.indexOf(oldChild);
		children.set(index, newChild);
	}

	public void mapChildren(Function<T, ? extends T> mapper) {
		Objects.requireNonNull(mapper);
		for (ListIterator<T> it = children.listIterator(); it.hasNext();) {
			final T child = it.next();
			final T replacement = mapper.apply(child);
			if ((replacement != null) && (replacement != child)) {
				it.set(replacement);
			}
		}
	}

}
