/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.extension;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;
import java.util.zip.*;

import javax.xml.parsers.*;

import org.spldev.util.logging.*;
import org.w3c.dom.*;

public class ExtensionLoader {

	private static HashMap<String, List<String>> extensionMap;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void load() {
		if (extensionMap == null) {
			extensionMap = new HashMap<>();
			getResources().stream()
				.filter(Pattern.compile(".*extensions[.]xml").asPredicate())
				.peek(System.out::println)
				.forEach(ExtensionLoader::load);
			for (final Entry<String, List<String>> entry : extensionMap.entrySet()) {
				final String extensionPointId = entry.getKey();
				try {
					final Class<?> extensionPointClass = ClassLoader.getSystemClassLoader().loadClass(extensionPointId);
					final Method instanceMethod = extensionPointClass.getDeclaredMethod("getInstance");
					final ExtensionPoint ep = (ExtensionPoint) instanceMethod.invoke(null);
					for (final String extensionId : entry.getValue()) {
						final Class<?> extensionClass = ClassLoader.getSystemClassLoader().loadClass(extensionId);
						final Extension ex = (Extension) extensionClass.getConstructor().newInstance();
						ep.addExtension(ex);
					}
				} catch (final Exception e) {
					Logger.logError(e);
				}
			}
		}
	}

	private static void load(String file) {
		try {
			final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document document = documentBuilder.parse(ClassLoader.getSystemResourceAsStream(file));
			document.getDocumentElement().normalize();

			final NodeList points = document.getElementsByTagName("point");
			for (int i = 0; i < points.getLength(); i++) {
				final Node point = points.item(i);
				if (point.getNodeType() == Node.ELEMENT_NODE) {
					final Element pointElement = (Element) point;
					final String extensionPointId = pointElement.getAttribute("id");
					List<String> extensionPoint = extensionMap.get(extensionPointId);
					if (extensionPoint == null) {
						extensionPoint = new ArrayList<>();
						extensionMap.put(extensionPointId, extensionPoint);
					}
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

	public static List<String> getResources() {
		final ArrayList<String> resources = new ArrayList<>();
		final String classPathProperty = System.getProperty("java.class.path", ".");
		final String pathSeparatorProperty = System.getProperty("path.separator");
		for (final String element : classPathProperty.split(pathSeparatorProperty)) {
			final Path path = Paths.get(element);
			try {
				if (Files.isRegularFile(path)) {
					try (ZipFile zf = new ZipFile(path.toFile())) {
						zf.stream()
							.map(ZipEntry::getName)
							.forEach(resources::add);
					}
				} else if (Files.isDirectory(path)) {
					Files.walk(path)
						.map(path::relativize)
						.map(Path::toString)
						.forEach(resources::add);
				}
			} catch (final IOException e) {
				Logger.logError(e);
			}
		}
		return resources;
	}

}
