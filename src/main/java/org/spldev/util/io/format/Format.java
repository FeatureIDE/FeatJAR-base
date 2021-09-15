/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util Lib.
 * 
 * Util Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.io.format;

import java.io.*;
import java.util.function.*;

import org.spldev.util.*;
import org.spldev.util.extension.*;

/**
 * Interface for reading and writing data from and to arbitrary objects.
 * @param <T> type of read/written data
 * @author Sebastian Krieter
 */
public interface Format<T> extends Extension {

	/**
	 * Parses the contents of the given source and transfers all information onto
	 * the given object. The object is intended to be completely overridden. A
	 * subclass may try to reset the information already stored inside the object,
	 * but is not obligated to do so. Thus, if possible an empty object should be
	 * passed here.
	 *
	 * @param source the source content.
	 * @return A {@link Result} containing the parsed object or a list of problems
	 *         that occurred during the parsing process.
	 *
	 * @see #supportsParse()
	 */
	default Result<T> parse(Input source) {
		throw new UnsupportedOperationException();
	}

	default Result<T> parse(Input source, Supplier<T> supplier) {
		return parse(source);
	}

	/**
	 * Writes the information of an object to a string. (Which information are
	 * considered is specified by the implementing class).
	 *
	 * @param object the object to get the information from.
	 * @return A string representing the object in this format.
	 *
	 * @see #supportsSerialize()
	 */
	default String serialize(T object) {
		throw new UnsupportedOperationException();
	}

	default void write(T object, Output out) throws IOException {
		out.writeText(serialize(object));
	}

	/**
	 * Returns the file extension for this format. (Without a leading ".")
	 *
	 * @return A string representing this format's file extension.
	 */
	String getFileExtension();

	/**
	 * Returns a meaningful name for this format. This is intended for user
	 * interfaces (e.g., in dialogs).
	 *
	 * @return A string representing this format's name.
	 */
	String getName();

	/**
	 * Returns an instance of this format. Clients should always call this method
	 * before calling any of {@link #parse(Input)}, {@link #parse(Input,
	 * Supplier<T>)}, {@link #serialize(Object)}, or {@link #write(Object, Output)}
	 * and call these methods the returned value to avoid any unintended concurrent
	 * access.<br>
	 * <br>
	 * <b>Example</b> <code>
	 * Format&lt;?&gt; format = getFormat();
	 * format.getInstance().write(new Object())</code> Implementing classes may
	 * return {@code this}, if {@code read} and {@code write} are implemented in a
	 * static fashion (i.e., do not use any non-static fields).
	 *
	 * @return An instance of this format.
	 */
	default Format<T> getInstance() {
		return this;
	}

	default boolean isBinary() {
		return false;
	}

	/**
	 * Returns whether this format supports the {@link #parse(Input)} operation.
	 *
	 * @return {@code true} if {@code read} is allowed by this format, {@code false}
	 *         otherwise.
	 */
	default boolean supportsParse() {
		return false;
	}

	/**
	 * Returns whether this format supports the {@link #serialize(Object)}
	 * operation.
	 *
	 * @return {@code true} if {@code write} is allowed by this format,
	 *         {@code false} otherwise.
	 */
	default boolean supportsSerialize() {
		return false;
	}

	/**
	 * Returns whether this format supports the {@link #write(Object, Output)}
	 * operation.
	 *
	 * @return {@code true} if {@code write} is allowed by this format,
	 *         {@code false} otherwise.
	 */
	default boolean supportsWrite() {
		return supportsSerialize();
	}

	/**
	 * Returns whether this format supports the parsing of the given content.
	 *
	 * @param inputHeader The beginning of the content to be parsed.
	 * @return {@code true} if the content should be parsable by this format,
	 *         {@code false} otherwise.
	 */
	default boolean supportsContent(InputHeader inputHeader) {
		return supportsParse();
	}

}
