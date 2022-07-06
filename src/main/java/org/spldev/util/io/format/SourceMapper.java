package org.spldev.util.io.format;

import org.spldev.util.data.Problem;
import org.spldev.util.data.Result;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps paths (e.g., on the physical file system) to sources (e.g., physical
 * files) to represent hierarchies of source data (e.g., a fragmented feature
 * model). Has at least one main source. Formats can freely decide whether to
 * process any other sources.
 * 
 * @author Elias Kuiter
 */
public class SourceMapper implements AutoCloseable {
	public enum Options {
		INCLUDE_HIERARCHY, ALLOW_CREATE
	}

	protected static final Path DEFAULT_ROOT_PATH = Paths.get("");
	protected static final Path DEFAULT_MAIN_PATH = Paths.get("<main>");
	protected final Map<Path, Source> sourceMap = new HashMap<>();
	protected final Path mainPath;
	protected final Function<Path, Result<Source>> sourceCreator;

	protected SourceMapper(Map<Path, Source> sourceMap, Path mainPath, Function<Path, Result<Source>> sourceCreator) {
		Objects.requireNonNull(mainPath);
		Objects.requireNonNull(sourceMap);
		Objects.requireNonNull(sourceCreator);
		if (sourceMap.get(mainPath) == null)
			throw new IllegalArgumentException("main source required");
		this.mainPath = mainPath;
		this.sourceCreator = sourceCreator;
		this.sourceMap.putAll(sourceMap);
	}

	protected SourceMapper(Source mainSource, Path mainPath, Function<Path, Result<Source>> sourceCreator) {
		Objects.requireNonNull(mainPath);
		Objects.requireNonNull(mainSource);
		Objects.requireNonNull(sourceCreator);
		this.mainPath = mainPath;
		this.sourceCreator = sourceCreator;
		sourceMap.put(mainPath, mainSource);
	}

	public static SourceMapper of(InputStream inputStream, OutputStream outputStream, Charset charset,
		String fileExtension, Options... options) {
		return new SourceMapper(
			Map.of(DEFAULT_MAIN_PATH, new Source(inputStream, outputStream, charset, fileExtension)),
			DEFAULT_MAIN_PATH,
			path -> Result.empty(new Problem(
				"cannot guess kind of requested IO stream, please populate source mapper manually",
				Problem.Severity.ERROR)));
	}

	public static SourceMapper of(String text, Charset charset, String fileExtension, Options... options) {
		return ofString(text, charset, fileExtension, options);
	}

	public static SourceMapper of(Path mainPath, Charset charset, Options... options) throws IOException {
		return Arrays.asList(options).contains(Options.INCLUDE_HIERARCHY) ? ofFileHierarchy(mainPath, charset, options)
			: ofFile(mainPath, charset, options);
	}

	public static SourceMapper ofStrings(Path rootPath, Map<Path, String> pathStringMap, Path mainPath, Charset charset,
		String fileExtension, Options... options) {
		if (pathStringMap.keySet().stream().anyMatch(path -> !path.startsWith(rootPath)) || !pathStringMap.containsKey(
			mainPath)) {
			throw new IllegalArgumentException();
		}
		Map<Path, Source> sourceMap = new HashMap<>();
		for (Path currentPath : pathStringMap.keySet()) {
			sourceMap.put(rootPath.relativize(currentPath), new Source(pathStringMap.get(currentPath), charset,
				fileExtension));
		}
		return new SourceMapper(sourceMap, rootPath.relativize(mainPath), path -> Result.of(new Source("", charset,
			fileExtension)));
	}

	public static SourceMapper ofString(String text, Charset charset, String fileExtension, Options... options) {
		return ofStrings(DEFAULT_ROOT_PATH, Map.of(DEFAULT_MAIN_PATH, text), DEFAULT_MAIN_PATH, charset, fileExtension,
			options);
	}

	public static SourceMapper ofFiles(Path rootPath, List<Path> paths, Path mainPath, Charset charset,
		Options... options) throws IOException {
		if (paths.stream().anyMatch(path -> !Files.isRegularFile(path) || !path.startsWith(rootPath)) || !paths
			.contains(mainPath) || !Files.isDirectory(rootPath)) {
			throw new IllegalArgumentException();
		}
		Map<Path, Source> sourceMap = new HashMap<>();
		for (Path currentPath : paths) {
			sourceMap.put(rootPath.relativize(currentPath), new Source(currentPath, charset));
		}
		return new SourceMapper(sourceMap, rootPath.relativize(mainPath), path -> {
			try {
				if (!Arrays.asList(options).contains(Options.ALLOW_CREATE))
					return Result.empty(new Problem("not allowed to create new sources", Problem.Severity.ERROR));
				return Result.of(new Source(rootPath.resolve(path), charset));
			} catch (IOException e) {
				return Result.empty(e);
			}
		});
	}

	public static SourceMapper ofFile(Path mainPath, Charset charset, Options... options) throws IOException {
		return ofFiles(mainPath.getParent(), List.of(mainPath), mainPath, charset, options);
	}

	public static SourceMapper ofFileHierarchy(Path mainPath, Charset charset, Options... options) throws IOException {
		Path rootPath = mainPath.getParent();
		List<Path> paths;
		try (Stream<Path> walk = Files.walk(rootPath)) {
			paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		}
		return ofFiles(rootPath, paths, mainPath, charset, options);
	}

	public Source getMainSource() {
		return sourceMap.get(mainPath);
	}

	public Optional<Source> getSource(Path path) {
		return Optional.ofNullable(sourceMap.get(path));
	}

	public Optional<Path> getPath(Source source) {
		return sourceMap.entrySet().stream()
			.filter(e -> Objects.equals(e.getValue(), source))
			.findAny()
			.map(Map.Entry::getKey);
	}

	public Result<Source> createSource(Path path) {
		Optional<Source> source = getSource(path);
		if (source.isPresent())
			return Result.of(source.get());
		Result<Source> sourceResult = sourceCreator.apply(path);
		sourceResult.addProblem(new Problem("created new source at " + path, Problem.Severity.INFO));
		if (sourceResult.isPresent())
			sourceMap.put(path, sourceResult.get());
		return sourceResult;
	}

	public Optional<Source> resolveSource(Source relativeTo, Path path) {
		return getSource(getPath(relativeTo)
			.orElseThrow(() -> new IllegalArgumentException("can only resolve against known source"))
			.resolve(path));
	}

	@Override
	public void close() throws IOException {
		for (Source source : sourceMap.values()) {
			source.close();
		}
	}
}
