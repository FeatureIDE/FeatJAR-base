package org.sk.utils.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reader for a simple format that stores a list of names as text (one per
 * line).
 * <p>
 * Get a sorted list of names with {@link #getNames()} and a sorted list of IDs
 * that correspond to the line numbers within the file with {@link #getIDs()}.
 * <p>
 * Names in the input file can ignore by either adding a tab character in front
 * or using the marker for comments: '#' for single line comment and '###' for
 * multi-line comment (end with '###' again).
 * 
 * @author Sebastian Krieter
 */
public class NameListReader {

	private static final String COMMENT = "#";
	private static final String STOP_MARK = "###";

	private List<String> systemNames;
	private List<Integer> systemIDs;

	public void read(Path file) throws IOException {
		List<String> lines = Files.readAllLines(file, Charset.defaultCharset());

		if (lines != null) {
			boolean pause = false;
			systemNames = new ArrayList<>(lines.size());
			systemIDs = new ArrayList<>(lines.size());
			int lineNumber = 0;
			for (String modelName : lines) {
				if (!modelName.trim().isEmpty()) {
					if (!modelName.startsWith("\t")) {
						if (modelName.startsWith(COMMENT)) {
							if (modelName.equals(STOP_MARK)) {
								pause = !pause;
							}
						} else if (!pause) {
							systemNames.add(modelName.trim());
							systemIDs.add(lineNumber);
						}
					}
				}
				lineNumber++;
			}
		} else {
			systemNames = Collections.<String>emptyList();
		}
	}

	public List<String> getNames() {
		return systemNames;
	}

	public List<Integer> getIDs() {
		return systemIDs;
	}

}
