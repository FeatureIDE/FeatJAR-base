package org.spldev.util.io;

import org.junit.jupiter.api.Test;
import org.spldev.util.data.Problem;
import org.spldev.util.data.Result;
import org.spldev.util.io.format.Format;
import org.spldev.util.tree.structure.SimpleTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class IOTest {
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
		public Result<Integer> parse(InputMapper inputMapper) {
			return inputMapper.get().readText().map(Integer::valueOf);
		}

		@Override
		public String serialize(Integer object) {
			return object.toString();
		}
	}

	static class IntegerTreeFormat implements Format<SimpleTree<Integer>> {
		@Override
		public String getFileExtension() {
			return "dat";
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
		public Result<SimpleTree<Integer>> parse(InputMapper inputMapper) {
			List<Problem> problems = new ArrayList<>();
			List<String> lines = inputMapper.get().readLines();
			if (lines.isEmpty())
				return Result.empty();
			SimpleTree<Integer> integerTree = new SimpleTree<>(Integer.valueOf(lines.remove(0)));
			for (String line : lines) {
				Result<SimpleTree<Integer>> result = inputMapper.withMainPath(IOObject.getPathWithExtension(line, getFileExtension()), () -> getInstance().parse(inputMapper));
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
		public void write(SimpleTree<Integer> object, OutputMapper outputMapper) throws IOException {
			outputMapper.get().writeText(serialize(object) + "\n" +
					object.getChildren().stream().map(Object::hashCode).map(Objects::toString).collect(Collectors.joining("\n")));
			for (SimpleTree<Integer> child : object.getChildren()) {
				outputMapper.withMainPath(IOObject.getPathWithExtension(String.valueOf(child.hashCode()), getFileExtension()), () -> getInstance().write(child, outputMapper));
			}
		}
	}

	public void testInteger(Path testPath) throws IOException {
		try {
			Result<Integer> result = IO.load("42x", new IntegerFormat());
			assertFalse(result.isPresent());

			result = IO.load("42", new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());

			IO.save(42, testPath, new IntegerFormat());
			result = IO.load(testPath, new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());

			String str = IO.print(42, new IntegerFormat());
			result = IO.load(str, new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IO.save(42, outputStream, new IntegerFormat());
			result = IO.load(new ByteArrayInputStream(outputStream.toByteArray()), new IntegerFormat());
			assertTrue(result.isPresent());
			assertEquals(42, result.get());
		} finally {
			Files.delete(testPath);
		}
	}

	@Test
	public void integer() throws IOException {
		testInteger(Paths.get("fileHandlerTest.dat"));
		testInteger(Paths.get("./fileHandlerTest.dat"));
		//testInteger(Paths.get("temp/fileHandlerTest.dat"));
	}

	public void testIntegerTree(Path testPath) throws IOException {
		try {
			SimpleTree<Integer> integerTree = new SimpleTree<>(1);
			integerTree.addChild(new SimpleTree<>(2));
			SimpleTree<Integer> child = new SimpleTree<>(3);
			integerTree.addChild(child);
			child.addChild(new SimpleTree<>(4));
			assertDoesNotThrow(() -> IO.save(integerTree, testPath, new IntegerTreeFormat()));

			Result<SimpleTree<Integer>> result = IO.load(testPath, new IntegerTreeFormat());
			assertTrue(result.isPresent());
			assertEquals(1, result.get().getData());
			assertEquals(0, result.get().getChildren().size());
			assertEquals(2, result.getProblems().size());
			result = IO.load(testPath, new IntegerTreeFormat(), IOMapper.Options.INPUT_FILE_HIERARCHY);
			assertTrue(result.isPresent());
			assertEquals(1, result.get().getData());
			assertEquals(2, result.get().getChildren().size());
            assertEquals(2, result.get().getFirstChild().get().getData());
			assertEquals(3, result.get().getLastChild().get().getData());
			assertEquals(4, result.get().getLastChild().get().getFirstChild().get().getData());

			Map<Path, String> stringMap = IO.printHierarchy(result.get(), new IntegerTreeFormat());
			assertTrue(stringMap.get(Paths.get("__main__")).startsWith("1"));

			assertDoesNotThrow(() -> IO.save(integerTree, testPath, new IntegerTreeFormat(), IOMapper.Options.OUTPUT_FILE_ZIP));
			assertDoesNotThrow(() -> IO.save(integerTree, testPath, new IntegerTreeFormat(), IOMapper.Options.OUTPUT_FILE_JAR));
		} finally {
			Files.walk(testPath.getParent() == null ? Paths.get("") : testPath.getParent(), 1).forEach(path -> {
				if (path.getFileName().toString().endsWith(".dat") || path.getFileName().toString().endsWith(".jar") || path.getFileName().toString().endsWith(".zip")) {
					try {
						Files.delete(path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	@Test
	public void integerTree() throws IOException {
		testIntegerTree(Paths.get("fileHandlerTest.dat"));
		testIntegerTree(Paths.get("./fileHandlerTest.dat"));
		//testInteger(Paths.get("temp/fileHandlerTest.dat"));
	}

	// todo: subdirs

	// todo: absolute paths

}