/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util Lib.
 * 
 * Util Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.tree;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.spldev.util.tree.structure.*;
import org.spldev.util.tree.visitor.*;
import org.spldev.util.tree.visitor.TreeVisitor.*;

/**
 * Convenience class that implements some static methods to manipulate and
 * traverse nodes.
 *
 * @author Sebastian Krieter
 */
public final class Trees {

	private static class VisitorFailException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	private Trees() {
	}

	/**
	 * Traverses the tree starting from the given node in using a depth-first
	 * search.
	 * 
	 * @param <T>     the type of the Tree
	 * @param <R>     the return type of the TreeVisitor
	 * @param node    the starting node
	 * @param visitor the TreeVisitor
	 * @return the optional result from the tree visitor
	 */
	public static <R, T extends Tree<?>> Optional<R> traverse(T node, DfsVisitor<R, T> visitor) {
		visitor.reset();
		try {
			dfsComplete(node, visitor);
			return Optional.ofNullable(visitor.getResult());
		} catch (final VisitorFailException e) {
			return Optional.empty();
		}
	}

	public static <R, T extends Tree<?>> Optional<R> traverse(T node, TreeVisitor<R, T> visitor) {
		visitor.reset();
		try {
			dfsPrePost(node, visitor);
			return Optional.ofNullable(visitor.getResult());
		} catch (final VisitorFailException e) {
			return Optional.empty();
		}
	}

	public static <T extends Tree<T>> List<T> getPreOrderList(T node) {
		return preOrderStream(node).collect(Collectors.toList());
	}

	public static <T extends Tree<T>> List<T> getPostOrderList(T node) {
		return postOrderStream(node).collect(Collectors.toList());
	}

	public static <T extends Tree<T>> Stream<T> paralellStream(T node) {
		return StreamSupport.stream(new ParalellSpliterator<>(node), true);
	}

	public static <T extends X, X extends Tree<X>> Stream<X> preOrderStream(T node) {
		return StreamSupport.stream(new PreOrderSpliterator<>(node), false);
	}

	public static <T extends Tree<T>> Stream<T> postOrderStream(T node) {
		return StreamSupport.stream(new PostOrderSpliterator<>(node), false);
	}

	private static class ParalellSpliterator<T extends Tree<T>> implements Spliterator<T> {

		final LinkedList<T> stack = new LinkedList<>();

