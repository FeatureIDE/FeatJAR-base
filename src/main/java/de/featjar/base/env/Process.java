/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Executes an external executable in a process.
 *
 * @author Elias Kuiter
 */
public class Process implements Supplier<Result<List<String>>> {
    protected final Path executablePath;
    protected final List<String> arguments;
    protected final Duration timeout;
    protected boolean errorOccurred;

    public Process(Path executablePath, String... arguments) {
        this(executablePath, List.of(arguments));
    }

    public Process(Path executablePath, List<String> arguments) {
        this(executablePath, arguments, null);
    }

    public Process(Path executablePath, List<String> arguments, Duration timeout) {
        this.executablePath = Objects.requireNonNull(executablePath);
        this.arguments = arguments == null ? List.of() : arguments;
        this.timeout = timeout;
    }

    @Override
    public Result<List<String>> get() {
        List<String> output = new ArrayList<>();
        Result<Void> result = run(output::add, output::add);
        return result.map(r -> output);
    }

    public Result<Void> run(String input, Consumer<String> outConsumer, Consumer<String> errConsumer) {
        List<String> command = new ArrayList<>();
        command.add(executablePath.toString());
        command.addAll(arguments);
        FeatJAR.log().debug(String.join(" ", command));
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        java.lang.Process process = null;
        try {
            Instant start = Instant.now();
            process = processBuilder.start();
            if (input != null) {
                process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
                process.getOutputStream().close();
            }
            consumeInputStream(process.getInputStream(), outConsumer, false);
            consumeInputStream(process.getErrorStream(), errConsumer, true);
            boolean terminatedInTime = true;
            if (timeout != null && !timeout.isZero())
                terminatedInTime = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            else process.waitFor();
            long elapsedTime = Duration.between(start, Instant.now()).toMillis();
            final int exitValue = process.exitValue();
            Result<Void> result;
            if (!errorOccurred) {
                result = Result.ofVoid();
            } else {
                result = Result.empty(
                        new Problem(executablePath + " exited with value " + exitValue, Problem.Severity.ERROR));
            }
            return Result.empty(
                            new Problem("exit code = " + exitValue, Problem.Severity.INFO),
                            new Problem("in time = " + terminatedInTime, Problem.Severity.INFO),
                            new Problem("elapsed time in ms = " + elapsedTime, Problem.Severity.INFO))
                    .merge(result);
        } catch (IOException | InterruptedException e) {
            return Result.empty(e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }

    public Result<Void> run(Consumer<String> outConsumer, Consumer<String> errConsumer) {
        return run(null, outConsumer, errConsumer);
    }

    protected void consumeInputStream(InputStream inputStream, Consumer<String> consumer, boolean isError) {
        if (consumer != null) {
            new Thread(() -> {
                        try (BufferedReader reader =
                                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                                consumer.accept(line);
                                if (isError) errorOccurred = true;
                            }
                        } catch (final IOException e) {
                            FeatJAR.log().error(e);
                        }
                    })
                    .start();
        }
    }
}
