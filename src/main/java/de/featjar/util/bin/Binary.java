/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
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
 */
package de.featjar.util.bin;

import de.featjar.util.extension.Extension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * A native binary bundled with the JAR.
 *
 * @author Elias Kuiter
 */
public abstract class Binary implements Extension {
	public static final Path BINARY_DIRECTORY = Paths.get(OperatingSystem.HOME_DIRECTORY, ".featjar-bin");

	public Binary() {
		extractResources();
	}

	public abstract Set<String> getResourceNames();

	public Path getPath() {
		return null;
	}

	public void extractResources() {
		BINARY_DIRECTORY.toFile().mkdirs();
		for (String resourceName : getResourceNames()) {
			try {
				Path outputPath = BINARY_DIRECTORY.resolve(resourceName);
				if (Files.notExists(outputPath)) {
					JAR.extractResource("bin/" + resourceName, outputPath);
					outputPath.toFile().setExecutable(true);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
