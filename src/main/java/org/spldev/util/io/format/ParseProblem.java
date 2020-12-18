package org.spldev.util.io.format;

import org.spldev.util.*;

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
