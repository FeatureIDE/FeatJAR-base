/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
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
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.util.tree.structure;

import java.util.List;
import java.util.Optional;

/**
 * A rooted tree where each node has an optional parent and any number of
 * children. Weak parents are used to model containment hierarchies.
 *
 * @author Elias Kuiter
 */
@SuppressWarnings("unchecked")
public abstract class RootedTree<T extends RootedTree<T>> extends AbstractNonTerminal<T> {
    protected T parent = null;

    public Optional<T> getParent() {
        return Optional.ofNullable(parent);
    }

    protected void setParent(T newParent) {
        if (newParent == parent) {
            return;
        }
        parent = newParent;
    }

    public boolean hasParent() {
        return parent != null;
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

    @Override
    public void addChild(int index, T newChild) {
        super.addChild(index, newChild);
        newChild.setParent((T) this);
    }

    @Override
    public void removeChild(T child) {
        super.removeChild(child);
        child.setParent(null);
    }

    @Override
    public T removeChild(int index) {
        T child = super.removeChild(index);
        child.setParent(null);
        return child;
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
