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

import de.featjar.base.tree.structure.Traversable;
import java.util.Collections;
import java.util.List;

/**
 * Prunes a tree to a given maximum depth.
 * That is, children below this depth will be removed from the tree.
 *
 * @author Sebastian Krieter
 */
public class TreePruner implements TreeVisitor<Traversable<?>, Void> {

    private int depthLimit = Integer.MAX_VALUE;

    public int getDepthLimit() {
        return depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    @Override
    public TraversalAction firstVisit(List<Traversable<?>> path) {
        try {
            if (path.size() > depthLimit) {
                final Traversable<?> node = getCurrentNode(path);
                node.setChildren(Collections.emptyList());
                return TraversalAction.SKIP_CHILDREN;
            }
            return TraversalAction.CONTINUE;
        } catch (final Exception e) {
            return TraversalAction.SKIP_ALL;
        }
    }
}
