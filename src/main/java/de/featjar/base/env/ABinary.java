/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.env;

import de.featjar.base.data.Result;
import de.featjar.base.extension.IExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

/**
 * A native binary bundled with FeatJAR.
 * Right now, whenever a native binary is needed, it is extracted into the user's home directory.
 * This is necessary so the binary can be executed.
 * Only the binaries required on the current operating system should be extracted.
 * For now, the binaries are just kept in the home directory indefinitely.
 *
 * @author Elias Kuiter
 */
public abstract class ABinary implements IExtension {
    /**
     * The directory used to store native binaries.
     */
    public static final Path BINARY_DIRECTORY = Paths.get(HostEnvironment.HOME_DIRECTORY, ".featjar-bin");

    /**
     * Initializes a native binary by extracting all its resources into the binary directory.
     */
    public ABinary() throws IOException {
        extractResources(getResourceNames());
    }

    /**
     * {@return the names of all resources (i.e., executables and libraries) to be extracted for this binary}
     * All names are relative to the {@code src/main/resources/bin} directory.
     */
    protected abstract LinkedHashSet<String> getResourceNames();

    /**
     * {@return the name of this binary's executable}
     * Returns {@code null} if this binary has no executable (i.e., it only provides library files).
     */
    protected String getExecutableName() {
        return null;
    }

    /**
     * {@return the path to this binary's executable, if any}
     * Returns {@code null} if this binary has no executable (i.e., it only provides library files).
     */
    public Path getExecutablePath() {
        return getExecutableName() != null ? BINARY_DIRECTORY.resolve(getExecutableName()) : null;
    }

    /**
     * Executes this binary's executable with the given arguments.
     * Creates a process and waits until it exits.
     *
     * @param arguments the arguments passed to this binary's executable
     *                  {@return the output of the process as a line stream, if any}
     */
    public Process getProcess(List<String> arguments, Duration timeout) {
        return new Process(getExecutablePath(), arguments, timeout);
    }

    /**
     * Executes this binary's executable with the given arguments.
     * Creates a process and waits until it exits.
     *
     * @param arguments the arguments passed to this binary's executable
     *                  {@return the output of the process as a line stream, if any}
     */
    public Process getProcess(String... arguments) {
        return new Process(getExecutablePath(), arguments);
    }

    /**
     * Runs a function (e.g., this binary's executable) that gets access to a temporary file.
     * The file is created in the default temporary-file directory and deleted after the function is done.
     *
     * @param prefix the prefix of the temporary file's name
     * @param suffix the suffix of the temporary file's name
     * @param fn     the function
     * @param <T>    the type of the returned result
     * @return the result returned by the function, if any
     */
    public <T> Result<T> withTemporaryFile(String prefix, String suffix, Function<Path, Result<T>> fn) {
        Path temporaryFilePath = null;
        try {
            temporaryFilePath = Files.createTempFile(prefix, suffix);
            return fn.apply(temporaryFilePath);
        } catch (IOException e) {
            return Result.empty(e);
        } finally {
            if (temporaryFilePath != null) {
                try {
                    Files.deleteIfExists(temporaryFilePath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Extracts this binary's resources into the binary directory.
     * Each resource is set to be executable.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void extractResources(LinkedHashSet<String> resourceNames) throws IOException {
        BINARY_DIRECTORY.toFile().mkdirs();
        for (String resourceName : resourceNames) {
            Path outputPath = BINARY_DIRECTORY.resolve(resourceName);
            if (Files.notExists(outputPath)) {
                JARs.extractResource("bin/" + resourceName, outputPath);
                outputPath.toFile().setExecutable(true);
            }
        }
    }
}
