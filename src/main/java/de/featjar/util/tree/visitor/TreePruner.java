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
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.tree.visitor;

import de.featjar.util.tree.structure.Tree;
import java.util.Collections;
import java.util.List;

public class TreePruner implements TreeVisitor<Void, Tree<?>> {

    private int depthLimit = Integer.MAX_VALUE;

    public int getDepthLimit() {
        return depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = depthLimit;
    }

    @Override
    public VisitorResult firstVisit(List<Tree<?>> path) {
        try {
            if (path.size() > depthLimit) {
                final Tree<?> node = TreeVisitor.getCurrentNode(path);
                node.setChildren(Collections.emptyList());
                return VisitorResult.SkipChildren;
            }
            return VisitorResult.Continue;
        } catch (final Exception e) {
            return VisitorResult.SkipAll;
        }
    }
}
