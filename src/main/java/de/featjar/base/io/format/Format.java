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
package de.featjar.base.io.format;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.extension.IExtension;
import de.featjar.base.io.InputHeader;
import de.featjar.base.io.InputMapper;
import de.featjar.base.io.Output;
import de.featjar.base.io.OutputMapper;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Parses and serializes objects.
 * For parsing, one or multiple {@link de.featjar.base.io.Input inputs} are read from an {@link InputMapper}.
 * For serializing, one or multiple {@link Output outputs} are written to an {@link OutputMapper} or a {@link String}.
 *
 * @param <T> the type of the read/written object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Format<T> extends IExtension {

    /**
     * Parses the content of an {@link InputMapper} into a new object.
     *
     * @param inputMapper the input mapper
     * @return the parsed result
     */
    default Result<T> parse(InputMapper inputMapper) {
        return Result.empty();
    }

    /**
     * Parses the content of an {@link InputMapper} into a supplied object.
     *
     * @param inputMapper the input mapper
     * @param supplier    the supplier
     * @return the parsed result
     */
    default Result<T> parse(InputMapper inputMapper, Supplier<T> supplier) {
        return parse(inputMapper);
    }

    /**
     * {@return the given object serialized into a string}
     *
     * @param object the object
     */
    default Result<String> serialize(T object) {
        return Result.empty();
    }

    /**
     * Writes the given object to an {@link OutputMapper}.
     *
     * @param object the object
     * @param outputMapper  the output mapper
     */
    default void write(T object, OutputMapper outputMapper) throws IOException {
        String string = serialize(object)
                // todo: improve exception handling - this should maybe be a Result instead?
                .orElseThrow(p -> new IOException(p.stream()
                        .map(Problem::toString).collect(Collectors.joining())));
        outputMapper.get().write(string);
    }

    /**
     * {@return the file extension for this format, if any}
     * There should be no leading ".".
     * The file extension is used to detect whether this format supports parsing a given file in {@link Formats}.
     * If omitted, this format supports files without extensions.
     */
    default String getFileExtension() {
        return null;
    }

    /**
     * {@return a meaningful name for this format}
     */
    String getName();

    /**
     * {@return an instance of this format}
     * Call this method before {@link #parse(InputMapper)}, {@link #parse(InputMapper, Supplier)},
     * {@link #serialize(Object)}, or {@link #write(Object, OutputMapper)} to avoid unintended concurrent access.
     * Implementing classes may return {@code this} if {@link #parse(InputMapper)} and
     * {@link #serialize(Object)} are implemented without state (i.e., non-static fields).*/
    default Format<T> getInstance() {
        return this;
    }

    /**
     * {@return whether this format supports {@link #serialize(Object)}}
     */
    default boolean supportsParse() {
        return false;
    }

    /**
     * {@return whether this format supports {@link #write(Object, OutputMapper)}}}
     */
    default boolean supportsSerialize() {
        return false;
    }

    /**
     * {@return whether this format supports parsing input with the given input header}
     *
     * @param inputHeader the input header
     */
    default boolean supportsContent(InputHeader inputHeader) {
        return supportsParse();
    }
}
