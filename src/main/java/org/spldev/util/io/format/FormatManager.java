package org.spldev.util.io.format;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.spldev.util.*;
import org.spldev.util.extension.*;
import org.spldev.util.io.*;

/**
 * Manages additional formats for a certain object.
 *
 * @author Sebastian Krieter
 */
public class FormatManager<T> extends ExtensionPoint<Format<T>> implements FormatSupplier<T> {

	public Optional<Format<T>> getFormatById(String id) throws NoSuchExtensionException {
		return getExtension(id);
	}

	public List<Format<T>> getFormatListForExtension(Path path) {
		if (path == null) {
			return Collections.emptyList();
		}
		return getFormatList(FileHandler.getFileExtension(path));
	}

	public List<Format<T>> getFormatListForExtension(String fileName) {
		if (fileName == null) {
			return Collections.emptyList();
		}
		return getFormatList(FileHandler.getFileExtension(fileName));
	}

	@Override
	public Result<Format<T>> getFormat(CharSequence content, final String fileExtension) {
		return getExtensions().stream()
			.filter(format -> fileExtension.equals(format.getFileExtension()))
			.filter(format -> format.supportsContent(content))
			.findFirst()
			.map(Result::of)
			.orElse(Result.empty(new NoSuchExtensionException("No suitable format found for file with file extension ."
				+ fileExtension)));
	}

	private List<Format<T>> getFormatList(final String fileExtension) {
		return getExtensions().stream()
			.filter(format -> format.supportsParse())
			.filter(format -> fileExtension.equals(format.getFileExtension()))
			.collect(Collectors.toList());
	}

}
