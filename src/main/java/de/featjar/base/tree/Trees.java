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
package de.featjar.base.tree;

import de.featjar.base.tree.structure.Traversable;
import de.featjar.base.tree.visitor.InOrderTreeVisitor;
import de.featjar.base.tree.visitor.TreeVisitor;
import de.featjar.base.tree.visitor.TreeVisitor.TraversalAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Traverses and manipulates trees.
 *
 * @author Sebastian Krieter
 */
public class Trees {

    /**
     * Thrown when a visitor requests the {@link TreeVisitor.TraversalAction#FAIL} action.
     */
    public static class VisitorFailException extends Exception {
    }

    /**
     * Traverses a tree using depth-first search, allowing for pre-, in-, and postorder traversal.
     *
     * @param node the starting node of the tree
     * @param visitor the visitor
     * @return the optional result from the visitor
     * @param <R> the type of result
     * @param <T> the type of tree
     */
    public static <R, T extends Traversable<?>> Optional<R> traverse(T node, InOrderTreeVisitor<R, T> visitor) {
        visitor.reset();
        try {
            depthFirstSearch(node, visitor);
            return visitor.getResult();
        } catch (final VisitorFailException e) {
            return Optional.empty();
        }
    }

    /**
     * Traverses a tree using depth-first search, allowing for pre- and postorder traversal.
     * This is equivalent to using a trivial {@link InOrderTreeVisitor}, but more efficient.
     *
     * @param node the starting node of the tree
     * @param visitor the visitor
     * @return the optional result from the visitor
     * @param <R> the type of result
     * @param <T> the type of tree
     */
    public static <R, T extends Traversable<?>> Optional<R> traverse(T node, TreeVisitor<R, T> visitor) {
        visitor.reset();
        try {
            depthFirstSearch(node, visitor);
            return visitor.getResult();
        } catch (final VisitorFailException e) {
            return Optional.empty();
        }
    }

