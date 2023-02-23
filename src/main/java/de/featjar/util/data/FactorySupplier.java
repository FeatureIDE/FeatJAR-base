/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.data;

import de.featjar.util.io.format.Format;
import java.nio.file.Path;

/**
 * Provides a factory for a given format and file path.
 *
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface FactorySupplier<T> {

    static <T> FactorySupplier<T> of(Factory<T> factory) {
        return (path, format) -> Result.of(factory);
    }

    /**
     * Returns the factory that fits the given parameter.
     *
     * @param path   the file path
     * @param format the file format
     *
     * @return A {@link Factory factory} that uses the given extension. Result may
     *         be empty if there is no suitable factory.
     */
    Result<Factory<T>> getFactory(Path path, Format<T> format);
}
