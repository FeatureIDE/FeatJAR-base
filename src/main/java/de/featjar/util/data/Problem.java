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
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.util.data;

import java.util.Optional;

/**
 * A problem that occurs in the creation or transformation of an object.
 * Can be stored in a {@link Result}.
 *
 * @author Sebastian Krieter
 */
public class Problem {
    /**
     * Severity of a problem.
     */
    public enum Severity {
        /**
         * A problem that does not affect the processing of an object, but should be reported to the user.
         */
        INFO,
        /**
         * A warning, which does not prevent processing of an object.
         */
        WARNING,
        /**
         * A severe error, which prevents processing of an object.
         */
        ERROR
    }

    protected final Severity severity;

    protected final String message;

    protected final Exception exception;

    /**
     * Wraps an exception as an error problem.
     *
     * @param exception the exception
     */
    public Problem(Exception exception) {
        this(exception.getMessage(), Severity.ERROR, exception);
    }

    /**
     * Wraps a message as a problem.
     *
     * @param message the message
     * @param severity the severity
     */
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
        return severity + ": " + message;
    }

    /**
     * {@return the severity of this problem}
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * {@return the message of this problem, if any}
     */
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    /**
     * {@return the exception of this problem, if any}
     */
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    /**
     * {@return whether this problem has an exception}
     */
    public boolean hasException() {
        return exception != null;
    }

    /**
     * {@return an exception describing this problem}
     */
    public Exception toException() {
        return getException()
                .orElseGet(() -> getMessage().map(RuntimeException::new).orElseGet(RuntimeException::new));
    }
}
