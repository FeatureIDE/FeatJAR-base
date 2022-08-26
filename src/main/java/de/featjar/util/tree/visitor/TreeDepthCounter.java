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

import de.featjar.util.tree.structure.Traversable;
import java.util.List;
import java.util.Optional;

/**
 * Visitor that computes the maximum depth of a tree.
 *
 * @author Sebastian Krieter
 *
 */
public class TreeDepthCounter implements TreeVisitor<Integer, Traversable<?>> {

    private Class<? extends Traversable<?>> terminalNode = null;

    private int maxDepth = 0;

    @Override
    public void reset() {
        maxDepth = 0;
    }

    @Override
    public TraversalAction firstVisit(List<Traversable<?>> path) {
        final int depth = path.size();
        if (maxDepth < depth) {
            maxDepth = depth;
        }
        final Traversable<?> node = TreeVisitor.getCurrentNode(path);
        if ((terminalNode != null) && terminalNode.isInstance(node)) {
            return TraversalAction.SKIP_CHILDREN;
        } else {
            return TraversalAction.CONTINUE;
        }
    }

    @Override
    public Optional<Integer> getResult() {
        return Optional.of(maxDepth);
    }

    public Class<? extends Traversable<?>> getTerminalNode() {
        return terminalNode;
    }

    public void setTerminalNode(Class<? extends Traversable<?>> terminalNode) {
        this.terminalNode = terminalNode;
    }
}
