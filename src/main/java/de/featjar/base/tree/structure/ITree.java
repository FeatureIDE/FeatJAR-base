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
import de.featjar.base.data.Sets;
import de.featjar.base.env.IBrowsable;
import de.featjar.base.io.graphviz.GraphVizTreeFormat;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.IInOrderTreeVisitor;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.base.tree.visitor.ITreeVisitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A tree of nodes that can be traversed.
 * Nodes are defined recursively.
 * For an example usage, see {@link LabeledTree}.
 * It is not supported to store children of a type different from the implementing type.
 * For this use case, consider a multi-level tree, where a {@link ALeafNode} references another {@link ITree}.
 * The parentage of nodes is not specified, so a node may occur in several nodes.
 * Thus, it is possible to store any directed acyclic graph.
 * For a directed acyclic graph with at most one parent per node, use {@link ARootedTree}.
 * Note that most consumers of {@link ITree} assume it to be acyclic.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
@SuppressWarnings("unchecked")
public interface ITree<T extends ITree<T>> extends IBrowsable<GraphVizTreeFormat<T>> {
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
     * The range is guaranteed to be respected for all mutating operations based on {@link #setChildren(List)}.
     * To guarantee that the range is respected at all times, call {@link #setChildren(List)} in the constructor.
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

    default void assertChildrenCountInRange(int newChildrenCount, Range range) {
        if (!range.test(newChildrenCount))
            throw new IllegalArgumentException(
                    String.format("attempted to set %d children, but expected one in %s", newChildrenCount, range));
    }

    default void assertChildrenCountInRange(int newChildrenCount) {
        assertChildrenCountInRange(newChildrenCount, getChildrenCountRange());
    }

    default void assertChildrenValidator(List<? extends T> children) {
        if (!children.stream().allMatch(getChildrenValidator()))
            throw new IllegalArgumentException(String.format("child %s is invalid",
                    children.stream().filter(c -> !getChildrenValidator().test(c)).findFirst().orElse(null)));
    }

    default void assertChildrenValidator(T child) {
        if (!getChildrenValidator().test(child))
            throw new IllegalArgumentException("child did not pass validation");
    }

    /**
     * {@return the n-th child of this node, if any}
     *
     * @param idx the index
     */
    default Optional<T> getChild(int idx) {
        if (idx < 0 || idx >= getChildren().size())
            return Optional.empty();
        return Optional.ofNullable(getChildren().get(idx));
    }

    /**
     * {@return the first child of this node, if any}
     */
    default Optional<T> getFirstChild() {
        return getChild(0);
    }

    /**
     * {@return the last child of this node, if any}
     */
    default Optional<T> getLastChild() {
        return getChild(getChildrenCount() - 1);
    }

    /**
     * {@return the index of the given node in the list of children, if any}
     *
     * @param node the node
     */
    default Optional<Integer> getChildIndex(T node) {
        return Result.indexToOptional(getChildren().indexOf(node));
    }

    /**
     * {@return whether the given node is a child of this node}
     *
     * @param child the node
     */
    default boolean hasChild(T child) {
        return getChildIndex(child).isPresent();
    }

    /**
     * Adds a new child at a given position.
     * If the position is out of bounds, add the new child as the last child.
     *
     * @param index the new position
     * @param newChild the new child
     */
    default void addChild(int index, T newChild) {
        assertChildrenCountInRange(getChildren().size() + 1);
        assertChildrenValidator(newChild);
        List<T> newChildren = new ArrayList<>(getChildren());
        if (index > getChildrenCount()) {
            newChildren.add(newChild);
        } else {
            newChildren.add(index, newChild);
        }
        setChildren(newChildren);
    }

    /**
     * Adds a new child as the last child.
     *
     * @param newChild the new child
     */
    default void addChild(T newChild) {
        assertChildrenCountInRange(getChildren().size() + 1);
        assertChildrenValidator(newChild);
        List<T> newChildren = new ArrayList<>(getChildren());
        newChildren.add(newChild);
        setChildren(newChildren);
    }

    /**
     * Removes a child.
     *
     * @param child the child to be removed
     * @throws NoSuchElementException if the given old node is not a child
     */
    default void removeChild(T child) {
        assertChildrenCountInRange(getChildren().size() - 1);
        List<T> newChildren = new ArrayList<>(getChildren());
        if (!newChildren.remove(child)) {
            throw new NoSuchElementException();
        }
        setChildren(newChildren);
    }

    /**
     * Removes the child at a given position.
     *
     * @param index the position to be removed
     * @return the removed child
     * @throws IndexOutOfBoundsException if the given index is out of bounds
     */
    default T removeChild(int index) {
        assertChildrenCountInRange(getChildren().size() - 1);
        List<T> newChildren = new ArrayList<>(getChildren());
        T t = newChildren.remove(index);
        setChildren(newChildren);
        return t;
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
     * @param idx      the index
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
    ITree<T> cloneNode();

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
     * For deep cloning, use {@link #equalsTree(ITree)}.
     *
     * @param other the other node
     * @return whether this node is shallowly equal to the other node
     */
    boolean equalsNode(T other);

    /**
     * Tests whether two nodes (and their children) are equal.
     * Relies on {@link #equalsNode(ITree)}.
     *
     * @param other the other node
     * @return whether this node is deeply equal to the other node
     */
    default boolean equalsTree(T other) {
        return Trees.equals((T) this, other);
    }

    /**
     * {@return the hash code of this node (not its children)}
     * For deep hash code calculation, use {@link #hashCodeTree()}.
     */
    int hashCodeNode();

    /**
     * {@return the hash code of this node (and its children)}
     * Relies on {@link #hashCodeNode()}.
     */
    default int hashCodeTree() {
        int hashCode = hashCodeNode();
        for (T child : getChildren()) {
            hashCode += (hashCode * 37) + child.hashCodeTree();
        }
        return hashCode;
    }

    /**
     * Traverses the tree using depth-first search, allowing for pre-, in-, and postorder traversal.
     * Only accepts tree visitors that operate on T.
     * For more general visitors, use {@link Trees#traverse(ITree, IInOrderTreeVisitor)} instead.
     *
     * @param treeVisitor the tree visitor
     * @param <R>         the type of result
     * @return the result from the visitor
     */
    default <R> Result<R> traverse(IInOrderTreeVisitor<T, R> treeVisitor) {
        return Trees.traverse((T) this, treeVisitor);
    }

    /**
     * Traverses the tree using depth-first search, allowing for pre- and postorder traversal.
     * Only accepts tree visitors that operate on T.
     * For more general visitors, use {@link Trees#traverse(ITree, ITreeVisitor)} instead.
     *
     * @param treeVisitor the tree visitor
     * @param <R>         the type of result
     * @return the result from the visitor
     */
    default <R> Result<R> traverse(ITreeVisitor<T, R> treeVisitor) {
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
    default LinkedHashSet<? extends T> getDescendants() {
        return parallelStream().collect(Sets.toSet());
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

    class Entry<T extends ITree<T>, U extends T> implements Function<T, U> {
        protected final U defaultValue;
        protected int index = -1;

        public Entry() {
            this(null);
        }

        public Entry(U defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Optional<U> getDefaultValue() {
            return Optional.ofNullable(defaultValue);
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public <V> V get(List<V> list) {
            if (index < 0 || index >= list.size())
                throw new IllegalArgumentException();
            return list.get(index);
        }

        @SuppressWarnings("unchecked")
        public U get(T tree) {
            return (U) tree.getChild(index).orElse(defaultValue);
        }

        public U apply(T tree) {
            return get(tree);
        }

        public void set(T tree, U child) {
            Objects.requireNonNull(child);
            while (tree.getChildrenCount() <= index)
                tree.addChild(null);
            tree.replaceChild(index, child);
        }
    }

    @Override
    default Result<URI> getBrowseURI(GraphVizTreeFormat<T> argument) {
        Result<String> dot = argument.serialize((T) this);
        if (dot.isEmpty())
            return Result.empty(dot);
        try {
            return Result.of(new URI("https", "edotor.net", "", "engine=dot", dot.get()));
        } catch (URISyntaxException e) {
            return Result.empty(e);
        }
    }
}
