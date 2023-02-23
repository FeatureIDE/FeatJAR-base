/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
package de.featjar.util.io.format;

import de.featjar.util.data.Problem;

/**
 * Stores a problem during parsing with a line number where it occurred.
 *
 * @author Sebastian Krieter
 */
public class ParseProblem extends Problem {

    protected final int line;

    public ParseProblem(Exception exception, int line) {
        this(exception.getMessage(), line, Severity.ERROR, exception);
    }

    public ParseProblem(String message, int line, Severity severity) {
        this(message, line, severity, null);
    }

    protected ParseProblem(String message, int line, Severity severity, Exception exception) {
        super(message, severity, exception);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
