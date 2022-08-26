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

import de.featjar.util.data.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A traversable tree of nodes.
 * Primary implementation of {@link Traversable} that uses {@link ArrayList} to store children.
 * The terms "tree" and "node" are interchangeable.
 *
 * @param <T> type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class Tree<T extends Traversable<T>> implements Traversable<T> {
    /**
     * The children of this node.
     */
    protected final List<T> children = new ArrayList<>();

    /**
     * {@inheritDoc}
     * The returned object cannot be modified.
     */
    @Override
    public List<? extends T> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * {@inheritDoc}
     * The given list is copied.
     */
    @Override
    public void setChildren(List<? extends T> children) {
        Objects.requireNonNull(children);
        this.children.clear();
        this.children.addAll(children);
    }

    /**
     * {@return the index of the given node in the list of children, if any}
     *
     * @param node the node
     */
    public Optional<Integer> getChildIndex(T node) {
        return Result.indexToOptional(children.indexOf(node));
    }

    /**
     * {@return whether the given node is a child of this node}
     *
     * @param child the node
     */
    public boolean hasChild(T child) {
        return getChildIndex(child).isPresent();
    }

    /**
     * Adds a new child at a given position.
     * If the position is out of bounds, add the new child as the last child.
     *
     * @param index the new position
     * @param newChild the new child
     */
    public void addChild(int index, T newChild) {
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
    public void addChild(T newChild) {
        children.add(newChild);
    }

    /**
     * Removes a child.
     * If the given node is not a child, throws a {@link NoSuchElementException}.
     *
     * @param child the child to be removed
     */
    public void removeChild(T child) {
        if (!children.remove(child)) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Removes the child at a given position.
     * If the given node is not a child, throws an {@link IndexOutOfBoundsException}.
     *
     * @param index the position to be removed
     * @return the removed child
     */
    public T removeChild(int index) {
        return children.remove(index);
    }

    /**
     * Replaces a child with a new child.
     * If the given old node is not a child, throws a {@link NoSuchElementException}.

     * @param oldChild the old child
     * @param newChild the new child
     */
    public void replaceChild(T oldChild, T newChild) {
        final int index = children.indexOf(oldChild);
        if (index == -1)
            throw new NoSuchElementException();
        children.set(index, newChild);
    }

    /**
     * {@inheritDoc}
     * Uses a {@link ListIterator} to avoid creation of a new list.
     */
    @Override
    public void replaceChildren(Function<T, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        for (ListIterator<T> it = children.listIterator(); it.hasNext(); ) {
            final T child = it.next();
            final T replacement = mapper.apply(child);
            if (replacement != null && replacement != child) {
                it.set(replacement);
            }
        }
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
     * {@return whether this node is equal to another}
     *
     * @param other the other node
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        return getClass() == other.getClass() && equalsTree((T) other);
    }
}
