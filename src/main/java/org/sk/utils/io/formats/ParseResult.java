package org.sk.utils.io.formats;

import java.util.*;

public class ParseResult<T> {

	private final T element;
	private final List<ParseProblem> problems;

	public static <T> ParseResult<T> of(T element, Collection<ParseProblem> problems) {
		return new ParseResult<T>(element, problems);
	}

	public static <T> ParseResult<T> of(T element) {
		return new ParseResult<T>(element, Collections.emptyList());
	}

	private ParseResult(T element, Collection<ParseProblem> problems) {
		this.element = element;
		this.problems = new ArrayList<>(problems);
	}

	public Optional<T> get() {
		return Optional.ofNullable(element);
	}

	public List<ParseProblem> getProblems() {
		return Collections.unmodifiableList(problems);
	}

}
