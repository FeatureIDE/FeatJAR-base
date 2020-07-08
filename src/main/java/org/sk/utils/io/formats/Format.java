package org.sk.utils.io.formats;

import org.sk.utils.extension.Extension;

/**
 * Interface for reading and writing data from and to arbitrary objects.
 *
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
	 * @return A list of {@link ParseProblem problems} that occurred during the parsing
	 *         process.
	 *
	 * @see #supportsParse()
	 */
	default ParseResult<T> parse(CharSequence source) {
		throw new UnsupportedOperationException();
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
	 * before calling {@link #parse(CharSequence)} or {@link #serialize(Object)}
	 * and call these methods the returned value to avoid any unintended concurrent
	 * access.<br>
	 * <br>
	 * <b>Example</b> <code>
	 * IPersistentFormat&lt;?&gt; format = getFormat();
	 * format.getInstance().write(new Object())</code> Implementing classes may
	 * return {@code this}, if {@code read} and {@code write} are implemented in a
	 * static fashion (i.e., do not use any non-static fields).
	 *
	 * @return An instance of this format.
	 */
	default Format<T> getInstance() {
		return this;
	}

	/**
	 * Returns whether this format supports the {@link #parse(CharSequence)}
	 * operation.
	 *
	 * @return {@code true} if {@code read} is allowed by this format, {@code false}
	 *         otherwise.
	 */
	default boolean supportsParse() {
		return false;
	}

	/**
	 * Returns whether this format supports the {@link #serialize(Object)} operation.
	 *
	 * @return {@code true} if {@code write} is allowed by this format,
	 *         {@code false} otherwise.
	 */
	default boolean supportsSerialize() {
		return false;
	}

	/**
	 * Returns whether this format supports the parsing of the given content.
	 *
	 * @param content The content to be parsed.
	 * @return {@code true} if the content should be parsable by this format,
	 *         {@code false} otherwise.
	 */
	default boolean supportsContent(CharSequence content) {
		return supportsParse();
	}

}