    /**
     * Creates a preorder stream of the descendents of a tree.
     * Is more efficient than {@link #traverse(Traversable, TreeVisitor)}, but lacks support for {@link TraversalAction}.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> Stream<T> preOrderStream(T node) {
        return StreamSupport.stream(new PreOrderSpliterator<>(node), false);
    }

    /**
     * Creates a postorder stream of the descendents of a tree.
     * Is more efficient than {@link #traverse(Traversable, TreeVisitor)}, but lacks support for {@link TraversalAction}.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> Stream<T> postOrderStream(T node) {
        return StreamSupport.stream(new PostOrderSpliterator<>(node), false);
    }

    /**
     * Creates a level-order stream of the descendents of a tree.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> Stream<T> levelOrderStream(T node) {
        return StreamSupport.stream(new LevelOrderSpliterator<>(node), false);
    }

    /**
     * Creates a parallel stream of the descendents of a tree.
     * Does not make any guarantees regarding the order of the descendents.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> Stream<T> parallelStream(T node) {
        return StreamSupport.stream(new ParallelSpliterator<>(node), true);
    }

    /**
     * Tests whether two nodes (and their children) are equal.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return whether the first node is deeply equal to the second node
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> boolean equals(T node1, T node2) {
        if (node1 == node2) {
            return true;
        }
        if ((node1 == null) || (node2 == null)) {
            return false;
        }
        final LinkedList<T> stack1 = new LinkedList<>();
        final LinkedList<T> stack2 = new LinkedList<>();
        stack1.push(node1);
        stack2.push(node2);
        while (!stack1.isEmpty()) {
            final T currentNode1 = stack1.pop();
            final T currentNode2 = stack2.pop();

            if (currentNode1 != currentNode2) {
                if ((currentNode1 == null) || (currentNode2 == null)) {
                    return false;
                } else {
                    if (!currentNode1.equalsNode(currentNode2)) {
                        return false;
                    }
                    stack1.addAll(0, currentNode1.getChildren());
                    stack2.addAll(0, currentNode2.getChildren());
                }
            }
        }
        return true;
    }

    /**
     * Clones a node (and its children).
     *
     * @param root the node
     * @return a deep clone of the node
     * @param <T> the type of tree
     */
    @SuppressWarnings("unchecked")
    public static <T extends Traversable<T>> T clone(T root) {
        if (root == null) {
            return null;
        }

        final ArrayList<T> path = new ArrayList<>();
        final LinkedList<StackEntry<T>> stack = new LinkedList<>();
        stack.push(new StackEntry<>(root));

        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.peek();
            final T node = entry.node;
            if (entry.remainingChildren == null) {
                path.add((T) node.cloneNode());
                entry.remainingChildren = new LinkedList<>(node.getChildren());
            }
            if (!entry.remainingChildren.isEmpty()) {
                stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
            } else {
                final int childrenCount = node.getChildren().size();
                if (childrenCount > 0) {
                    final List<T> subList = path.subList(path.size() - childrenCount, path.size());
                    path.get(path.size() - (childrenCount + 1)).setChildren(subList);
                    subList.clear();
                }
                stack.pop();
            }
        }
        return path.get(0);
    }

    /**
     * Sorts a node (and its children).
     *
     * @param root the node
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> void sort(T root) {
        sort(root, Comparator.comparing(T::toString));
    }

    /**
     * Sorts a node (and its children).
     *
     * @param root the node
     * @param comparator comparator used for sorting
     * @param <T> the type of tree
     */
    public static <T extends Traversable<T>> void sort(T root, Comparator<T> comparator) {
        final LinkedList<StackEntry<T>> stack = new LinkedList<>();
        stack.push(new StackEntry<>(root));

        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.peek();
            final T node = entry.node;
            if (entry.remainingChildren == null) {
                entry.remainingChildren = new LinkedList<>(node.getChildren());
            }
            if (!entry.remainingChildren.isEmpty()) {
                stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
            } else {
                final ArrayList<T> children = new ArrayList<>(node.getChildren());
                children.sort(comparator);
                node.setChildren(children);
                stack.pop();
            }
        }
    }

    private static class StackEntry<T> {
        private final T node;
        private List<T> remainingChildren;

        public StackEntry(T node) {
            this.node = node;
        }
    }

    private static class PreOrderSpliterator<T extends Traversable<T>> implements Spliterator<T> {


        final LinkedList<T> stack = new LinkedList<>();

        public PreOrderSpliterator(T node) {
            if (node != null) {
                stack.addFirst(node);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            } else {
                final T node = stack.removeFirst();
                consumer.accept(node);
                stack.addAll(0, node.getChildren());
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class PostOrderSpliterator<T extends Traversable<T>> implements Spliterator<T> {

        final LinkedList<StackEntry<T>> stack = new LinkedList<>();

        public PostOrderSpliterator(T node) {
            if (node != null) {
                stack.push(new StackEntry<>(node));
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            }
            while (!stack.isEmpty()) {
                final StackEntry<T> entry = stack.peek();
                if (entry.remainingChildren == null) {
                    entry.remainingChildren = new LinkedList<>(entry.node.getChildren());
                }
                if (!entry.remainingChildren.isEmpty()) {
                    stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
                } else {
                    consumer.accept(entry.node);
                    stack.pop();
                }
            }
            return true;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class LevelOrderSpliterator<T extends Traversable<T>> implements Spliterator<T> {

        final LinkedList<T> queue = new LinkedList<>();

        public LevelOrderSpliterator(T node) {
            if (node != null) {
                queue.addFirst(node);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (queue.isEmpty()) {
                return false;
            } else {
                final T node = queue.removeFirst();
                consumer.accept(node);
                queue.addAll(node.getChildren());
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class ParallelSpliterator<T extends Traversable<T>> implements Spliterator<T> {

        final LinkedList<T> stack = new LinkedList<>();

        public ParallelSpliterator(T node) {
            if (node != null) {
                stack.push(node);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            } else {
                final T node = stack.pop();
                consumer.accept(node);
                stack.addAll(0, node.getChildren());
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            if (!stack.isEmpty()) {
                return new ParallelSpliterator<>(stack.pop());
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Traversable<?>> void depthFirstSearch(T node, InOrderTreeVisitor<?, T> visitor)
            throws VisitorFailException {
        if (node == null) {
            return;
        }
        final ArrayList<T> path = new ArrayList<>();
        final List<T> unmodifiablePath = Collections.unmodifiableList(path);

        final ArrayDeque<StackEntry<T>> stack = new ArrayDeque<>();
        stack.addLast(new StackEntry<>(node));
        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.getLast();
            if (entry.remainingChildren == null) {
                path.add(entry.node);
                final TraversalAction traversalAction = visitor.firstVisit(unmodifiablePath);
                switch (traversalAction) {
                    case CONTINUE:
                        entry.remainingChildren =
                                new LinkedList<>((Collection<? extends T>) entry.node.getChildren());
                        break;
                    case SKIP_CHILDREN:
                        entry.remainingChildren = Collections.emptyList();
                        break;
                    case SKIP_ALL:
                        return;
                    case FAIL:
                        throw new VisitorFailException();
                    default:
                        throw new IllegalStateException(String.valueOf(traversalAction));
                }
            } else {
                final TraversalAction traversalAction = visitor.visit(unmodifiablePath);
                switch (traversalAction) {
                    case CONTINUE:
                        break;
                    case SKIP_CHILDREN:
                        stack.removeLast();
                        path.remove(path.size() - 1);
                        continue;
                    case SKIP_ALL:
                        return;
                    case FAIL:
                        throw new VisitorFailException();
                    default:
                        throw new IllegalStateException(String.valueOf(traversalAction));
                }
            }

            if (!entry.remainingChildren.isEmpty()) {
                stack.addLast(new StackEntry<>(entry.remainingChildren.remove(0)));
            } else {
                final TraversalAction traversalAction = visitor.lastVisit(unmodifiablePath);
                switch (traversalAction) {
                    case CONTINUE:
                    case SKIP_CHILDREN:
                        break;
                    case SKIP_ALL:
                        return;
                    case FAIL:
                        throw new VisitorFailException();
                    default:
                        throw new IllegalStateException(String.valueOf(traversalAction));
                }
                stack.removeLast();
                path.remove(path.size() - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Traversable<?>> void depthFirstSearch(T node, TreeVisitor<?, T> visitor) throws VisitorFailException {
        if (node != null) {
            final ArrayList<T> path = new ArrayList<>();
            final List<T> unmodifiablePath = Collections.unmodifiableList(path);

            final ArrayDeque<T> stack = new ArrayDeque<>();
            stack.addLast(node);
            while (!stack.isEmpty()) {
                final T curNode = stack.getLast();
                if (path.isEmpty() || (curNode != path.get(path.size() - 1))) {
                    path.add(curNode);
                    final TraversalAction traversalAction = visitor.firstVisit(unmodifiablePath);
                    switch (traversalAction) {
                        case CONTINUE:
                            final Collection<? extends T> children = (Collection<? extends T>) curNode.getChildren();
                            children.forEach(stack::addFirst);
                            children.forEach(c -> stack.addLast(stack.removeFirst()));
                            break;
                        case SKIP_CHILDREN:
                            break;
                        case SKIP_ALL:
                            return;
                        case FAIL:
                            throw new VisitorFailException();
                        default:
                            throw new IllegalStateException(String.valueOf(traversalAction));
                    }
                } else {
                    final TraversalAction traversalAction = visitor.lastVisit(unmodifiablePath);
                    switch (traversalAction) {
                        case CONTINUE:
                        case SKIP_CHILDREN:
                            break;
                        case SKIP_ALL:
                            return;
                        case FAIL:
                            throw new VisitorFailException();
                        default:
                            throw new IllegalStateException(String.valueOf(traversalAction));
                    }
                    stack.removeLast();
                    path.remove(path.size() - 1);
                }
            }
        }
    }
}
