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
package org.spldev.util.tree.structure;

import java.util.*;

public abstract class AbstractNonTerminal<T extends Tree<T>> implements Tree<T> {

	protected final List<T> children = new ArrayList<>();

	@Override
	public void setChildren(Collection<? extends T> children) {
		Objects.requireNonNull(children);
		this.children.clear();
		this.children.addAll(children);
	}

	@Override
	public List<? extends T> getChildren() {
		return Collections.unmodifiableList(children);
	}

}
