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
package de.featjar.util.tree.visitor;

import de.featjar.util.tree.Trees;
import de.featjar.util.tree.structure.Traversable;
import java.util.List;
import java.util.Optional;

/**
 * Visits each node of a tree in a depth-first search.
 * The actual traversal algorithm is implemented in {@link Trees#dfsPrePost(Traversable, TreeVisitor)}.
 *
 * @param <R> type of result
 * @param <T> type of tree
 * @author Sebastian Krieter
 */
public interface TreeVisitor<R, T extends Traversable<?>> {

    enum TraversalAction {
        CONTINUE,
        SKIP_CHILDREN,
        SKIP_ALL,
        FAIL
    }

    static <T> T getCurrentNode(List<T> path) {
        return path.get(path.size() - 1);
    }

    static <T> T getParentNode(List<T> path) {
        return (path.size() > 1) ? path.get(path.size() - 2) : null;
    }

    /**
     * Called when a node is visited the first time.
     * Override this to implement a preorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction firstVisit(List<T> path) {
        return TraversalAction.CONTINUE;
    }

    /**
     * Called when a node is visited the last time.
     * Override this to implement a postorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction lastVisit(List<T> path) {
        return TraversalAction.CONTINUE;
    }

    default void reset() {}

    default Optional<R> getResult() {
        return Optional.empty();
    }
}
