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
import de.featjar.base.data.Result;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An extension point installs {@link Extension extensions} of the same type.
 * As a naming convention, an extension named "Thing" belongs to an extension point named "Things".
 * Initialization is done by the {@link ExtensionManager} with a public no-arg constructor, which must be available.
 * De-initialization is done with {@link #close()}.
 *
 * @param <T> the type of the loaded extensions
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class ExtensionPoint<T extends Extension> {
    /**
     * Thrown when a requested extension cannot be found.
     */
    public static class NoSuchExtensionException extends Exception {
        public NoSuchExtensionException(String message) {
            super(message);
        }
    }

    private final HashMap<String, Integer> indexMap = new HashMap<>();
    private final List<T> extensions = new CopyOnWriteArrayList<>();

    /**
     * {@return a unique identifier for this extension point}
     */
    public String getIdentifier() {
        return getClass().getCanonicalName();
    }

    /**
     * Installs a new extension at this extension point.
     *
     * @param extension the extension
     * @return whether this extension is new and was installed correctly
     */
    public synchronized boolean installExtension(T extension) {
        if ((extension != null) && !indexMap.containsKey(extension.getIdentifier())) {
            indexMap.put(extension.getIdentifier(), extensions.size());
            extensions.add(extension);
            return true;
        }
        return false;
    }

    /**
     * Uninstalls an extension installed at this extension point.
     *
     * @param extension the extension
     * @return whether this extension was installed before
     */
    public synchronized boolean uninstallExtension(T extension) {
        Feat.log().debug("uninstalling extension " + extension.getClass());
        if ((extension != null) && indexMap.containsKey(extension.getIdentifier())) {
            indexMap.remove(extension.getIdentifier());
            extensions.remove(extension);
            extension.close();
            return true;
        }
        return false;
    }

    /**
     * Uninstalls all extensions installed at this extension point.
     */
    public synchronized void uninstallExtensions() {
        extensions.forEach(this::uninstallExtension);
    }

    /**
     * De-initializes this extension point, called by {@link ExtensionManager}.
     * Similar to {@link AutoCloseable#close()}, but called explicitly instead of implicitly in a try...with block.
     */
    public void close() {
        Feat.log().debug("uninstalling extension point " + getClass());
        uninstallExtensions();
    }

    /**
     * {@return all extensions installed at this extension point}
     * The list is in the same order as the extensions were installed with {@link #installExtension(Extension)}.
     */
    public synchronized List<T> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    /**
     * {@return the installed extension with a given identifier, if any}
     *
     * @param identifier the identifier
     */
    public Result<T> getExtension(String identifier) {
        Objects.requireNonNull(identifier, "identifier must not be null!");
        final Integer index = indexMap.get(identifier);
        return index != null
                ? Result.of(extensions.get(index))
                : Result.empty(new NoSuchExtensionException("No extension found for identifier " + identifier));
    }

    /**
     * {@return the installed extension point for a given class, if any}
     *
     * @param klass the class
     */
    public Result<T> getExtension(Class<? extends Extension> klass) {
        return getExtension(klass.getCanonicalName());
    }
}
