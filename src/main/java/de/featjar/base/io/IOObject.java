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
package de.featjar.base.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Input or output mapped by a {@link IOMapper}.
 * This could be a physical file, string, or stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IOObject extends AutoCloseable {
    @Override
    void close() throws IOException;

    /**
     * {@return a path's file name without its extension}
     *
     * @param path the path
     */
    static String getFileNameWithoutExtension(Path path) {
        return getFileNameWithoutExtension(path.getFileName().toString());
    }

    /**
     * {@return a full file name's file name without its extension}
     *
     * @param fileName the file name
     */
    static String getFileNameWithoutExtension(String fileName) {
        final int extensionIndex = fileName.lastIndexOf('.');
        return (extensionIndex > 0) ? fileName.substring(0, extensionIndex) : fileName;
    }

    /**
     * {@return a path's file extension, if any}
     * A dot at the first position of the file name is ignored.
     * E.g., ".file" has no extension, but ".file.txt" would return "txt".
     *
     * @param path the path
     */
    static Optional<String> getFileExtension(Path path) {
        return Optional.ofNullable(path).flatMap(_path -> getFileExtension(_path.getFileName().toString()));
    }

    /**
     * {@return a full file name's file extension, if any}
     * A dot at the first position of the file name is ignored.
     * E.g., ".file" has no extension, but ".file.txt" would return "txt".
     *
     * @param fileName the file name
     */
    static Optional<String> getFileExtension(String fileName) {
        if (fileName == null)
            return Optional.empty();
        final int extensionIndex = fileName.lastIndexOf('.');
        return Optional.ofNullable(extensionIndex > 0 ? fileName.substring(extensionIndex + 1) : null);
    }

    /**
     * {@return a file name with a replaced file extension}
     *
     * @param fileName the file name
     * @param fileExtension the new file extension
     */
    static String getFileNameWithExtension(String fileName, String fileExtension) {
        if (fileExtension == null)
            return IOObject.getFileNameWithoutExtension(fileName);
        return String.format("%s.%s", IOObject.getFileNameWithoutExtension(fileName), fileExtension);
    }

    /**
     * {@return a path with a replaced file extension}
     *
     * @param path the path
     * @param fileExtension the new file extension
     */
    static Path getPathWithExtension(Path path, String fileExtension) {
        return path.resolveSibling(getFileNameWithExtension(path.getFileName().toString(), fileExtension));
    }

    /**
     * {@return a path with a replaced file extension}
     *
     * @param fileName the file name
     * @param fileExtension the new file extension
     */
    static Path getPathWithExtension(String fileName, String fileExtension) {
        return Paths.get(getFileNameWithExtension(fileName, fileExtension));
    }
}
