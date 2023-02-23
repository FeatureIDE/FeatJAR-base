/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.tree.visitor;

import de.featjar.util.tree.Trees;
import de.featjar.util.tree.structure.Tree;
import java.util.List;
import java.util.Optional;

/**
 * Interface for a visitor used in the traversal of a tree.
 *
 * @see Trees
 *
 * @author Sebastian Krieter
 */
public interface TreeVisitor<R, T extends Tree<?>> {

    enum VisitorResult {
        Continue,
        SkipChildren,
        SkipAll,
        Fail
    }

    static <T> T getCurrentNode(List<T> path) {
        return path.get(path.size() - 1);
    }

    static <T> T getParentNode(List<T> path) {
        return (path.size() > 1) ? path.get(path.size() - 2) : null;
    }

    default VisitorResult firstVisit(List<T> path) {
        return VisitorResult.Continue;
    }

    default VisitorResult lastVisit(List<T> path) {
        return VisitorResult.Continue;
    }

    default void reset() {}

    default Optional<R> getResult() {
        return Optional.empty();
    }
}
