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
