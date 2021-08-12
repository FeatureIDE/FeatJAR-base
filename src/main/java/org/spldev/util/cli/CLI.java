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
package org.spldev.util.cli;

import java.io.*;
import java.util.*;

import org.spldev.util.extension.*;

/**
 * Command line interface for several functions of FeatureIDE.
 *
 * @author Sebastian Krieter
 */
public class CLI {

	public static void main(String[] args) {
		if (args.length == 0) {
			printError("No operation specified!");
			return;
		}
		ExtensionLoader.load();
		final String functionName = args[0];

		CLIFunctionManager.getInstance().getExtension(functionName)
			.ifPresentOrElse(function -> {
				runFunction(args, function);
			}, problems -> {
				printError("No function found with the name " + functionName);
			});
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
			return;
		}
	}

	private static void printHelp(PrintStream printStream) {
		printStream.println("Following functions are available:");
		for (final CLIFunction availableFunction : CLIFunctionManager.getInstance().getExtensions()) {
			printStream.println("\t" + availableFunction.getName());
		}
	}

	public static String getArgValue(final Iterator<String> iterator, final String arg) {
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			throw new IllegalArgumentException("No value specified for " + arg);
		}
	}

}
