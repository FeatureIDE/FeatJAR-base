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
package org.spldev.util.io.namelist;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.spldev.util.data.Result;
import org.spldev.util.io.InputMapper;
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
	public Result<List<NameEntry>> parse(InputMapper inputMapper) {
		final List<String> lines = inputMapper.get().readLines();
		return parse(lines, new ArrayList<>(lines.size()));
	}

	@Override
	public Result<List<NameEntry>> parse(InputMapper inputMapper, Supplier<List<NameEntry>> supplier) {
		final List<String> lines = inputMapper.get().readLines();
		return parse(lines, supplier.get());
	}

	private Result<List<NameEntry>> parse(final List<String> lines, final List<NameEntry> entries) {
		int lineNumber = 0;
		boolean pause = false;
		for (final String modelName : lines) {
			lineNumber++;
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
		}
		return Result.of(entries);
	}

	@Override
	public String getIdentifier() {
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
