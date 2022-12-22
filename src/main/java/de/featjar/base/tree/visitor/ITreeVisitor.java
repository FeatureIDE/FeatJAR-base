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
package de.featjar.base.tree.visitor;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ITree;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Visits each node of a tree in a depth-first search.
 * The actual traversal algorithm is {@link Trees#traverse(ITree, ITreeVisitor)}.
 *
 * @param <T> the type of tree
 * @param <U> the type of result
 * @author Sebastian Krieter
 */
public interface ITreeVisitor<T extends ITree<?>, U> {
    /**
     * All possible actions a traversal can take after visiting a tree node.
     */
    enum TraversalAction {
        /**
         * Continue normally.
         * That is, traverse all children of the visited node.
         */
        CONTINUE,
        /**
         * Skip all children of the visited node.
         */
        SKIP_CHILDREN,
        /**
         * Skip all nodes left to be visited.
         * That is, stop the traversal, but still return a result, if already determined.
         */
        SKIP_ALL,
        /**
         * Signal that the traversal has failed.
         * That is, stop the traversal and do not return a result.
         */
        FAIL
    }

    /**
     * {@return a problem with the the node about to be visited, if any}
     * If a problem is returned, the traversal algorithm will fail.
     *
     * @param path the path to the visited node
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default Optional<Problem> nodeValidator(List<T> path) {
        return Optional.empty();
    }

    /**
     * {@return a condition that is {@code true} if visiting the root implies it satisfying a given predicate}
     *
     * @param path the path to the visited node
     * @param predicate the predicate
     */
    default Optional<Problem> rootValidator(List<T> path, Function<T, Boolean> predicate, String message) {
        return path.size() != 1 || predicate.apply(path.get(0))
                ? Optional.empty()
                : Optional.of(new Problem(message, Problem.Severity.ERROR));
    }

    /**
     * Visit a node for the first time.
     * Override this to implement preorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction firstVisit(List<T> path) {
        return TraversalAction.CONTINUE;
    }

    /**
     * Visit a node for the last time.
     * Override this to implement postorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction lastVisit(List<T> path) {
        return TraversalAction.CONTINUE;
    }

    /**
     * Resets any internal state of this tree visitor.
     * Should be overridden to allow for reusing this tree visitor instance.
     */
    default void reset() {}

    /**
     * {@return} the result of the traversal, if any
     */
    default Result<U> getResult() {
        return Result.empty();
    }

    /**
     * {@return the currently visited node}
     *
     * @param path the current traversal path, guaranteed to contain at least one node
     */
    default T getCurrentNode(List<T> path) {
        return path.get(path.size() - 1);
    }

    /**
     * {@return the parent of the currently visited node}
     *
     * @param path the current traversal path, guaranteed to contain at least one node
     */
    default Optional<T> getParentNode(List<T> path) {
        return (path.size() > 1) ? Optional.of(path.get(path.size() - 2)) : Optional.empty();
    }
}
