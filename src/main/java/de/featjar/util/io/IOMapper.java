/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
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
 * -----------------------------------------------------------------------------
 */
package de.featjar.util.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps paths (e.g., on the physical file system) to inputs or outputs (e.g.,
 * physical files) to represent hierarchies of data (e.g., a fragmented feature
 * model). Has at least one main {@link IOObject}. Formats can freely decide
 * whether to process any other IO objects. Can represent the physical or a
 * virtual file system.
 *
 * @param <T>
 * @author Elias Kuiter
 */
public abstract class IOMapper<T extends IOObject> implements AutoCloseable, Supplier<T> {
	public enum Options {
		/**
		 * Whether to map not only the given main file, but also all other files
		 * residing in the same directory. Only supported for parsing {@link Input.File}
		 * objects.
		 */
		INPUT_FILE_HIERARCHY,
		/**
		 * Whether to create a single ZIP archive instead of (several) physical files.
		 * Only supported for writing with OutputMapper.of.
		 */
		OUTPUT_FILE_ZIP,
		/**
		 * Whether to create a single JAR archive instead of (several) physical files.
		 * Only supported for writing with OutputMapper.of.
		 */
		OUTPUT_FILE_JAR
	}

	protected static final Path DEFAULT_MAIN_PATH = Paths.get("__main__");
	protected final Map<Path, T> ioMap = new HashMap<>();
	protected Path mainPath;

	protected IOMapper(Path mainPath) {
		Objects.requireNonNull(mainPath);
		this.mainPath = mainPath;
	}

	protected IOMapper(Map<Path, T> ioMap, Path mainPath) {
		this(mainPath);
		Objects.requireNonNull(ioMap);
		if (ioMap.get(mainPath) == null)
			throw new IllegalArgumentException("could not find main path " + mainPath);
		this.ioMap.putAll(ioMap);
	}

	protected static List<Path> getFilePathsInDirectory(Path rootPath) throws IOException {
		List<Path> paths;
		rootPath = rootPath != null ? rootPath : Paths.get("");
		try (Stream<Path> walk = Files.walk(rootPath)) {
			paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		}
		return paths;
	}

	protected static Path relativizeRootPath(Path rootPath, Path currentPath) {
		return rootPath != null ? rootPath.relativize(currentPath) : currentPath;
	}

	protected static Path resolveRootPath(Path rootPath, Path currentPath) {
		return rootPath != null ? rootPath.resolve(currentPath) : currentPath;
	}

	protected static void checkParameters(Collection<Path> paths, Path rootPath, Path mainPath) {
		if (rootPath != null && paths.stream().anyMatch(path -> !path.startsWith(
			rootPath))) {
			throw new IllegalArgumentException("all paths must start with the root path");
		} else if (!paths.contains(mainPath)) {
			throw new IllegalArgumentException("main path must be included");
		}
	}

	@Override
	public T get() {
		return ioMap.get(mainPath);
	}

	public Optional<T> get(Path path) {
		return Optional.ofNullable(ioMap.get(path));
	}

	public Optional<Path> getPath(T ioObject) {
		return ioMap.entrySet().stream()
			.filter(e -> Objects.equals(e.getValue(), ioObject))
			.findAny()
			.map(Map.Entry::getKey);
	}

	public Optional<Path> resolve(T relativeTo, Path path) {
		return getPath(relativeTo).map(_path -> _path.resolve(path));
	}

	@Override
	public void close() throws IOException {
		for (T ioObject : ioMap.values()) {
			ioObject.close();
		}
	}
}
