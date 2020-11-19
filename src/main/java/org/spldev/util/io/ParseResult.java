package org.spldev.util.io;

import java.util.*;
import java.util.function.*;

import org.spldev.util.io.format.*;

public final class ParseResult<T> {

	private final T object;

	private final List<ParseProblem> parseProblems;

	public static <T> ParseResult<T> of(T object) {
		return new ParseResult<>(object, null);
	}

	public static <T> ParseResult<T> of(T object, List<ParseProblem> parseProblems) {
		return new ParseResult<>(object, parseProblems);
	}

	public static <T> ParseResult<T> empty(List<ParseProblem> parseProblems) {
		return new ParseResult<>(null, parseProblems);
	}

	public static <T> ParseResult<T> empty(ParseProblem... parseProblems) {
		return new ParseResult<>(null, Arrays.asList(parseProblems));
	}

	private ParseResult(T object, List<ParseProblem> parseProblems) {
		this.object = object;
		this.parseProblems = parseProblems != null ? new ArrayList<>(parseProblems) : Collections.emptyList();
	}

	public boolean isPresent() {
		return object != null;
	}

	public T get() {
		return object;
	}

	public T orElse(T alternative) {
		return object != null ? object : alternative;
	}

	public T orElseGet(Supplier<T> alternativeSupplier) {
		return object != null ? object : alternativeSupplier.get();
	}

	public T orElseHandleProblems(Consumer<List<ParseProblem>> errorHandler) {
		if (object != null) {
			return object;
		} else {
			errorHandler.accept(parseProblems);
			return null;
		}
	}

	public void ifPresentOrElse(Consumer<T> resultHandler, Consumer<List<ParseProblem>> errorHandler) {
		if (object != null) {
			resultHandler.accept(object);
		} else {
			errorHandler.accept(parseProblems);
		}
	}

	public List<ParseProblem> getParseProblems() {
		return Collections.unmodifiableList(parseProblems);
	}

	public boolean hasParseProblems() {
		return !parseProblems.isEmpty();
	}

}
