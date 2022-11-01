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
package de.featjar.base.cli;

import de.featjar.base.Feat;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.Format;
import de.featjar.base.io.format.FormatSupplier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Runs commands.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class CommandLine {
    public static final String DEFAULT_MAXIMUM_VERBOSITY = "info";
    public static final String SYSTEM_INPUT = "system:in.xml"; //todo
    public static final String SYSTEM_OUTPUT = "system:out";
    public static final String SYSTEM_ERROR = "system:err";
    private static final Pattern SYSTEM_INPUT_PATTERN = Pattern.compile("system:in\\.(.+)");

    public static void run(String[] args) {
        Feat.log().debug("running command-line interface");
        if (args.length == 0) {
            System.err.println("No command given. Please pass a command as the first argument.");
            printUsage();
            return;
        }
        final String commandName = args[0];
        FeatJAR.extensionPoint(Commands.class).getExtensions().stream()
                .filter(e -> Objects.equals(commandName, e.getName()))
                .findFirst()
                .ifPresentOrElse(
                        command -> runCommand(command, args),
                        () -> {
                            System.err.println("The command " + commandName + " could not be found.");
                            printUsage();
                        });
    }

    private static void printUsage() {
        List<Command> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        if (commands.size() > 0) {
            System.err.println("The following commands are available:");
            for (final Command command : commands) {
                System.err.printf("\t%-20s %s\n", command.getName(), command.getDescription().orElse(""));
            }
        } else {
            System.err.println("No commands are available. You can register commands using FeatJAR's extension manager.");
        }
    }

    private static void runCommand(Command command, String[] args) {
        try {
            command.run(Arrays.asList(args).subList(1, args.length));
        } catch (final IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(command.getUsage().orElse(""));
        }
    }

    public static String getArgValue(final Iterator<String> iterator, final String arg) {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new IllegalArgumentException("No value specified for " + arg);
        }
    }

    public static <T> Optional<T> runInThread(Callable<T> method, long timeout) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(method);
        try {
            return Optional.of(timeout == 0 ? future.get() : future.get(timeout, TimeUnit.MILLISECONDS));
        } catch (final TimeoutException e) {
            System.exit(0);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            executor.shutdownNow();
        }
        return Optional.empty();
    }

    public static boolean isValidInput(String pathOrStdin) {
        return SYSTEM_INPUT_PATTERN.matcher(pathOrStdin).matches() || Files.exists(Paths.get(pathOrStdin));
    }

    public static <T> Result<T> loadFile(String pathOrStdin, FormatSupplier<T> formatSupplier) {
        Matcher matcher = SYSTEM_INPUT_PATTERN.matcher(pathOrStdin);
        if (matcher.matches()) {
            Path path = Paths.get("stdin." + matcher.group(1));
            String content = new BufferedReader(new InputStreamReader(System.in, IO.DEFAULT_CHARSET))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return IO.load(content, path, formatSupplier);
        } else {
            return IO.load(Paths.get(pathOrStdin), formatSupplier);
        }
    }

    public static <T> void saveFile(T object, String pathOrStdout, Format<T> format) {
        try {
            if (pathOrStdout.equals(SYSTEM_OUTPUT)) {
                IO.save(object, System.out, format);
            } else {
                IO.save(object, Paths.get(pathOrStdout), format);
            }
        } catch (final IOException e) {
            Feat.log().error(e);
        }
    }
}
