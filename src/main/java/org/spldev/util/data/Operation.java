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
package org.spldev.util.data;

import java.util.*;
import java.util.function.*;

import org.spldev.util.logging.*;

/**
 * Abstract operation to modify elements from a {@link CacheHolder}.
 *
 * @author Sebastian Krieter
 */
public abstract class Operation {

	protected abstract Map<Identifier<?>, BiFunction<?, ?, ?>> getImplementations();

	@SuppressWarnings("unchecked")
	public final <T> T apply(Identifier<T> identifier, Object parameters, Object element) {
		try {
			final BiFunction<T, Object, T> op4Rep = (BiFunction<T, Object, T>) getImplementations().get(identifier);
			return (op4Rep != null)
				? op4Rep.apply((T) element, parameters)
				: null;
		} catch (final ClassCastException e) {
			Logger.logError(e);
			return null;
		}
	}

}
