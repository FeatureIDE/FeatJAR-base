package org.spldev.util.io.file;

import java.io.IOException;

/**
 * File (virtual or physical) mapped by a {@link FileMapper}.
 *
 * @author Elias Kuiter
 */
public interface File extends AutoCloseable {
	@Override
	void close() throws IOException;
}
