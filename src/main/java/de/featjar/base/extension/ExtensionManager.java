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
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.extension;

import de.featjar.base.Feat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Searches, installs, and uninstalls extensions on the classpath.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ExtensionManager implements AutoCloseable {
    private static ExtensionManager instance;

    /**
     * {@return the current extension manager, initializing it if needed}
     */
    public static ExtensionManager getInstance() {
        return instance == null ? new ExtensionManager() : instance;
    }

    /**
     * Removes the current extension manager.
     * The next call to {@link #getInstance()} will create a new extension manager.
     */
    public static void resetInstance() {
        instance = null;
    }

    private final HashMap<String, List<String>> extensionMap;
    private final Set<ExtensionPoint<?>> extensionPoints;

    /**
     * Installs all extensions that can be found in the class path.
     * To this end, filters all files on the class path for extension definition files, and loads each of them.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ExtensionManager() {
        extensionMap = new HashMap<>();
        extensionPoints = new HashSet<>();
        getResources().stream() //
                .filter(ExtensionManager::filterByFileName) //
                .peek(message -> Feat.log().debug(message))
                .forEach(this::loadExtensionDefinitionFile);
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        for (final Entry<String, List<String>> entry : extensionMap.entrySet()) {
            final String extensionPointId = entry.getKey();
            try {
                final Class<ExtensionPoint<?>> extensionPointClass = (Class<ExtensionPoint<?>>) systemClassLoader.loadClass(extensionPointId);
                final Method instanceMethod = extensionPointClass.getDeclaredMethod("getInstanceAsExtensionPoint");
                final ExtensionPoint ep = (ExtensionPoint) instanceMethod.invoke(null);
                extensionPoints.add(ep);
                for (final String extensionId : entry.getValue()) {
                    try {
                        final Class<Extension> extensionClass = (Class<Extension>) systemClassLoader.loadClass(extensionId);
                        Feat.log().debug(extensionClass.toString());
                        Extension extension = extensionClass.getConstructor().newInstance();
                        ep.installExtension(extension);
                    } catch (final Exception e) {
                        Feat.log().error(e);
                    }
                }
            } catch (final Exception e) {
                Feat.log().error(e);
            }
        }
    }

    /**
     * Uninstalls all currently installed extensions.
     */
    @Override
    public void close() {
        extensionPoints.forEach(ExtensionPoint::uninstallExtensions);
        resetInstance();
    }

    /**
     * {@return whether the file with the given name is an extension definition file}
     *
     * @param pathName the file name
     */
    private static boolean filterByFileName(String pathName) {
        try {
            if (pathName != null) {
                return Paths.get(pathName).getFileName().toString().matches("extensions(-.*)?[.]xml");
            }
            return false;
        } catch (final Exception e) {
            Feat.log().error(e);
            return false;
        }
    }

    /**
     * Installs all extensions from a given extension definition file.
     *
     * @param file the extension definition file
     */
    private void loadExtensionDefinitionFile(String file) {
        try {
            final Enumeration<URL> systemResources =
                    ClassLoader.getSystemClassLoader().getResources(file);
            while (systemResources.hasMoreElements()) {
                try {
                    final DocumentBuilder documentBuilder =
                            DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    final Document document =
                            documentBuilder.parse(systemResources.nextElement().openStream());
                    document.getDocumentElement().normalize();

                    final NodeList points = document.getElementsByTagName("point");
                    for (int i = 0; i < points.getLength(); i++) {
                        final Node point = points.item(i);
                        if (point.getNodeType() == Node.ELEMENT_NODE) {
                            final Element pointElement = (Element) point;
                            final String extensionPointId = pointElement.getAttribute("id");
                            List<String> extensionPoint =
                                    extensionMap.computeIfAbsent(extensionPointId, k -> new ArrayList<>());
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
                    Feat.log().error(e);
                }
            }
        } catch (final Exception e) {
            Feat.log().error(e);
        }
    }

    /**
     * {@return all names of files on the classpath}
     */
    private static Set<String> getResources() {
        final HashSet<String> resources = new HashSet<>();
        final String classPathProperty = System.getProperty("java.class.path", ".");
        final String pathSeparatorProperty = System.getProperty("path.separator");
        for (final String element : classPathProperty.split(pathSeparatorProperty)) {
            final Path path = Paths.get(element);
            try {
                if (Files.isRegularFile(path)) {
                    try (ZipFile zf = new ZipFile(path.toFile())) {
                        zf.stream().map(ZipEntry::getName).forEach(resources::add);
                    }
                } else if (Files.isDirectory(path)) {
                    try (Stream<Path> pathStream = Files.walk(path)) {
                        pathStream.map(path::relativize).map(Path::toString).forEach(resources::add);
                    }
                }
            } catch (final IOException e) {
                Feat.log().error(e);
            }
        }
        return resources;
    }
}
