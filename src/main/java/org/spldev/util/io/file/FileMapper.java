package org.spldev.util.io.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps paths (e.g., on the physical file system) to inputs or outputs (e.g.,
 * physical files) to represent hierarchies of data (e.g., a fragmented feature
 * model). Has at least one main file. Formats can freely decide whether to
 * process any other files. Can represent the physical or a virtual file system.
 *
 * @param <T>
 * @author Elias Kuiter
 */
public abstract class FileMapper<T extends File> implements AutoCloseable {
	public enum Options {
		/**
		 * Whether to map not only the given main file, but also all other files
		 * residing in the same directory. Only considered by the .of(Path, ...)
		 * methods.
		 */
		INCLUDE_HIERARCHY,
		/**
		 * Whether creating new files on the physical file system is allowed. Only
		 * considered by the OutputFileMapper.of(Path, ...) method.
		 */
		ALLOW_CREATE,
		/**
		 * Whether to write files into a ZIP archive instead of physical files.
		 * Only considered by the OutputFileMapper.of(Path, ...) method.
		 */
		CREATE_ZIP
	}

	protected static final Path DEFAULT_MAIN_PATH = Paths.get("__main__");
	protected final Map<Path, T> fileMap = new HashMap<>();
	protected final Path mainPath;

	protected FileMapper(Map<Path, T> fileMap, Path mainPath) {
		Objects.requireNonNull(mainPath);
		Objects.requireNonNull(fileMap);
		if (fileMap.get(mainPath) == null)
			throw new IllegalArgumentException("main file required");
		this.mainPath = mainPath;
		this.fileMap.putAll(fileMap);
	}

	protected static List<Path> walkDirectory(Path rootPath) throws IOException {
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

	protected static void checkParameters(List<Path> paths, Path rootPath, Path mainPath) {
		if (rootPath != null && !Files.isDirectory(rootPath)) {
			throw new IllegalArgumentException("root path must be directory");
		} else if (rootPath != null && paths.stream().anyMatch(path -> !Files.isRegularFile(path) || !path.startsWith(
			rootPath))) {
			throw new IllegalArgumentException("all file paths must start with the root path");
		} else if (!paths.contains(mainPath)) {
			throw new IllegalArgumentException("main file paths must be included");
		}
	}

	public T getMainFile() {
		return fileMap.get(mainPath);
	}

	public Optional<T> getFile(Path path) {
		return Optional.ofNullable(fileMap.get(path));
	}

	public Optional<Path> getPath(T file) {
		return fileMap.entrySet().stream()
			.filter(e -> Objects.equals(e.getValue(), file))
			.findAny()
			.map(Map.Entry::getKey);
	}

	public Optional<T> resolveFile(T relativeTo, Path path) {
		return getFile(getPath(relativeTo)
			.orElseThrow(() -> new IllegalArgumentException("can only resolve against known file")) // todo: throw?
			.resolve(path));
	}

	@Override
	public void close() throws IOException {
		for (T file : fileMap.values()) {
			file.close();
		}
	}
}
