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
package de.featjar.base.tree.structure;

import de.featjar.base.data.Result;

import java.util.List;

/**
 * A tree of nodes, each of which has an optional parent.
 * Use this only if nodes need to know about their parents.
 * If possible, use {@link ATree} instead, which allows reusing subtrees.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Elias Kuiter
 */
public abstract class ARootedTree<T extends ARootedTree<T>> extends ATree<T> {
    /**
     * the parent node of this node
     */
    protected T parent = null;

    /**
     * {@return the parent node of this node, if any}
     */
    public Result<T> getParent() {
        return Result.ofNullable(parent);
    }

    /**
     * Sets the parent node of this node.
     *
     * @param newParent the new parent node
     */
    protected void setParent(T newParent) {
        if (newParent == parent) {
            return;
        }
        parent = newParent;
    }

    /**
     * {@return whether this node has a parent node}
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * {@inheritDoc}
     * The old children are changed to have no parent node.
     * The new children are changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
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

    /**
     * {@inheritDoc}
     * The new child is changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addChild(T newChild) {
        super.addChild(newChild);
        newChild.setParent((T) this);
    }

    /**
     * {@inheritDoc}
     * The new child is changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addChild(int index, T newChild) {
        super.addChild(index, newChild);
        newChild.setParent((T) this);
    }

    /**
     * {@inheritDoc}
     * The old child is changed to have no node.
     */
    @Override
    public void removeChild(T child) {
        super.removeChild(child);
        child.setParent(null);
    }

    /**
     * {@inheritDoc}
     * The old child is changed to have no node.
     */
    @Override
    public T removeChild(int index) {
        T child = super.removeChild(index);
        child.setParent(null);
        return child;
    }

    /**
     * {@inheritDoc}
     * The old child is changed to have no node.
     * The new child is changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void replaceChild(T oldChild, T newChild) {
        super.replaceChild(oldChild, newChild);
        oldChild.setParent(null);
        newChild.setParent((T) this);
    }

    /**
     * {@return whether the given node is an ancestor of this node}
     *
     * @param node the node
     */
    public boolean isAncestor(ARootedTree<T> node) {
        Result<T> currentParent = getParent();
        while (currentParent.isPresent()) {
            if (node == currentParent.get()) {
                return true;
            }
            currentParent = currentParent.get().getParent();
        }
        return false;
    }

    /**
     * {@return the root node of this tree}
     */
    @SuppressWarnings("unchecked")
    public T getRoot() {
        T currentTree = (T) this;
        while (currentTree.getParent().isPresent()) {
            currentTree = currentTree.getParent().get();
        }
        return currentTree;
    }

    /**
     * {@return the index of this node in its parent's list of children, if any}
     */
    @SuppressWarnings("unchecked")
    public Result<Integer> getIndex() {
        return getParent().flatMap(parent -> parent.getChildIndex((T) this));
    }
}
