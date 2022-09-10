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
package de.featjar.util.io.format;

import de.featjar.util.data.Result;
import de.featjar.util.extension.Extension;
import de.featjar.util.io.InputHeader;
import de.featjar.util.io.InputMapper;
import de.featjar.util.io.Output;
import de.featjar.util.io.OutputMapper;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Parses and serializes objects.
 * For parsing, one or multiple {@link de.featjar.util.io.Input inputs} are read from an {@link InputMapper}.
 * For serializing, one or multiple {@link Output outputs} are written to an {@link OutputMapper} or a {@link String}.
 *
 * @param <T> the type of the read/written object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Format<T> extends Extension {

    /**
     * Parses the content of an {@link InputMapper} into a new object.
     *
     * @param inputMapper the input mapper
     * @return the parsed result
     */
    default Result<T> parse(InputMapper inputMapper) {
        throw new UnsupportedOperationException();
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
    default String serialize(T object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes the given object to an {@link OutputMapper}.
     *
     * @param object the object
     * @param outputMapper  the output mapper
     */
    default void write(T object, OutputMapper outputMapper) throws IOException {
        outputMapper.get().write(serialize(object));
    }

    /**
     * {@return the file extension for this format, if any}
     * Omits a leading ".".
     */
    Optional<String> getFileExtension();

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
