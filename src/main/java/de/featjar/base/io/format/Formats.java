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
import de.featjar.base.extension.ExtensionPoint;
import de.featjar.base.io.IOObject;
import de.featjar.base.io.InputHeader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages formats.
 * Should be extended to manage formats for a specific kind of object.
 *
 * @param <T> the type of the read/written object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class Formats<T> extends ExtensionPoint<Format<T>> implements FormatSupplier<T> {
    /**
     * {@return the format known by the given identifier}
     *
     * @param identifier the identifier
     */
    public Result<Format<T>> getFormatByIdentifier(String identifier) {
        return getExtension(identifier);
    }

    /**
     * {@return all formats that support a given file extension}
     *
     * @param fileExtension the file extension
     */
    public List<Format<T>> getFormatList(final String fileExtension) {
        return getExtensions().stream()
                .filter(Format::supportsParse)
                .filter(format -> Objects.equals(fileExtension, format.getFileExtension()))
                .collect(Collectors.toList());
    }

    /**
     * {@return all formats that support a given file path}
     *
     * @param path the path
     */
    public List<Format<T>> getFormatList(Path path) {
        return getFormatList(IOObject.getFileExtension(path).orElse(null));
    }

    @Override
    public Result<Format<T>> getFormat(InputHeader inputHeader) {
        return getExtensions().stream()
                .filter(format -> Objects.equals(inputHeader.getFileExtension().orElse(null), format.getFileExtension()))
                .filter(format -> format.supportsContent(inputHeader))
                .findFirst()
                .map(Result::of)
                .orElseGet(() ->
                        Result.empty(new Problem("No suitable format found for file extension \"."
                                + inputHeader.getFileExtension() + "\". Possible formats: " + getExtensions(),
                                Problem.Severity.ERROR)));
    }
}
