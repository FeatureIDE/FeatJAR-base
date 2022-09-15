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
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.InOrderTreeVisitor;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.base.tree.visitor.TreeVisitor;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An object that can be traversed, presumably a node in a tree.
 * Nodes are defined recursively.
 * For an example usage, see {@link LabeledTree}.
 * It is not supported to store children of a type different from the implementing type.
 * For this use case, consider a multi-level tree, where a {@link LeafNode} references another {@link Traversable}.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
@SuppressWarnings("unchecked")
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
     * {@return a range that specifies the minimum and maximum number of this node's children}
     */
    default Range getChildrenCountRange() {
        return Range.open();
    }

    /**
     * {@return a function that validates this node's children}
     */
    default Predicate<T> getChildrenValidator() {
        return t -> true;
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
     * @param mapper maps an old child with its index onto a new child
     */
    default void replaceChildren(BiFunction<Integer, T, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        final List<? extends T> oldChildren = getChildren();
        if (!oldChildren.isEmpty()) {
            final List<T> newChildren = new ArrayList<>(oldChildren.size());
            boolean modified = false;
            for (int i = 0; i < oldChildren.size(); i++) {
                T child = oldChildren.get(i);
                final T replacement = mapper.apply(i, child);
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
     * Replaces each child of this node with a new child.
     * If the {@code mapper} returns null or the old child, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child onto a new child
     */
    default void replaceChildren(Function<T, ? extends T> mapper) {
        replaceChildren((index, child) -> mapper.apply(child));
    }

    /**
     * Replaces a child with a new child.
     * Does nothing if the old child was not found.
     *
     * @param oldChild the old child
     * @param newChild the new child
     */
    default void replaceChild(T oldChild, T newChild) {
        replaceChildren(child -> child == oldChild ? newChild : null);
    }

    /**
     * Replaces a child at an index with a new child.
     * Does nothing if the index is out of bounds.
     *
     * @param idx the index
     * @param newChild the new child
     */
    default void replaceChild(int idx, T newChild) {
        replaceChildren((index, child) -> index == idx ? newChild : null);
    }

    /**
     * Replaces each child of this node with a list of new children.
     * If the {@code mapper} returns null, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child with its index onto a list of new children
     */
    default void flatReplaceChildren(BiFunction<Integer, T, List<? extends T>> mapper) {
        Objects.requireNonNull(mapper);
        final List<? extends T> oldChildren = getChildren();
        if (!oldChildren.isEmpty()) {
            final ArrayList<T> newChildren = new ArrayList<>(oldChildren.size());
            boolean modified = false;
            for (int i = 0; i < oldChildren.size(); i++) {
                T child = oldChildren.get(i);
                final List<? extends T> replacement = mapper.apply(i, child);
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
     * Replaces each child of this node with a new child.
     * If the {@code mapper} returns null or the old child, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child onto a new child
     */
    default void flatReplaceChildren(Function<T, List<? extends T>> mapper) {
        flatReplaceChildren((index, child) -> mapper.apply(child));
    }

    /**
     * Clones this node (not its children).
     * For deep cloning, use {@link #cloneTree()}.
     *
     * @return a shallow clone of this node
     */
    Traversable<T> cloneNode(); // todo: can this be changed to T?

    /**
     * Clones this node (and its children).
     * Relies on {@link #cloneNode()}.
     *
     * @return a deep clone of this node
     */
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
    default boolean equalsTree(T other) {
        return Trees.equals((T) this, other);
    }

    /**
     * Traverses the tree using depth-first search, allowing for pre-, in-, and postorder traversal.
     * Only accepts tree visitors that operate on T.
     * For more general visitors, use {@link Trees#traverse(Traversable, InOrderTreeVisitor)} instead.
     *
     * @param treeVisitor the tree visitor
     * @return the optional result from the visitor
     * @param <R> the type of result
     */
    default <R> Optional<R> traverse(InOrderTreeVisitor<R, T> treeVisitor) {
        return Trees.traverse((T) this, treeVisitor);
    }

    /**
     * Traverses the tree using depth-first search, allowing for pre- and postorder traversal.
     * Only accepts tree visitors that operate on T.
     * For more general visitors, use {@link Trees#traverse(Traversable, TreeVisitor)} instead.
     *
     * @param treeVisitor the tree visitor
     * @return the optional result from the visitor
     * @param <R> the type of result
     */
    default <R> Optional<R> traverse(TreeVisitor<T, R> treeVisitor) {
        return Trees.traverse((T) this, treeVisitor);
    }

    /**
     * {@return the tree printed as a string}
     */
    default String print() {
        return Trees.traverse(this, new TreePrinter()).orElse("");
    }

    /**
     * {@return a parallel stream of the descendants of this node}
     */
    default Stream<? extends T> parallelStream() {
        return Trees.parallelStream((T) this);
    }

    /**
     * {@return a preorder stream of the descendants of this node}
     */
    default Stream<? extends T> preOrderStream() {
        return Trees.preOrderStream((T) this);
    }

    /**
     * {@return a postorder stream of the descendants of this node}
     */
    default Stream<? extends T> postOrderStream() {
        return Trees.postOrderStream((T) this);
    }

    /**
     * {@return a lever-order stream of the descendants of this node}
     */
    default Stream<? extends T> levelOrderStream() {
        return Trees.levelOrderStream((T) this);
    }

    /**
     * {@return the descendants of this node}
     */
    default Set<? extends T> getDescendants() {
        return parallelStream().collect(Collectors.toSet());
    }

    /**
     * {@return a preorder list of the descendants of this node}
     */
    default List<? extends T> getDescendantsAsPreOrder() {
        return preOrderStream().collect(Collectors.toList());
    }

    /**
     * {@return a postorder list of the descendants of this node}
     */
    default List<? extends T> getDescendantsAsPostOrder() {
        return postOrderStream().collect(Collectors.toList());
    }

    /**
     * {@return a level-order list of the descendants of this node}
     */
    default List<? extends T> getDescendantsAsLevelOrder() {
        return levelOrderStream().collect(Collectors.toList());
    }

    /**
     * Sorts this node (and its children).
     */
    default void sort() {
        Trees.sort((T) this);
    }

    /**
     * Sorts this node (and its children).
     *
     * @param comparator comparator used for sorting
     */
    default void sort(Comparator<T> comparator) {
        Trees.sort((T) this, comparator);
    }
}
