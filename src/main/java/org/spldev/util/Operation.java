package org.spldev.util;

import java.util.*;
import java.util.function.*;

import org.spldev.util.logging.*;

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
