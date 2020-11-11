package org.spldev.utils.logging;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.spldev.utils.io.*;

/**
 * Extends the standard output with time codes, indentation, and log file
 * writing.
 * 
 * @author Sebastian Krieter
 */
public final class Logger {

	public enum LogType {
		ERROR, INFO, DEBUG, PROGRESS
	}

	private static class Log {
		private final HashSet<LogType> enabledLogTypes = new HashSet<>();
		private final String path;
		private final PrintStream out;

		public Log(String path, PrintStream out, LogType... logTypes) {
			this.path = path;
			this.out = out;
			for (final LogType logType : logTypes) {
				enabledLogTypes.add(logType);
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(path);
		}

		@Override
		public boolean equals(Object obj) {
			return (this == obj) || ((obj instanceof Log) && Objects.equals(path, ((Log) obj).path));
		}
	}

	private static final PrintStream orgOut = System.out;
	private static final PrintStream orgErr = System.err;

	private static boolean installed = false;

	private static final LinkedHashSet<Log> logs = new LinkedHashSet<>();
	private static final LinkedList<Formatter> formatters = new LinkedList<>();

	public synchronized static boolean addFileLog(Path path, LogType... logTypes) {
		if (!installed) {
			path = path.toAbsolutePath().normalize();
			try {
				final PrintStream stream = new PrintStream(new FileOutputStream(path.toFile()));
				return logs.add(new Log(path.toString(), stream, logTypes));
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public synchronized static boolean addOutLog(LogType... logTypes) {
		if (!installed) {
			return logs.add(new Log("system:out", System.out, logTypes));
		}
		return false;
	}

	public synchronized static boolean addErrLog(LogType... logTypes) {
		if (!installed) {
			return logs.add(new Log("system:err", System.err, logTypes));
		}
		return false;
	}

	public synchronized static void addFormatter(Formatter formatter) {
		formatters.add(formatter);
	}

	public synchronized static void removeFormatter(Formatter formatter) {
		formatters.remove(formatter);
	}

	public synchronized static void install() throws FileNotFoundException {
		if (!installed) {
			final List<OutputStream> outStreamList = new ArrayList<>();
			final List<OutputStream> errStreamList = new ArrayList<>();
			for (final Log log : logs) {
				if (log.enabledLogTypes.contains(LogType.DEBUG)) {
					outStreamList.add(log.out);
				}
				if (log.enabledLogTypes.contains(LogType.ERROR)) {
					errStreamList.add(log.out);
				}
			}

			System.setOut(new PrintStream(new MultiStream(outStreamList)));
			System.setErr(new PrintStream(new MultiStream(errStreamList)));

			installed = true;
		}
	}

	public synchronized static void uninstall() {
		if (installed) {
			System.setOut(orgOut);
			System.setErr(orgErr);
			installed = false;
		}
	}

	public static void logError(Throwable error) {
		println(error);
	}

	public static void logError(String message) {
		println(message, LogType.ERROR);
	}

	public static void logInfo(String message) {
		println(message, LogType.INFO);
	}

	public static void logDebug(String message) {
		println(message, LogType.DEBUG);
	}

	public static void logProgress(String message) {
		println(message, LogType.PROGRESS);
	}

	public static void log(String message, LogType logType) {
		println(message, logType);
	}

	private synchronized static void println(String message, LogType logType) {
		final String formatedMessage = formatMessage(message);
		for (final Log log : logs) {
			if (log.enabledLogTypes.contains(logType)) {
				log.out.println(formatedMessage);
			}
		}
	}

	private synchronized static void println(Throwable error) {
		final String formatedMessage = formatMessage(error.getMessage());
		for (final Log log : logs) {
			if (log.enabledLogTypes.contains(LogType.ERROR)) {
				log.out.println(formatedMessage);
				error.printStackTrace(log.out);
			}
		}
	}

	private static String formatMessage(String message) {
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
