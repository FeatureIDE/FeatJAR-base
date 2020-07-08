package org.sk.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.sk.utils.io.MultiStream;

/**
 * Extends the standard output with time codes, indentation, and log file
 * writing.
 * 
 * @author Sebastian Krieter
 */
public class Logger {

	private static final String DATE_FORMAT_STRING = "MM/dd/yyyy-HH:mm:ss";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

	private static final String outFileName = "console_log.txt";
	private static final String errFileName = "error_log.txt";

	private static final Logger INSTANCE = new Logger();

	private Logger() {
		orgOut = System.out;
		orgErr = System.err;
		outStream = orgOut;
		errStream = orgErr;
	}

	public static final Logger getInstance() {
		return INSTANCE;
	}

	private final PrintStream orgOut;
	private final PrintStream orgErr;

	private PrintStream outStream;
	private PrintStream errStream;

	public int verboseLevel = 1;
	public boolean installed = false;

	public void install(Path outputPath) throws FileNotFoundException {
		install(outputPath, true);
	}

	public void install(Path outputPath, boolean printToStandardConsole) throws FileNotFoundException {
		synchronized (INSTANCE) {
			if (!installed) {
				File outFile = outputPath.resolve(outFileName).toFile();
				File errFile = outputPath.resolve(errFileName).toFile();
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

	public void uninstall() {
		synchronized (INSTANCE) {
			if (installed) {
				System.setOut(orgOut);
				System.setErr(orgErr);
				outStream = orgOut;
				errStream = orgErr;
				installed = false;
			}
		}
	}

	public int getVerboseLevel() {
		return verboseLevel;
	}

	public void setVerboseLevel(int verboseLevel) {
		this.verboseLevel = verboseLevel;
	}

	public int isVerbose() {
		return verboseLevel;
	}

	public final void logError(String message) {
		println(errStream, message, 1);
	}

	public final void logError(Throwable error) {
		println(errStream, error, 0);
	}

	public final void logInfo(String message) {
		println(outStream, message, 1);
	}

	public final void logError(String message, int verboseLevel) {
		println(errStream, message, verboseLevel);
	}

	public final void logError(Throwable error, int verboseLevel) {
		println(errStream, error, verboseLevel);
	}

	public final void logInfo(String message, int verboseLevel) {
		println(outStream, message, verboseLevel);
	}

	private void println(PrintStream stream, String message, int verbose) {
		if (verboseLevel >= verbose) {
			message = formatMessage(message);
			synchronized (INSTANCE) {
				stream.println(message);
			}
		}
	}

	private void println(PrintStream stream, Throwable error, int verbose) {
		if (verboseLevel >= verbose) {
			synchronized (INSTANCE) {
				stream.print(getCurTime() + " ");
				error.printStackTrace(stream);
			}
		}
	}

	public static final String getCurTime() {
		return DATE_FORMAT.format(new Timestamp(System.currentTimeMillis()));
	}

	public static String formatMessage(String message) {
		final String curTime = getCurTime();

		final StringBuilder sb = new StringBuilder(DATE_FORMAT_STRING.length() + 1 + message.length());
		sb.append(curTime);
		sb.append(' ');
		sb.append(message);
		return sb.toString();
	}

}
