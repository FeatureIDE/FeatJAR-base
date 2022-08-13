/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
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
 */
package de.featjar.util.data;

import java.util.Optional;

/**
 * Wraps an arbitrary problem when creating or transforming an object. Can be
 * stored in a {@link Result}.
 *
 * @author Sebastian Krieter
 */
public class Problem {

	public enum Severity {
		INFO(0), WARNING(1), ERROR(2);

		private final int level;

		Severity(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}
	}

	protected final Severity severity;

	protected final String message;

	protected final Exception exception;

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

	public Optional<Exception> getException() {
		return Optional.ofNullable(exception);
	}

	public Exception toException() {
		return getException().orElseGet(() -> getMessage().map(RuntimeException::new).orElseGet(RuntimeException::new));
	}

	public boolean hasError() {
		return exception != null;
	}

}
