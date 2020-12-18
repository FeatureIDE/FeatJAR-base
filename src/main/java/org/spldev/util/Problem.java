package org.spldev.util;

import java.util.*;

/**
 * Wraps an arbitrary problem when creating/transforming and object. Can be
 * stored in a {@link Result result wrapper}.
 *
 * @author Sebastian Krieter
 */
public class Problem {

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

	protected final Severity severity;

	protected final String message;

	protected final Throwable exception;

	public Problem(Exception exception) {
		this(exception.getMessage(), Severity.ERROR, exception);
	}

	public Problem(String message, Severity severity) {
		this(message, severity, null);
	}

	protected Problem(String message, Severity severity, Exception exception) {
		this.message = message;
		this.severity = severity;
		this.exception = exception;
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

	public Optional<Throwable> getError() {
		return Optional.ofNullable(exception);
	}

}
