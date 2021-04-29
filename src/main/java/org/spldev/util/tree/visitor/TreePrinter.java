/* -----------------------------------------------------------------------------
 * Tree-Lib - Simple Java framework for creating and traversing tree structures.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Tree-Lib.
 * 
 * Tree-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Tree-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tree-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/trees> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.tree.visitor;

import java.util.*;

import org.spldev.util.tree.structure.*;

public class TreePrinter implements TreeVisitor<String, Tree<?>> {

	private String indentation = "  ";

	private StringBuilder treeStringBuilder = new StringBuilder();

	@Override
	public void reset() {
		treeStringBuilder.delete(0, treeStringBuilder.length());
	}

	@Override
	public String getResult() {
		return treeStringBuilder.toString();
	}

	public String getIndentation() {
		return indentation;
	}

	public void setIndentation(String indentation) {
		this.indentation = indentation;
	}

	@Override
	public VistorResult firstVisit(List<Tree<?>> path) {
		try {
			for (int i = 1; i < path.size(); i++) {
				treeStringBuilder.append(indentation);
			}
			treeStringBuilder.append(TreeVisitor.getCurrentNode(path));
			treeStringBuilder.append('\n');
		} catch (final Exception e) {
			return VistorResult.SkipAll;
		}
		return VistorResult.Continue;
	}

}
