package org.sk.utils.logging;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.sk.utils.io.*;

/**
 * Extends the standard output with time codes, indentation, and log file
 * writing.
 * 
 * @author Sebastian Krieter
 */
public class Logger {

	private static final String outFileName = "console_log.txt";
	private static final String errFileName = "error_log.txt";

	private static final Object lock = new Object();

	private static final PrintStream orgOut = System.out;
	private static final PrintStream orgErr = System.err;

	private static PrintStream outStream = orgOut;
	private static PrintStream errStream = orgErr;

	private static int verboseLevel = 1;
	private static boolean installed = false;

	private static final List<Formatter> formatters = new LinkedList<>();

	public static void install(Path outputPath) throws FileNotFoundException {
		install(outputPath, true);
	}

	public static void install(Path outputPath, boolean printToStandardConsole) throws FileNotFoundException {
		synchronized (lock) {
			if (!installed) {
				final File outFile = outputPath.resolve(outFileName).toFile();
				final File errFile = outputPath.resolve(errFileName).toFile();
				final FileOutputStream outFileStream = new FileOutputStream(outFile);
				final FileOutputStream errFileStream = new FileOutputStream(errFile);
				if (printToStandardConsole) {
					outStream = new PrintStream(new MultiStream(orgOut, outFileStream));
					errStream = new PrintStream(new MultiStream(orgErr, errFileStream));
				} else {
					outStream = new PrintStream(outFileStream);
					errStream = new PrintStream(errFileStream);
				}

				System.setOut(outStream);
				System.setErr(errStream);
			}
		}
	}

	public static void uninstall() {
		synchronized (lock) {
			if (installed) {
				System.setOut(orgOut);
				System.setErr(orgErr);
				outStream = orgOut;
				errStream = orgErr;
				installed = false;
			}
		}
	}

	public static int getVerboseLevel() {
		return verboseLevel;
	}

	public static void setVerboseLevel(int verboseLevel) {
		Logger.verboseLevel = verboseLevel;
	}

	public static int isVerbose() {
		return verboseLevel;
	}

	public static void addFormatter(Formatter formatter) {
		synchronized (lock) {
			formatters.add(formatter);
		}
	}

	public static void removeFormatter(Formatter formatter) {
		synchronized (lock) {
			formatters.remove(formatter);
		}
	}

	public static final void logError(String message) {
		println(errStream, message, 1);
	}

	public static final void logError(Throwable error) {
		println(errStream, error, 0);
	}

	public static final void logInfo(String message) {
		println(outStream, message, 1);
	}

	public static final void logError(String message, int verboseLevel) {
		println(errStream, message, verboseLevel);
	}

	public static final void logError(Throwable error, int verboseLevel) {
		println(errStream, error, verboseLevel);
	}

	public static final void logInfo(String message, int verboseLevel) {
		println(outStream, message, verboseLevel);
	}

	private static void println(PrintStream stream, String message, int verbose) {
		if (verboseLevel >= verbose) {
			synchronized (lock) {
				stream.println(formatMessage(message));
			}
		}
	}

	private static void println(PrintStream stream, Throwable error, int verbose) {
		if (verboseLevel >= verbose) {
			synchronized (lock) {
				stream.print(formatMessage(error.getMessage()));
				error.printStackTrace(stream);
			}
		}
	}

	public static String formatMessage(String message) {
		if (formatters.isEmpty()) {
			return message;
		} else {
			final StringBuilder sb = new StringBuilder();
			for (final Formatter formatter : formatters) {
				formatter.format(sb);
			}
			sb.append(message);
			return sb.toString();
		}
	}

}
