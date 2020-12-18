package org.spldev.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Similar to {@link Optional} wraps an object or {@code null} that is the
 * return value of some function. Can also store any {@link problems} that
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
		this.problems = problems != null ? new ArrayList<>(problems) : Collections.emptyList();
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

	public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
		return object != null ? mapper.apply(object) : Result.empty(problems);
	}

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
