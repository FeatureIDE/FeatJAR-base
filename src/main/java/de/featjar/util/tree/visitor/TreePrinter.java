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
package de.featjar.util.tree.visitor;

import java.util.*;
import java.util.function.*;

import de.featjar.util.tree.structure.Tree;
import de.featjar.util.tree.structure.*;

public class TreePrinter implements TreeVisitor<String, Tree<?>> {

	private String indentation = "  ";

	private StringBuilder treeStringBuilder = new StringBuilder();
	private Predicate<Tree<?>> filter = null;
	private Function<Tree<?>, String> toStringFunction = Object::toString;

	@Override
	public void reset() {
		treeStringBuilder.delete(0, treeStringBuilder.length());
	}

	@Override
	public Optional<String> getResult() {
		return Optional.of(treeStringBuilder.toString());
	}

	public String getIndentation() {
		return indentation;
	}

	public void setIndentation(String indentation) {
		this.indentation = indentation;
	}

	@Override
	public VisitorResult firstVisit(List<Tree<?>> path) {
		final Tree<?> currentNode = TreeVisitor.getCurrentNode(path);
		if ((filter == null) || filter.test(currentNode)) {
			try {
				for (int i = 1; i < path.size(); i++) {
					treeStringBuilder.append(indentation);
				}
				treeStringBuilder.append(toStringFunction.apply(currentNode));
				treeStringBuilder.append('\n');
			} catch (final Exception e) {
				return VisitorResult.SkipAll;
			}
		}
		return VisitorResult.Continue;
	}

	public void setFilter(Predicate<Tree<?>> filter) {
		this.filter = filter;
	}

	public void setToStringFunction(Function<Tree<?>, String> toStringFunction) {
		this.toStringFunction = toStringFunction;
	}

}
