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
package de.featjar.util.extension;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;
import java.util.zip.*;

import javax.xml.parsers.*;

import de.featjar.util.logging.Logger;
import de.featjar.util.logging.*;
import org.w3c.dom.*;

/**
 * Initializes, loads und unloads extensions.
 *
 * @author Sebastian Krieter
 */
public class ExtensionLoader {
	/**
	 * Maps identifiers of extension points to identifiers of loaded extensions.
	 */
	private static HashMap<String, List<String>> extensionMap;

	/**
	 * Unloads all currently loaded extensions.
	 */
	public static synchronized void unload() {
		if (extensionMap != null) {
			extensionMap.clear();
			extensionMap = null;
		}
	}

	/**
	 * Loads all extensions that can be found in the class path. Requires
	 * getInstance to be declared on the used extension points.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static synchronized void load() {
		if (extensionMap == null) {
			extensionMap = new HashMap<>();
			getResources().stream() //
				.filter(ExtensionLoader::filterByFileName) //
				.peek(Logger::logDebug)
				.forEach(ExtensionLoader::load);
			final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
			for (final Entry<String, List<String>> entry : extensionMap.entrySet()) {
				final String extensionPointId = entry.getKey();
				try {
					final Class<?> extensionPointClass = systemClassLoader.loadClass(extensionPointId);
					final Method instanceMethod = extensionPointClass.getDeclaredMethod("getInstance");
					final ExtensionPoint ep = (ExtensionPoint) instanceMethod.invoke(null);
					for (final String extensionId : entry.getValue()) {
						try {
							final Class<?> extensionClass = systemClassLoader.loadClass(extensionId);
							Logger.logDebug(extensionClass.toString());
							ep.addExtension((Extension) extensionClass.getConstructor().newInstance());
						} catch (final Exception e) {
							Logger.logError(e);
						}
					}
				} catch (final Exception e) {
					Logger.logError(e);
				}
			}
		}
	}

	/**
	 * Determines whether the file with the given names is an extension definition
	 * file.
	 */
	private static boolean filterByFileName(String pathName) {
		try {
			if (pathName != null) {
				return Paths.get(pathName).getFileName().toString().matches("extensions(-.*)?[.]xml");
			}
			return false;
		} catch (final Exception e) {
			Logger.logError(e);
			return false;
		}
	}

	/**
	 * Loads all extensions from a given extension definition file.
	 */
	private static void load(String file) {
		try {
			final Enumeration<URL> systemResources = ClassLoader.getSystemClassLoader().getResources(file);
			while (systemResources.hasMoreElements()) {
				try {
					final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					final Document document = documentBuilder.parse(systemResources.nextElement().openStream());
					document.getDocumentElement().normalize();

					final NodeList points = document.getElementsByTagName("point");
					for (int i = 0; i < points.getLength(); i++) {
						final Node point = points.item(i);
						if (point.getNodeType() == Node.ELEMENT_NODE) {
							final Element pointElement = (Element) point;
							final String extensionPointId = pointElement.getAttribute("id");
							List<String> extensionPoint = extensionMap
								.computeIfAbsent(extensionPointId, k -> new ArrayList<>());
							final NodeList extensions = pointElement.getChildNodes();
							for (int j = 0; j < extensions.getLength(); j++) {
								final Node extension = extensions.item(j);
								if (extension.getNodeType() == Node.ELEMENT_NODE) {
									final Element extensionElement = (Element) extension;
									final String extensionId = extensionElement.getAttribute("id");
									extensionPoint.add(extensionId);
								}
							}
						}
					}
				} catch (final Exception e) {
					Logger.logError(e);
				}
			}
		} catch (final Exception e) {
			Logger.logError(e);
		}
	}

	/**
	 * Returns all names of files in the class path.
	 */
	private static List<String> getResources() {
		final HashSet<String> resources = new HashSet<>();
		final String classPathProperty = System.getProperty("java.class.path", ".");
		final String pathSeparatorProperty = System.getProperty("path.separator");
		for (final String element : classPathProperty.split(pathSeparatorProperty)) {
			final Path path = Paths.get(element);
			Logger.logDebug(path);
			try {
				if (Files.isRegularFile(path)) {
					try (ZipFile zf = new ZipFile(path.toFile())) {
						zf.stream().map(ZipEntry::getName).forEach(resources::add);
					}
				} else if (Files.isDirectory(path)) {
					Files.walk(path).map(path::relativize).map(Path::toString).forEach(resources::add);
				}
			} catch (final IOException e) {
				Logger.logError(e);
			}
		}
		return new ArrayList<>(resources);
	}

}
