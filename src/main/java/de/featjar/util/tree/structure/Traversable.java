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

import de.featjar.util.tree.Trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * An object that can be traversed, presumably a node in a tree.
 * Nodes are defined recursively. For an example usage, see {@link LabeledTree}.
 *
 * @param <T> type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Traversable<T extends Traversable<T>> {
    /**
     * {@return the children of this node}
     */
    List<? extends T> getChildren();

    /**
     * Sets the children of this node.
     *
     * @param children the new children
     */
    void setChildren(List<? extends T> children);

    /**
     * {@return whether this node has any children}
     */
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    /**
     * {@return how many children this node has}
     */
    default int getChildrenCount() {
        return getChildren().size();
    }

    /**
     * {@return the first child of this node, if any}
     */
    default Optional<T> getFirstChild() {
        if (getChildren().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getChildren().get(0));
    }

    /**
     * {@return the last child of this node, if any}
     */
    default Optional<T> getLastChild() {
        if (getChildren().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getChildren().get(getChildrenCount() - 1));
    }

    /**
     * Replaces each child of this node with a new child.
     * If the {@code mapper} returns null or the old child, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child onto a new child
     */
    default void replaceChildren(Function<T, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        final List<? extends T> oldChildren = getChildren();
        if (!oldChildren.isEmpty()) {
            final List<T> newChildren = new ArrayList<>(oldChildren.size());
            boolean modified = false;
            for (final T child : oldChildren) {
                final T replacement = mapper.apply(child);
                if (replacement != null && replacement != child) {
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

    /**
     * Replaces each child of this node with a list of new children.
     * If the {@code mapper} returns null, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child onto a list of new children
     */
    default void flatReplaceChildren(Function<T, List<? extends T>> mapper) {
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

    /**
     * Clones this node (not its children).
     * For deep cloning, use {@link #cloneTree()}.
     *
     * @return a shallow clone of this node
     */
    Traversable<T> cloneNode();

    /**
     * Clones this node (and its children).
     * Relies on {@link #cloneNode()}.
     *
     * @return a deep clone of this node
     */
    @SuppressWarnings("unchecked")
    default T cloneTree() {
        return Trees.clone((T) this);
    }

    /**
     * Tests whether two nodes (not their children) are equal.
     * For deep cloning, use {@link #equalsTree(Traversable)}.
     *
     * @param other the other node
     * @return whether this node is shallowly equal to the other node
     */
    boolean equalsNode(T other);

    /**
     * Tests whether two nodes (and their children) are equal.
     * Relies on {@link #equalsNode(Traversable)}.

     * @param other the other node
     * @return whether this node is deeply equal to the other node
     */
    @SuppressWarnings("unchecked")
    default boolean equalsTree(T other) {
        return Trees.equals((T) this, other);
    }
}
