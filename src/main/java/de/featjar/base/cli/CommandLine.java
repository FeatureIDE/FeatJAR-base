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
 * Runs commands inside a shell.
 * TODO: add proper argument-parsing, probably with our own small library or something like
 *  <a href="https://github.com/ekuiter/PCLocator/blob/master/src/de/ovgu/spldev/pclocator/Arguments.java">this</a>
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class CommandLine {
    public static final String DEFAULT_MAXIMUM_VERBOSITY = "info";
    public static final String SYSTEM_INPUT = "<stdin>";
    public static final String SYSTEM_OUTPUT = "<stdout>";
    public static final String SYSTEM_ERROR = "<stderr>";
    private static final Pattern SYSTEM_INPUT_PATTERN = Pattern.compile("<stdin>(\\.(.+))?");

    public static void run(String[] args) {
        Feat.log().debug("running command-line interface");
        CLIArgumentParser argumentParser = new CLIArgumentParser(args); // todo: maybe instantiate earlier to get at verbosity?
        argumentParser.getCommand().run(argumentParser);
    }

    public static String getArgValue(final Iterator<String> iterator, final String arg) {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new IllegalArgumentException("No value specified for " + arg);
        }
    }

    public static <T> Optional<T> runInThread(Callable<T> method, Long timeout) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(method);
        try {
            return Optional.of(timeout == null ? future.get() : future.get(timeout, TimeUnit.MILLISECONDS));
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
        return SYSTEM_INPUT_PATTERN.matcher(pathOrStdin.toLowerCase()).matches() || Files.exists(Paths.get(pathOrStdin));
    }

    public static <T> Result<T> loadFile(String pathOrStdin, FormatSupplier<T> formatSupplier) {
        Matcher matcher = SYSTEM_INPUT_PATTERN.matcher(pathOrStdin.toLowerCase());
        if (matcher.matches()) {
            Path path = Paths.get("<stdin>." + matcher.group(2));
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
            if (pathOrStdout.equalsIgnoreCase(SYSTEM_OUTPUT)) {
                IO.save(object, System.out, format);
            } else {
                IO.save(object, Paths.get(pathOrStdout), format);
            }
        } catch (final IOException e) {
            Feat.log().error(e);
        }
    }
}
