/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.base.shell;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The session in which all loaded formats are stored.
 *
 * @author Niclas Kleinert
 */
public class ShellSession {

    private static class StoredElement<T> {
        private Class<T> type;
        private T element;

        public StoredElement(Class<T> type, T element) {
            this.type = type;
            this.element = element;
        }
    }

    private final Map<String, StoredElement<?>> elements;

    public ShellSession() {
        elements = new LinkedHashMap<>();
    }

    /**
     * Returns the element if the key is present in the session
     * and casts the element to the known type.
     *
     * @param <T> generic type of element
     * @param key the elements' key
     * @param kownType the elements' type
     * @return the element of the shell session or an empty result if the element is not present
     */
    @SuppressWarnings("unchecked")
    public <T> Result<T> get(String key, Class<T> kownType) {
        StoredElement<?> storedElement = elements.get(key);

        if (storedElement == null) {
            return Result.empty(addNotPresentProblem(key));
        }
        if (storedElement.type == kownType) {
            return Result.of((T) storedElement.type.cast(storedElement.element));
        } else {
            throw new RuntimeException("Wrong Type");
        }
    }

    /**
     * Returns the element if the key is present in the session or
     * ,otherwise, an empty result containing an error message.
     *
     * @param key the elements' key
     * @return the element that is mapped to the key or an empty result if it is not present
     */
    public Result<Object> get(String key) {
        return elements.get(key) != null
                ? Result.of(elements.get(key)).map(e -> e.element)
                : Result.empty(addNotPresentProblem(key));
    }

    /**
     * Returns the type of an element or
     * ,otherwise, an empty result containing an error message.
     *
     * @param key the elements' key
     * @return the type of the element or an empty result if the element is not present
     */
    public <T> Result<Object> getType(String key) {
        return elements.get(key) != null
                ? Result.of(elements.get(key)).map(e -> e.type)
                : Result.empty(addNotPresentProblem(key));
    }

    /**
     * Removes a single element of the shell session.
     *
     * @param key the elements' key
     * @return non-null previous value if the removal was successful
     */
    public Result<?> remove(String key) {
        return Result.of(elements.remove(key)).or(Result.empty(addNotPresentProblem(key)));
    }

    private Problem addNotPresentProblem(String key) {
        return new Problem(String.format("A variable named '%s' is not present in the session!", key), Severity.ERROR);
    }

    /**
     * Puts an element into the session.
     *
     * @param <T> generic type of element
     * @param key the elements' key
     * @param element the element of the shell session
     * @param type the elements' type
     */
    public <T> void put(String key, T element, Class<T> type) {
        elements.put(key, new StoredElement<T>(type, element));
    }

    /**
     * Removes all elements of the session.
     */
    public void clear() {
        elements.clear();
    }

    /**
     * {@return the number of elements in the session}
     */
    public int getSize() {
        return elements.size();
    }

    /**
     * Checks if the shell session contains a element with a specific key.
     *
     * @param key the elements' key
     * @return true if a variable with given key is present
     */
    public boolean containsKey(String key) {
        return elements.containsKey(key);
    }

    /**
     * Checks if the shell session is empty.
     *
     * @return true if no element is present
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Prints a single if there is a matching key.
     *
     * @param key the elements' key
     */
    public void printVariable(String key) {
        for (Entry<String, StoredElement<?>> entry : elements.entrySet()) {
            if (entry.getKey().equals(key)) {
                FeatJAR.log()
                        .message("Variable: " + key + " Type: "
                                + entry.getValue().type.getSimpleName() + "\n");
                break;
            }
        }
    }

    /**
     * Prints everything present in the session.
     */
    public void printVariables() {
        elements.entrySet().forEach(m -> FeatJAR.log()
                .message(m.getKey() + "   (" + m.getValue().type.getSimpleName() + ")"));
    }

    @SuppressWarnings({"unused"})
    public void printSortedByVarNames() {
        elements.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(m -> FeatJAR.log()
                .message(m.getKey() + " " + m.getValue().type.getSimpleName()));
    }

    @SuppressWarnings({"unused"})
    public void printSortedByType() {
        elements.entrySet().stream()
                .sorted(Comparator.comparing(e -> String.valueOf(e.getValue().type)))
                .forEach(m -> FeatJAR.log()
                        .message(m.getKey() + " " + m.getValue().type.getSimpleName()));
    }
}
