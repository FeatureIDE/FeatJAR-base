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

import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A traversable tree of nodes.
 * Primary implementation of {@link Traversable} that uses {@link ArrayList} to store children.
 * The terms "tree" and "node" are interchangeable.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class Tree<T extends Traversable<T>> implements Traversable<T> {
    /**
     * The children of this node.
     */
    private final List<T> children = new ArrayList<>();

    /**
     * {@inheritDoc}
     * The returned object cannot be modified.
     */
    @Override
    public List<? extends T> getChildren() {
        return Collections.unmodifiableList(children);
    }

    protected void assertChildrenCountInRange(int newChildrenCount, Range range) {
        if (!range.test(newChildrenCount))
            throw new IllegalArgumentException(
                    String.format("attempted to set %d children, but expected one in %s", newChildrenCount, range));
    }

    protected void assertChildrenCountInRange(int newChildrenCount) {
        assertChildrenCountInRange(newChildrenCount, getChildrenCountRange());
    }

    protected void assertChildrenValidator(List<? extends T> children) {
        if (!children.stream().allMatch(getChildrenValidator()))
            throw new IllegalArgumentException(String.format("child %s is invalid",
                    children.stream().filter(c -> !getChildrenValidator().test(c)).findFirst().get()));
    }

    protected void assertChildrenValidator(T child) {
        if (!getChildrenValidator().test(child))
            throw new IllegalArgumentException("child did not pass validation");
    }

    /**
     * {@inheritDoc}
     * The given list is copied.
     */
    @Override
    public void setChildren(List<? extends T> children) {
        Objects.requireNonNull(children);
        assertChildrenCountInRange(children.size());
        assertChildrenValidator(children);
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
        assertChildrenCountInRange(children.size() + 1);
        assertChildrenValidator(newChild);
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
        assertChildrenCountInRange(children.size() + 1);
        assertChildrenValidator(newChild);
        children.add(newChild);
    }

    /**
     * Removes a child.
     *
     * @param child the child to be removed
     * @throws NoSuchElementException if the given old node is not a child
     */
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
    public T removeChild(int index) {
        assertChildrenCountInRange(children.size() - 1);
        return children.remove(index);
    }

    /**
     * {@inheritDoc}
     * Uses a {@link ListIterator} to avoid creation of a new list.
     */
    @Override
    public void replaceChildren(BiFunction<Integer, T, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        for (ListIterator<T> it = children.listIterator(); it.hasNext(); ) {
            final int idx = it.nextIndex();
            final T child = it.next();
            final T replacement = mapper.apply(idx, child);
            if (replacement != null && replacement != child) {
                assertChildrenValidator(replacement);
                it.set(replacement);
            }
        }
    }

    /**
     * Replaces a child with a new child.

     * @param oldChild the old child
     * @param newChild the new child
     * @throws NoSuchElementException if the given old node is not a child
     */
    @Override
    public void replaceChild(T oldChild, T newChild) {
        final int index = children.indexOf(oldChild);
        if (index == -1)
            throw new NoSuchElementException();
        assertChildrenValidator(newChild);
        children.set(index, newChild);
    }

    /**
     * Replaces a child at an index with a new child.
     * Does nothing if the index is out of bounds.
     *
     * @param idx the index
     * @param newChild the new child
     */
    @Override
    public void replaceChild(int idx, T newChild) {
        if (idx < 0 || idx > getChildrenCount())
            throw new NoSuchElementException();
        assertChildrenValidator(newChild);
        children.set(idx, newChild);
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
