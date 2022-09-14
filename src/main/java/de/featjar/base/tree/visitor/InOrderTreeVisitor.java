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

import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.Traversable;
import java.util.List;

/**
 * Visits each node of a tree in a depth-first search.
 * Compared with {@link TreeVisitor}, also allows for inorder traversal.
 * The actual traversal algorithm is {@link Trees#traverse(Traversable, InOrderTreeVisitor)}.
 *
 * @param <R> the type of result
 * @param <T> the type of tree
 * @author Sebastian Krieter
 */
public interface InOrderTreeVisitor<R, T extends Traversable<?>> extends TreeVisitor<T, R> {
    /**
     * Visit a node in between the visits of its children.
     * Override this to implement inorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction visit(List<T> path) {
        return TraversalAction.CONTINUE;
    }
}
