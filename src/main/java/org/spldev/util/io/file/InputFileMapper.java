package org.spldev.util.io.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputFileMapper extends FileMapper<InputFile> {
	public InputFileMapper(Map<Path, InputFile> fileMap, Path mainPath) {
		super(fileMap, mainPath);
	}

	public static InputFileMapper ofInputStream(InputStream inputStream, Charset charset,
		String fileExtension, Options... options) {
		return new InputFileMapper(
			Map.of(DEFAULT_MAIN_PATH, new InputFile(inputStream, charset, fileExtension)),
			DEFAULT_MAIN_PATH);
	}

	public static InputFileMapper ofFiles(List<Path> paths, Path rootPath, Path mainPath, Charset charset,
		Options... options) throws IOException {
		checkParameters(paths, rootPath, mainPath);
		Map<Path, InputFile> fileMap = new HashMap<>();
		for (Path currentPath : paths) {
			fileMap.put(relativizeRootPath(rootPath, currentPath), new InputFile(currentPath, charset));
		}
		return new InputFileMapper(fileMap, relativizeRootPath(rootPath, mainPath));
	}

	public static InputFileMapper of(Path mainPath, Charset charset, Options... options) throws IOException {
		return Arrays.asList(options).contains(Options.INCLUDE_HIERARCHY) ? ofFileHierarchy(mainPath, charset, options)
			: ofFile(mainPath, charset, options);
	}

	public static InputFileMapper ofFile(Path mainPath, Charset charset, Options... options) throws IOException {
		return ofFiles(List.of(mainPath), mainPath.getParent(), mainPath, charset, options);
	}

	public static InputFileMapper ofFileHierarchy(Path mainPath, Charset charset, Options... options)
		throws IOException {
		Path rootPath = mainPath.getParent();
		return ofFiles(walkDirectory(rootPath), rootPath, mainPath, charset, options);
	}

	public static InputFileMapper ofStrings(Map<Path, String> pathStringMap, Path rootPath, Path mainPath,
		Charset charset,
		String fileExtension, Options... options) {
		if (rootPath != null && pathStringMap.keySet().stream().anyMatch(path -> !path.startsWith(rootPath))) {
			throw new IllegalArgumentException("all file paths must start with the root path");
		} else if (!pathStringMap.containsKey(mainPath)) {
			throw new IllegalArgumentException("main file paths must be included");
		}
		Map<Path, InputFile> fileMap = new HashMap<>();
		for (Path currentPath : pathStringMap.keySet()) {
			fileMap.put(relativizeRootPath(rootPath, currentPath), new InputFile(pathStringMap.get(currentPath),
				charset,
				fileExtension));
		}
		return new InputFileMapper(fileMap, relativizeRootPath(rootPath, mainPath));
	}

	public static InputFileMapper ofString(String text, Charset charset, String fileExtension, Options... options) {
		return ofStrings(Map.of(DEFAULT_MAIN_PATH, text), null, DEFAULT_MAIN_PATH, charset, fileExtension,
			options);
	}

	public InputFileMapper withMainFile(Path newMainPath) { // todo: return Result?
		if (getFile(newMainPath).isEmpty())
			throw new IllegalArgumentException("file " + newMainPath + " not mapped");
		return new InputFileMapper(fileMap, newMainPath);
	}
}
