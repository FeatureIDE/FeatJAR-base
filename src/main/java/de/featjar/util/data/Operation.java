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
package de.featjar.util.data;

import java.util.*;
import java.util.function.*;

import de.featjar.util.logging.Logger;
import de.featjar.util.logging.*;

/**
 * Abstract operation to modify elements from a {@link Cache}.
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
