/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
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
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.tree.visitor;

import java.util.*;

import org.spldev.util.tree.structure.*;

/**
 * Visitor that computes the maximum depth of a tree.
 * 
 * @author Sebastian Krieter
 *
 */
public class TreeDepthCounter implements TreeVisitor<Integer, Tree<?>> {

	private Class<? extends Tree<?>> terminalNode = null;

	private int maxDepth = 0;

	@Override
	public void reset() {
		maxDepth = 0;
	}

	@Override
	public VisitorResult firstVisit(List<Tree<?>> path) {
		final int depth = path.size();
		if (maxDepth < depth) {
			maxDepth = depth;
		}
		final Tree<?> node = TreeVisitor.getCurrentNode(path);
		if ((terminalNode != null) && terminalNode.isInstance(node)) {
			return VisitorResult.SkipChildren;
		} else {
			return VisitorResult.Continue;
		}
	}

	@Override
	public Optional<Integer> getResult() {
		return Optional.of(maxDepth);
	}

	public Class<? extends Tree<?>> getTerminalNode() {
		return terminalNode;
	}

	public void setTerminalNode(Class<? extends Tree<?>> terminalNode) {
		this.terminalNode = terminalNode;
	}

}
