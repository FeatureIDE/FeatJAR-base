/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
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
	public VistorResult firstVisit(List<Tree<?>> path) {
		final int depth = path.size();
		if (maxDepth < depth) {
			maxDepth = depth;
		}
		final Tree<?> node = TreeVisitor.getCurrentNode(path);
		if ((terminalNode != null) && terminalNode.isInstance(node)) {
			return VistorResult.SkipChildren;
		} else {
			return VistorResult.Continue;
		}
	}

	@Override
	public Integer getResult() {
		return maxDepth;
	}

	public Class<? extends Tree<?>> getTerminalNode() {
		return terminalNode;
	}

	public void setTerminalNode(Class<? extends Tree<?>> terminalNode) {
		this.terminalNode = terminalNode;
	}

}
