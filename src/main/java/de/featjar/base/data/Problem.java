/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.data;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A problem that wraps an {@link Exception}. Can be stored in a {@link Result}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Problem {

    /**
     * Severity of a problem.
     */
    public static enum Severity {
        /**
         * An info message, which does not prevent processing of an object.
         */
        INFO,
        /**
         * A warning, which does not prevent processing of an object.
         */
        WARNING,
        /**
         * A severe error, which usually prevents processing of an object.
         */
        ERROR
    }

    /**
     * Checks whether a list of {@link Problem problems} contains at least one problem classified as {@link Severity#ERROR}.
     * @param problemList the list of problems
     * @return {@code true} if list contains at least one error, {@code false} otherwise
     */
    public static boolean containsError(List<Problem> problemList) {
        return problemList.stream()
                .filter(p -> p.getSeverity() == Severity.ERROR)
                .findAny()
                .isPresent();
    }

    /**
     * Writes all messages of the given problems to a string.
     *
     * @param problems the problem list
     * @return a string containing one message per line
     */
    public static String printProblems(List<Problem> problems) {
        if (!problems.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Problem problem : problems) {
                sb.append(problem.getMessage());
                sb.append('\n');
                Exception exception = problem.getException();
                if (exception != null) {
                    Arrays.stream(exception.getStackTrace())
                            .map(Object::toString)
                            .forEach(s -> sb.append('\t').append(s).append('\n'));
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } else {
            return "";
        }
    }

    protected final Exception exception;
    protected final Severity severity;

    /**
     * Creates an error problem with a message.
     *
     * @param message the message
     */
    public Problem(String message) {
        this(new Exception(message));
    }

    /**
     * Creates an error problem with an exception.
     *
     * @param exception the exception
     */
    public Problem(Exception exception) {
        this(exception, Severity.ERROR);
    }

    /**
     * Creates an error problem with a message.
     *
     * @param message  the message
     * @param severity the severity
     */
    public Problem(String message, Severity severity) {
        this(new Exception(message), severity);
    }

    /**
     * Creates an error problem with an exception.
     *
     * @param exception the exception
     * @param severity  the severity
     */
    public Problem(Exception exception, Severity severity) {
        this.exception = Objects.requireNonNull(exception);
        this.severity = Objects.requireNonNull(severity);
    }

    @Override
    public String toString() {
        return severity + ": " + getMessage() + "\n";
    }

    /**
     * {@return the exception of this problem}
     */
    public Exception getException() {
        return exception;
    }

    /**
     * {@return the message of this problem}
     */
    public String getMessage() {
        return Optional.ofNullable(exception.getMessage())
                .orElse(exception.getClass().getSimpleName());
    }

    /**
     * {@return the severity of this problem}
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * {@return an unchecked exception describing this problem}
     */
    public RuntimeException getUncheckedException() {
        return new RuntimeException(exception);
    }
}
