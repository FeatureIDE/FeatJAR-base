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
package org.spldev.util.tree.visitor;

import java.util.*;

import org.spldev.util.tree.structure.*;

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
