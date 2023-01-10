package de.featjar.base.io;

import de.featjar.base.io.input.FileInput;
import de.featjar.base.io.output.AOutputMapper;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Options for an {@link AIOMapper}.
 */
public enum IOMapperOptions {
    /**
     * Whether to map not only the given main file, but also all other files residing in the same directory.
     * Only supported for parsing {@link FileInput} objects.
     */
    INPUT_FILE_HIERARCHY,
    /**
     * Whether to create a single ZIP archive instead of (several) physical files.
     * Only supported for writing with {@link AOutputMapper#of(Path, Charset, IOMapperOptions...)}.
     */
    OUTPUT_FILE_ZIP,
    /**
     * Whether to create a single JAR archive instead of (several) physical files.
     * Only supported for writing with {@link AOutputMapper#of(Path, Charset, IOMapperOptions...)}.
     */
    OUTPUT_FILE_JAR
}
