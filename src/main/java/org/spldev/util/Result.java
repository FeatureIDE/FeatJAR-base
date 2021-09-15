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
package org.spldev.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.spldev.util.Problem.*;

/**
 * Similar to {@link Optional}, this wraps an object or {@code null} that is the
 * return value of some function. Can also store any {@link Problem} that
 * occurred during execution of the function.
 * 
 * @author Sebastian Krieter
 */
public class Result<T> {

	private final T object;

	private final List<Problem> problems;

	public static <T> Result<T> of(T object) {
		return new Result<>(object, null);
	}

	public static <T> Result<T> of(T object, List<Problem> problems) {
		return new Result<>(object, problems);
	}

	public static <T> Result<T> empty(List<Problem> problems) {
		return new Result<>(null, problems);
	}

	public static <T> Result<T> empty(Problem... problems) {
		return new Result<>(null, Arrays.asList(problems));
	}

	public static <T> Result<T> empty(Exception... exceptions) {
		return new Result<>(null, Arrays.stream(exceptions).map(Problem::new).collect(Collectors.toList()));
	}

	public static <T> Result<T> empty() {
		return new Result<>(null, null);
	}

	private Result(T object, List<Problem> problems) {
		this.object = object;
		this.problems = (problems == null) || problems.isEmpty() ? Collections.emptyList() : new ArrayList<>(problems);
	}

	public boolean isEmpty() {
		return object == null;
	}

	public boolean isPresent() {
		return object != null;
	}

	public T get() {
		return object;
	}

	public Optional<T> toOptional() {
		return Optional.ofNullable(object);
	}

	/**
	 * Maps the object in this result to another object using a mapper function that
	 * also returns an {@link Result}.
	 * 
	 * @param <R>    The type of the mapped object.
	 * @param mapper the mapper function.
	 * @return A new result with the mapped object or an empty result, if any
	 *         exceptions occur during the mapping or if this result was empty
	 *         before.
	 */
	public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
		return object != null ? mapper.apply(object) : Result.empty(problems);
	}

	/**
	 * Maps the object in this result to another object using a mapper function.
	 * 
	 * @param <R>    The type of the mapped object.
	 * @param mapper the mapper function.
	 * @return A new result with the mapped object or an empty result, if any
	 *         exceptions occur during the mapping or if this result was empty
	 *         before.
	 */
	public <R> Result<R> map(Function<T, R> mapper) {
		if (object != null) {
			try {
				return Result.of(mapper.apply(object), problems);
			} catch (final Exception e) {
				return Result.empty(e);
			}
		} else {
			return Result.empty(problems);
		}
	}

	public Result<T> peek(Consumer<T> consumer) {
		if (object != null) {
			try {
				consumer.accept(object);
			} catch (final Exception e) {
			}
		}
		return this;
	}

	public T orElse(T alternative) {
		return object != null ? object : alternative;
	}

	public T orElse(Supplier<T> alternativeSupplier) {
		return object != null ? object : alternativeSupplier.get();
	}

	public T orElse(Consumer<List<Problem>> errorHandler) {
		if (object != null) {
			return object;
		} else {
			errorHandler.accept(problems);
			return null;
		}
	}

	public T orElse(T alternative, Consumer<List<Problem>> errorHandler) {
		if (object != null) {
			return object;
		} else {
			errorHandler.accept(problems);
			return alternative;
		}
	}

	public T orElse(Supplier<T> alternativeSupplier, Consumer<List<Problem>> errorHandler) {
		if (object != null) {
			return object;
		} else {
			errorHandler.accept(problems);
			return alternativeSupplier.get();
		}
	}

	public <E extends Exception> T orElseThrow(Function<List<Problem>, E> errorHandler) throws E {
		if (object != null) {
			return object;
		} else {
			throw errorHandler.apply(problems);
		}
	}

	public T orElseThrow() throws RuntimeException {
		if (object != null) {
			return object;
		} else {
			throw problems.stream() //
				.filter(p -> p.getSeverity() == Severity.ERROR) //
				.findFirst() //
				.map(this::getError) //
				.orElseGet(RuntimeException::new);
		}
	}

	private RuntimeException getError(Problem p) {
		return p.getError().map(RuntimeException::new)
			.orElseGet(() -> p.getMessage().map(RuntimeException::new).orElseGet(RuntimeException::new));
	}

	public void ifPresent(Consumer<T> resultHandler) {
		if (object != null) {
			resultHandler.accept(object);
		}
	}

	public void ifPresentOrElse(Consumer<T> resultHandler, Consumer<List<Problem>> errorHandler) {
		if (object != null) {
			resultHandler.accept(object);
		} else {
			errorHandler.accept(problems);
		}
	}

	public List<Problem> getProblems() {
		return Collections.unmodifiableList(problems);
	}

	public boolean hasProblems() {
		return !problems.isEmpty();
	}

}
