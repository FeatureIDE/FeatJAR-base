/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021-2022  Sebastian Krieter
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

import org.spldev.util.tree.*;
import org.spldev.util.tree.structure.*;

/**
 * Interface for a visitor used in the traversal of a tree.
 * 
 * @see Trees
 * 
 * @author Sebastian Krieter
 */
public interface DfsVisitor<R, T extends Tree<?>> extends TreeVisitor<R, T> {

	default VisitorResult visit(List<T> path) {
		return VisitorResult.Continue;
	}

}
