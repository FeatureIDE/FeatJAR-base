package org.spldev.util.io;

import org.junit.jupiter.api.Test;
import org.spldev.util.data.Problem;
import org.spldev.util.data.Result;
import org.spldev.util.io.file.InputFileMapper;
import org.spldev.util.io.file.OutputFileMapper;
import org.spldev.util.io.format.Format;
import org.spldev.util.io.file.FileMapper;
import org.spldev.util.tree.structure.SimpleTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FileHandlerTest {
	Path testPath = Paths.get("fileHandlerTest.dat");

	static class IntegerFormat implements Format<Integer> {
		@Override
		public String getFileExtension() {
			return "int";
		}

		@Override
		public String getName() {
			return "Integer";
		}

		@Override
		public boolean supportsParse() {
			return true;
		}

		@Override
		public boolean supportsSerialize() {
			return true;
		}

		@Override
		public Result<Integer> parse(InputFileMapper inputFileMapper) {
			return inputFileMapper.getMainFile().readText().map(Integer::valueOf);
		}

		@Override
		public String serialize(Integer object) {
			return object.toString();
		}
	}

	static class IntegerTreeFormat implements Format<SimpleTree<Integer>> {
		@Override
		public String getFileExtension() {
			return "int-tree";
		}

		@Override
		public String getName() {
			return "IntegerTree";
		}

		@Override
		public boolean supportsParse() {
			return true;
		}

		@Override
		public boolean supportsSerialize() {
			return true;
		}

		@Override
		public Format<SimpleTree<Integer>> getInstance() {
			return new IntegerTreeFormat();
		}

		@Override
		public Result<SimpleTree<Integer>> parse(InputFileMapper inputFileMapper) {
			List<Problem> problems = new ArrayList<>();
			List<String> lines = inputFileMapper.getMainFile().getLines().collect(Collectors.toList());
			if (lines.isEmpty())
				return Result.empty();
			SimpleTree<Integer> integerTree = new SimpleTree<>(Integer.valueOf(lines.remove(0)));
			for (String line : lines) {
				Result<SimpleTree<Integer>> result = getInstance().parse(inputFileMapper.withMainFile(Paths.get(line + ".dat")));
				if (result.isPresent())
					integerTree.addChild(result.get());
				else
					problems.add(new Problem("could not parse subtree", Problem.Severity.WARNING));
			}
			return Result.of(integerTree, problems);
		}

		@Override
		public String serialize(SimpleTree<Integer> object) {
			return object.getData().toString();
		}

		@Override
		public void write(SimpleTree<Integer> object, OutputFileMapper outputFileMapper) throws IOException {
			outputFileMapper.getMainFile().writeText(serialize(object) + "\n" +
					object.getChildren().stream().map(Object::hashCode).map(Objects::toString).collect(Collectors.joining("\n")));
			for (SimpleTree<Integer> child : object.getChildren()) {
				getInstance().write(child, outputFileMapper.withMainFile(Paths.get(child.hashCode() + ".dat")));
			}
		}
	}

	@Test
	public void integer() throws IOException {
		try {
			Result<Integer> result = FileHandler.load("42x", new IntegerFormat());
			assertFalse(result.isPresent());

			result = FileHandler.load("42", new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());

			FileHandler.save(42, testPath, new IntegerFormat());
			result = FileHandler.load(testPath, new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());

			String str = FileHandler.print(42, new IntegerFormat());
			result = FileHandler.load(str, new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			FileHandler.save(42, outputStream, new IntegerFormat());
			result = FileHandler.load(new ByteArrayInputStream(outputStream.toByteArray()), new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());
		} finally {
			Files.delete(testPath);
		}
	}

	@Test
	public void integerTree() throws IOException {
		try {
			SimpleTree<Integer> integerTree = new SimpleTree<>(1);
			integerTree.addChild(new SimpleTree<>(2));
			SimpleTree<Integer> child = new SimpleTree<>(3);
			integerTree.addChild(child);
			child.addChild(new SimpleTree<>(4));
			assertThrows(IllegalArgumentException.class, () -> FileHandler.save(integerTree, testPath, new IntegerTreeFormat()));
			assertDoesNotThrow(() -> FileHandler.save(integerTree, testPath, new IntegerTreeFormat(), FileMapper.Options.ALLOW_CREATE));

			assertThrows(IllegalArgumentException.class, () -> FileHandler.load(testPath, new IntegerTreeFormat()));
			Result<SimpleTree<Integer>> result = FileHandler.load(testPath, new IntegerTreeFormat(), FileMapper.Options.INCLUDE_HIERARCHY);
			assertTrue(result.isPresent());
			assertEquals(1, result.get().getData());
			assertEquals(2, result.get().getChildren().size());
            assertEquals(2, result.get().getFirstChild().get().getData());
			assertEquals(3, result.get().getLastChild().get().getData());
			assertEquals(4, result.get().getLastChild().get().getFirstChild().get().getData());

			Map<Path, String> stringMap = FileHandler.printHierarchy(result.get(), new IntegerTreeFormat());
			assertTrue(stringMap.get(Paths.get("__main__")).startsWith("1"));
		} finally {
			Files.walk(Paths.get(""), 1).forEach(path -> {
				if (path.getFileName().toString().endsWith(".dat")) {
					try {
						Files.delete(path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	// todo: subdirs
}
