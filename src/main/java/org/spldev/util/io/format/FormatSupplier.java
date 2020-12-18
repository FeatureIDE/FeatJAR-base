package org.spldev.util.io.format;

import org.spldev.util.*;

/**
 * Provides a format for a given file content and file extension.
 * 
 * @author Sebastian Krieter
 */
@FunctionalInterface
public interface FormatSupplier<T> {

	static <T> FormatSupplier<T> of(Format<T> format) {
		return (content, extension) -> Result.of(format);
	}

	/**
	 * Returns the format that fits the given parameter.
	 *
	 * @param content       the file's content
	 * @param fileExtension the file extension
	 *
	 * @return A {@link Format format} that uses the given extension or {@code null}
	 *         if there is none.
	 */
	Result<Format<T>> getFormat(CharSequence content, String fileExtension);

}
