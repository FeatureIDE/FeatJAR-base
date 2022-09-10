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
package de.featjar.util.io.format;

import de.featjar.util.data.Problem;

/**
 * A problem that occurs while parsing an {@link de.featjar.util.io.Input}.
 * Stores a line number where the problem occurred.
 *
 * @author Sebastian Krieter
 */
public class ParseProblem extends Problem {
    // todo: store file, too (for UVL)
    protected final int lineNumber;

    /**
     * Create a new parse problem.
     *
     * @param exception the exception
     * @param lineNumber the line number
     */
    public ParseProblem(Exception exception, int lineNumber) {
        this(exception.getMessage(), lineNumber, Severity.ERROR, exception);
    }

    /**
     * Create a new parse problem.
     *
     * @param message the message
     * @param lineNumber the line number
     * @param severity the severity
     */
    public ParseProblem(String message, int lineNumber, Severity severity) {
        this(message, lineNumber, severity, null);
    }

    protected ParseProblem(String message, int lineNumber, Severity severity, Exception exception) {
        super(message, severity, exception);
        this.lineNumber = lineNumber;
    }

    /**
     * {@return the line where the problem occurred}
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
