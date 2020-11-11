package org.spldev.util.io.format.namelist;

import java.util.*;

import org.spldev.util.io.format.*;

/**
 * Simple format that stores a list of names as text (one per line).
 * <p>
 * Get a list of names and IDs ordered by occurrence in the file. The IDs
 * correspond to the line numbers within the file.
 * <p>
 * Names in the input file can ignore by either adding a tab character in front
 * or using the marker for comments: '#' for single line comment and '###' for
 * multi-line comment (end with '###' again).
 * 
 * @author Sebastian Krieter
 */
public class NameListFormat implements Format<List<NameListFormat.NameEntry>> {

	public static class NameEntry {
		private final String name;
		private final int id;

		public NameEntry(String name, int id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public int getID() {
			return id;
		}

	}

	public static final String ID = NameListFormat.class.getCanonicalName();

	private static final String COMMENT = "#";
	private static final String STOP_MARK = "###";

	@Override
	public Optional<List<NameEntry>> parse(CharSequence source) {
		final String[] lines = source.toString().split("\\R");
		final ArrayList<NameEntry> entries = new ArrayList<>(lines.length);
		int lineNumber = 0;
		boolean pause = false;
		for (final String modelName : lines) {
			if (!modelName.trim().isEmpty()) {
				if (!modelName.startsWith("\t")) {
					if (modelName.startsWith(COMMENT)) {
						if (modelName.equals(STOP_MARK)) {
							pause = !pause;
						}
					} else if (!pause) {
						entries.add(new NameEntry(modelName, lineNumber));
					}
				}
			}
			lineNumber++;
		}
		return Optional.of(entries);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getFileExtension() {
		return "list";
	}

	@Override
	public String getName() {
		return "Name List";
	}

	@Override
	public boolean supportsParse() {
		return true;
	}

}
