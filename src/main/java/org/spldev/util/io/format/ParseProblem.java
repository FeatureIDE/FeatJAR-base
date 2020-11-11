package org.spldev.util.io.format;

import java.util.*;

/**
 * Saves a warning with a line number where it occurred.
 *
 * @author Sebastian Krieter
 */
public class ParseProblem {

	public enum Severity {
		INFO(0), WARNING(1), ERROR(2);

		private final int level;

		private Severity(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}
	}

	private final Severity severity;

	private final String message;

	private final int line;

	private final Throwable error;

	public ParseProblem(Throwable throwable, int line) {
		this(throwable.getMessage(), line, Severity.ERROR, throwable);
	}

	public ParseProblem(String message, int line, Severity severity) {
		this(message, line, severity, null);
	}

	protected ParseProblem(String message, int line, Severity severity, Throwable error) {
		this.message = message;
		this.line = line;
		this.severity = severity;
		this.error = error;
	}

	@Override
	public String toString() {
		return severity + ": " + String.valueOf(message);
	}

	public Severity getSeverity() {
		return severity;
	}

	public Optional<String> getMessage() {
		return Optional.ofNullable(message);
	}

	public int getLine() {
		return line;
	}

	public Optional<Throwable> getError() {
		return Optional.ofNullable(error);
	}

}
