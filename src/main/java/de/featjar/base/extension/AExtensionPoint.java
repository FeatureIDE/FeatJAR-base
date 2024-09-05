/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.extension;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.log.IndentFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * An extension point installs {@link IExtension extensions} of a given type.
 * As a naming convention, an extension named "Thing" should be registered in an extension point named "Things".
 * Extension points can be registered in {@code resources/extensions.xml}.
 * Initialization is done by the {@link ExtensionManager} with a public no-arg constructor, which must be available.
 * De-initialization is done with {@link #close()}.
 *
 * @param <T> the type of the loaded extensions
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AExtensionPoint<T extends IExtension> {
    private final LinkedHashMap<String, Integer> indexMap = Maps.empty();
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
        FeatJAR.log().debug("uninstalling extension " + extension.getClass().getName());
        if (indexMap.containsKey(extension.getIdentifier())) {
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
        FeatJAR.log().debug("uninstalling extension point " + getClass().getName());
        uninstallExtensions();
    }

    /**
     * {@return all extensions installed at this extension point}
     * The list is in the same order as the extensions were installed with {@link #installExtension(IExtension)}.
     */
    public synchronized List<T> getExtensions() {
        return extensions;
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
                : Result.empty(new Problem("no extension found for identifier " + identifier, Problem.Severity.ERROR));
    }

    /**
     * {@return the installed extension matching the given part of its identifier, if any}
     * The matching is case-insensitive.
     * If no extensions match or the match is ambiguous, an empty result is returned.
     *
     * @param partOfIdentifier the part of the extension's identifier
     */
    public Result<T> getMatchingExtension(String partOfIdentifier) {
        final String identifierPart = partOfIdentifier.toLowerCase();
        LinkedHashSet<String> matchingIdentifiers = indexMap.keySet().stream()
                .filter(identifier -> identifier.toLowerCase().contains(identifierPart))
                .collect(Sets.toSet());
        if (matchingIdentifiers.isEmpty())
            return Result.empty(
                    new Problem("found no extensions matching " + partOfIdentifier, Problem.Severity.ERROR));
        if (matchingIdentifiers.size() > 1)
            return Result.empty(new Problem(
                    "found more than one extensions matching " + partOfIdentifier + ": \n"
                            + IndentFormatter.formatList(matchingIdentifiers),
                    Problem.Severity.ERROR));
        return getExtension(matchingIdentifiers.iterator().next());
    }

    /**
     * {@return all installed extensions matching the given regular expression}
     * The matching is case-insensitive.
     * If no extensions match, an empty set is returned.
     *
     * @param regex the regular expression
     */
    public LinkedHashSet<T> getMatchingExtensions(String regex) {
        LinkedHashSet<String> matchingIdentifiers = indexMap.keySet().stream()
                .filter(Pattern.compile(regex).asPredicate())
                .collect(Sets.toSet());
        return matchingIdentifiers.stream()
                .map(this::getExtension)
                .filter(Result::isPresent)
                .map(Result::get)
                .collect(Sets.toSet());
    }

    /**
     * {@return the installed extension point for a given class, if any}
     *
     * @param klass the class
     */
    public Result<T> getExtension(Class<? extends IExtension> klass) {
        return getExtension(klass.getCanonicalName());
    }
}