		public ParalellSpliterator(T node) {
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
				return new ParalellSpliterator<>(stack.pop());
			} else {
				return null;
			}
		}
	}

	private static class PreOrderSpliterator<T extends X, X extends Tree<X>> implements Spliterator<X> {

		final LinkedList<X> stack = new LinkedList<>();

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
		public boolean tryAdvance(Consumer<? super X> consumer) {
			if (stack.isEmpty()) {
				return false;
			} else {
				final X node = stack.removeFirst();
				consumer.accept(node);
				stack.addAll(0, node.getChildren());
				return true;
			}
		}

		@Override
		public Spliterator<X> trySplit() {
			return null;
		}
	}

	private static class StackEntry<T> {
		private T node;
		private List<T> remainingChildren;

		public StackEntry(T node) {
			this.node = node;
		}
	}

	private static class PostOrderSpliterator<T extends Tree<T>> implements Spliterator<T> {

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

	@SuppressWarnings("unchecked")
	private static <X extends Tree<?>, T extends X> void dfsComplete(T node, DfsVisitor<?, X> visitor)
		throws VisitorFailException {
		if (node != null) {
			final ArrayList<T> path = new ArrayList<>();
			final List<X> unmodifiablePath = Collections.unmodifiableList(path);

			final ArrayDeque<StackEntry<T>> stack = new ArrayDeque<>();
			stack.addLast(new StackEntry<>(node));
			loop: while (!stack.isEmpty()) {
				final StackEntry<T> entry = stack.getLast();
				if (entry.remainingChildren == null) {
					path.add(entry.node);
					final VisitorResult visitorResult = visitor.firstVisit(unmodifiablePath);
					switch (visitorResult) {
					case Continue:
						entry.remainingChildren = new LinkedList<>((Collection<? extends T>) entry.node.getChildren());
						break;
					case SkipChildren:
						entry.remainingChildren = Collections.emptyList();
						break;
					case SkipAll:
						return;
					case Fail:
						throw new VisitorFailException();
					default:
						throw new IllegalStateException(String.valueOf(visitorResult));
					}
				} else {
					final VisitorResult visitorResult = visitor.visit(unmodifiablePath);
					switch (visitorResult) {
					case Continue:
						break;
					case SkipChildren:
						stack.removeLast();
						path.remove(path.size() - 1);
						continue loop;
					case SkipAll:
						return;
					case Fail:
						throw new VisitorFailException();
					default:
						throw new IllegalStateException(String.valueOf(visitorResult));
					}
				}

				if (!entry.remainingChildren.isEmpty()) {
					stack.addLast(new StackEntry<>(entry.remainingChildren.remove(0)));
				} else {
					final VisitorResult visitorResult = visitor.lastVisit(unmodifiablePath);
					switch (visitorResult) {
					case Continue:
					case SkipChildren:
						break;
					case SkipAll:
						return;
					case Fail:
						throw new VisitorFailException();
					default:
						throw new IllegalStateException(String.valueOf(visitorResult));
					}
					stack.removeLast();
					path.remove(path.size() - 1);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Tree<?>> void dfsPrePost(T node, TreeVisitor<?, T> visitor)
		throws VisitorFailException {
		if (node != null) {
			final ArrayList<T> path = new ArrayList<>();
			final List<T> unmodifiablePath = Collections.unmodifiableList(path);

			final ArrayDeque<T> stack = new ArrayDeque<>();
			stack.addLast(node);
			while (!stack.isEmpty()) {
				final T curNode = stack.getLast();
				if (path.isEmpty() || (curNode != path.get(path.size() - 1))) {
					path.add(curNode);
					final VisitorResult visitorResult = visitor.firstVisit(unmodifiablePath);
					switch (visitorResult) {
					case Continue:
						final Collection<? extends T> children = (Collection<? extends T>) curNode.getChildren();
						children.forEach(stack::addFirst);
						children.forEach(c -> stack.addLast(stack.removeFirst()));
						break;
					case SkipChildren:
						break;
					case SkipAll:
						return;
					case Fail:
						throw new VisitorFailException();
					default:
						throw new IllegalStateException(String.valueOf(visitorResult));
					}
				} else {
					final VisitorResult visitorResult = visitor.lastVisit(unmodifiablePath);
					switch (visitorResult) {
					case Continue:
					case SkipChildren:
						break;
					case SkipAll:
						return;
					case Fail:
						throw new VisitorFailException();
					default:
						throw new IllegalStateException(String.valueOf(visitorResult));
					}
					stack.removeLast();
					path.remove(path.size() - 1);
				}
			}
		}
	}

	public static <T extends Tree<T>> boolean equals(T node1, T node2) {
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

	@SuppressWarnings("unchecked")
	public static <X extends T, T extends Tree<T>> X cloneTree(X root) {
		if (root == null) {
			return null;
		}

		final ArrayList<X> path = new ArrayList<>();
		final LinkedList<StackEntry<T>> stack = new LinkedList<>();
		stack.push(new StackEntry<>(root));

		while (!stack.isEmpty()) {
			final StackEntry<T> entry = stack.peek();
			final T node = entry.node;
			if (entry.remainingChildren == null) {
				path.add((X) node.cloneNode());
				entry.remainingChildren = new LinkedList<>(node.getChildren());
			}
			if (!entry.remainingChildren.isEmpty()) {
				stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
			} else {
				final int childrenCount = node.getChildren().size();
				if (childrenCount > 0) {
					final List<X> subList = path.subList(path.size() - childrenCount, path.size());
					path.get(path.size() - (childrenCount + 1)).setChildren(subList);
					subList.clear();
				}
				stack.pop();
			}
		}
		return path.get(0);
	}

}
