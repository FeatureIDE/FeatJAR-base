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

import java.util.*;
import java.util.function.BiFunction;

/**
 * A tree of nodes that can be traversed.
 * Primary implementation of {@link ITree} that uses {@link ArrayList} to store children.
 * The terms "tree" and "node" are interchangeable.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class ATree<T extends ITree<T>> implements ITree<T> {
    /**
     * The children of this node.
     */
    protected final List<T> children = new ArrayList<>();
    protected boolean hashCodeValid;
    protected int hashCode; // todo: cache hash code

    @Override
    public List<? extends T> getChildren() {
        return children;
    }

    /**
     * {@inheritDoc}
     * The given list is copied.
     */
    @Override
    public void setChildren(List<? extends T> children) {
        Objects.requireNonNull(children);
        assertChildrenCountInRange(children.size());
        assertChildValidator(children);
        this.children.clear();
        this.children.addAll(children);
    }

    /**
     * Adds a new child at a given position.
     * If the position is out of bounds, add the new child as the last child.
     *
     * @param index the new position
     * @param newChild the new child
     */
    @Override
    public void addChild(int index, T newChild) {
        assertChildrenCountInRange(children.size() + 1);
        assertChildValidator(newChild);
        if (index > getChildrenCount()) {
            children.add(newChild);
        } else {
            children.add(index, newChild);
        }
    }

    /**
     * Adds a new child as the last child.
     *
     * @param newChild the new child
     */
    @Override
    public void addChild(T newChild) {
        assertChildrenCountInRange(children.size() + 1);
        assertChildValidator(newChild);
        children.add(newChild);
    }

    /**
     * Removes a child.
     *
     * @param child the child to be removed
     * @throws NoSuchElementException if the given old node is not a child
     */
    @Override
    public void removeChild(T child) {
        assertChildrenCountInRange(children.size() - 1);
        if (!children.remove(child)) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Removes the child at a given position.
     *
     * @param index the position to be removed
     * @return the removed child
     * @throws IndexOutOfBoundsException if the given index is out of bounds
     */
    @Override
    public T removeChild(int index) {
        assertChildrenCountInRange(children.size() - 1);
        return children.remove(index);
    }

    /**
     * {@inheritDoc}
     * Uses a {@link ListIterator} to avoid creation of a new list.
     */
    @Override
    public boolean replaceChildren(BiFunction<Integer, T, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        boolean modified = false;
        for (ListIterator<T> it = children.listIterator(); it.hasNext(); ) {
            final int idx = it.nextIndex();
            final T child = it.next();
            final T replacement = mapper.apply(idx, child);
            if (replacement != null && replacement != child) {
                assertChildValidator(replacement);
                it.set(replacement);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Replaces a child with a new child.

     * @param oldChild the old child
     * @param newChild the new child
     * @throws NoSuchElementException if the given old node is not a child
     * @return whether any child was modified
     */
    @Override
    public boolean replaceChild(T oldChild, T newChild) {
        final int index = children.indexOf(oldChild);
        if (index == -1)
            throw new NoSuchElementException();
        assertChildValidator(newChild);
        if (oldChild != newChild)
            children.set(index, newChild);
        return oldChild != newChild;
    }

    /**
     * Replaces a child at an index with a new child.
     * Does nothing if the index is out of bounds.
     *
     * @param idx the index
     * @param newChild the new child
     * @return whether any child was modified
     */
    @Override
    public boolean replaceChild(int idx, T newChild) {
        if (idx < 0 || idx > getChildrenCount())
            throw new NoSuchElementException();
        assertChildValidator(newChild);
        if (children.get(idx) != newChild)
            children.set(idx, newChild);
        return children.get(idx) != newChild;
    }

    /**
     * {@return a deep clone of this node}
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        return cloneTree();
    }

    /**
     * {@return whether this node (and its children) are equal to another (and its children)}
     *
     * @param other the other node
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        return getClass() == other.getClass() && equalsTree((T) other);
    }

    /**
     * {@return the hash code of this node (and its children)}
     */
    @Override
    public int hashCode() {
        return hashCodeTree();
    }
}
