package org.spldev.util.io;

import org.junit.jupiter.api.Test;
import org.spldev.util.data.Result;
import org.spldev.util.io.file.InputFileMapper;
import org.spldev.util.io.format.Format;
import org.spldev.util.io.file.FileMapper;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileHandlerTest {
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
	};

	@Test
	public void main() throws IOException {
		Result<Integer> result = FileHandler.load("42", new IntegerFormat());
		assertTrue(result.isPresent());
		assertEquals(42, result.get());
		FileHandler.save(42, Paths.get("fortytwo.int"), new IntegerFormat());
	}
}
