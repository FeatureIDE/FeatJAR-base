/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021-2022  Sebastian Krieter
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
package org.spldev.util.cli;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

import org.spldev.util.data.*;
import org.spldev.util.extension.*;
import org.spldev.util.io.*;
import org.spldev.util.io.format.*;
import org.spldev.util.logging.*;

/**
 * Command line interface for several functions of FeatureIDE.
 *
 * @author Sebastian Krieter
 */
public class CLI {
	public static final String DEFAULT_VERBOSITY = "info";
	public static final String SYSTEM_INPUT = "system:in.xml";
	public static final String SYSTEM_OUTPUT = "system:out";
	public static final String SYSTEM_ERROR = "system:err";
	private static final Pattern SYSTEM_INPUT_PATTERN = Pattern.compile("system:in\\.(.+)");

	public static void main(String[] args) {
		ExtensionLoader.load();
		if (args.length == 0) {
			printError("No function specified. Please specify a function as the first argument.");
			return;
		}
		final String functionName = args[0];

		CLIFunctionManager.getInstance().getExtensions().stream()
			.filter(e -> Objects.equals(functionName, e.getName()))
			.findFirst()
			.ifPresentOrElse(function -> {
				runFunction(args, function);
			}, () -> {
				printError("The function " + functionName + " could not be found.");
			});
	}

	public static void installLogger(String verbosity) {
		String[] verbosities = new String[] { "none", "error", "info", "debug", "progress" };
		if (!Arrays.asList(verbosities).contains(verbosity))
			throw new IllegalArgumentException("invalid verbosity " + verbosity);
		if (verbosity.equals("none")) {
			Logger.setErrLog();
		} else {
			Logger.setErrLog(Logger.LogType.ERROR);
		}
		if (verbosity.equals("progress")) {
			Logger.setOutLog(Logger.LogType.INFO, Logger.LogType.DEBUG, Logger.LogType.PROGRESS);
		} else if (verbosity.equals("debug")) {
			Logger.setOutLog(Logger.LogType.INFO, Logger.LogType.DEBUG);
		} else if (verbosity.equals("info")) {
			Logger.setOutLog(Logger.LogType.INFO);
		} else if (verbosity.equals("error")) {
			Logger.setOutLog();
		}
		Logger.install();
	}

	private static void printError(String errorMessage) {
		System.err.println(errorMessage);
		printHelp(System.err);
	}

	private static void runFunction(String[] args, CLIFunction function) {
		try {
			function.run(Arrays.asList(args).subList(1, args.length));
		} catch (final IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println(function.getHelp());
		}
	}

	private static void printHelp(PrintStream printStream) {
		printStream.println("The following functions are available:");
		for (final CLIFunction availableFunction : CLIFunctionManager.getInstance().getExtensions()) {
			printStream.printf("\t%-20s %s\n", availableFunction.getName(), availableFunction.getDescription());
		}
	}

	public static String getArgValue(final Iterator<String> iterator, final String arg) {
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			throw new IllegalArgumentException("No value specified for " + arg);
		}
	}

	public static <T> T runInThread(Callable<T> method, long timeout) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<T> future = executor.submit(method);
		try {
			return timeout == 0 ? future.get() : future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (final TimeoutException e) {
			System.exit(0);
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			executor.shutdownNow();
		}
		return null;
	}

	public static boolean isValidInput(String pathOrStdin) {
		return SYSTEM_INPUT_PATTERN.matcher(pathOrStdin).matches() || Files.exists(Paths.get(pathOrStdin));
	}

	public static <T> Result<T> loadFile(String pathOrStdin, FormatSupplier<T> formatSupplier) {
		Matcher matcher = SYSTEM_INPUT_PATTERN.matcher(pathOrStdin);
		if (matcher.matches()) {
			Path path = Paths.get("stdin." + matcher.group(1));
			String content = new BufferedReader(
				new InputStreamReader(System.in, FileHandler.DEFAULT_CHARSET))
					.lines()
					.collect(Collectors.joining("\n"));
			return FileHandler.load(content, path, formatSupplier);
		} else {
			return FileHandler.load(Paths.get(pathOrStdin), formatSupplier);
		}
	}

	public static <T> void saveFile(T object, String pathOrStdout, Format<T> format) {
		try {
			if (pathOrStdout.equals(SYSTEM_OUTPUT)) {
				FileHandler.save(object, System.out, format);
			} else {
				FileHandler.save(object, Paths.get(pathOrStdout), format);
			}
		} catch (final IOException e) {
			Logger.logError(e);
		}
	}
}
