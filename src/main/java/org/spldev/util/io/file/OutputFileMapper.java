package org.spldev.util.io.file;

import org.spldev.util.data.Problem;
import org.spldev.util.data.Result;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OutputFileMapper extends FileMapper<OutputFile> {
	protected final Function<Path, Result<OutputFile>> fileCreator;

	public OutputFileMapper(Map<Path, OutputFile> fileMap, Path mainPath,
		Function<Path, Result<OutputFile>> fileCreator) {
		super(fileMap, mainPath);
		Objects.requireNonNull(fileCreator);
		this.fileCreator = fileCreator;
	}

	public static OutputFileMapper ofOutputStream(OutputStream outputStream, Charset charset, Options... options) {
		return new OutputFileMapper(
			Map.of(DEFAULT_MAIN_PATH, new OutputFile(outputStream, charset)),
			DEFAULT_MAIN_PATH,
			path -> Result.empty(new Problem(
				"cannot guess kind of requested output stream",
				Problem.Severity.ERROR)));
	}

	public static OutputFileMapper ofFiles(List<Path> paths, Path rootPath, Path mainPath, Charset charset,
		Options... options) throws IOException {
		checkParameters(paths, rootPath, mainPath);
		Map<Path, OutputFile> fileMap = new HashMap<>();
		for (Path currentPath : paths) {
			fileMap.put(relativizeRootPath(rootPath, currentPath), new OutputFile(currentPath, charset));
		}
		return new OutputFileMapper(fileMap, relativizeRootPath(rootPath, mainPath), path -> {
			if (!Arrays.asList(options).contains(Options.ALLOW_CREATE))
				return Result.empty(new Problem("not allowed to create new files", Problem.Severity.ERROR));
			try {
				return Result.of(new OutputFile(resolveRootPath(rootPath, path), charset));
			} catch (IOException e) {
				return Result.empty(e);
			}
		});
	}

	public static OutputFileMapper of(Path mainPath, Charset charset, Options... options) throws IOException {
		return Arrays.asList(options).contains(Options.INCLUDE_HIERARCHY) ? ofFileHierarchy(mainPath, charset, options)
			: ofFile(mainPath, charset, options);
	}

	public static OutputFileMapper ofFile(Path mainPath, Charset charset, Options... options) throws IOException {
		return ofFiles(List.of(mainPath), mainPath.getParent(), mainPath, charset, options);
	}

	public static OutputFileMapper ofFileHierarchy(Path mainPath, Charset charset, Options... options)
		throws IOException {
		Path rootPath = mainPath.getParent();
		return ofFiles(walkDirectory(rootPath), rootPath, mainPath, charset, options);
	}

	public static OutputFileMapper ofString(Charset charset, Options... options) {
		return new OutputFileMapper(
			Map.of(DEFAULT_MAIN_PATH, new OutputFile(charset)),
			DEFAULT_MAIN_PATH,
			path -> Result.of(new OutputFile(charset)));
	}

	public OutputFileMapper withMainFile(Path newMainPath) { // todo: return Result? // todo: handle relative paths / subdirs?
		if (createFile(newMainPath).isEmpty())
			throw new IllegalArgumentException("not allowed to create new files");
		return new OutputFileMapper(fileMap, newMainPath, fileCreator);
	}

	public Result<OutputFile> createFile(Path path) {
		Optional<OutputFile> file = getFile(path);
		if (file.isPresent())
			return Result.of(file.get());
		Result<OutputFile> fileResult = fileCreator.apply(path);
		if (fileResult.isPresent())
			fileMap.put(path, fileResult.get());
		return fileResult;
	}

	public Map<Path, OutputStream> getOutputStreams() {
		return fileMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getOutputStream()));
	}
}
