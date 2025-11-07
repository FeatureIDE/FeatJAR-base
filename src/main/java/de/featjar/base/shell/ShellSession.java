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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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

    /**
     * similar functionality as {@link #getElement(String)}
     * but avoids unchecked cast warnings when the type of element is known
     * @param <T> generic type of element
     * @param key the elements' key
     * @param type the elements' type
     * @return the element of the shell session or an empty optional if the element is not present
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        StoredElement<?> storedElement = elements.get(key);

        if (storedElement == null) {
            return Optional.empty();
        }
        if (storedElement.type == type) {
            return Optional.of((T) storedElement.type.cast(storedElement.element));
        } else {
            throw new RuntimeException("Wrong Type");
        }
    }

    /**
     * @param key the elements' key
     * @return the type of the element or null if it is not present
     */
    public Optional<Object> getType(String key) {
        return Optional.ofNullable(elements.get(key)).map(e -> e.type);
    }

    /**
     * @param key the elements' key
     * @return the element that is mapped to the key or null if it is not present
     */
    public Optional<Object> getElement(String key) {
        return Optional.ofNullable(elements.get(key)).map(e -> e.element);
    }

    public ShellSession() {
        elements = new LinkedHashMap<>();
    }

    /**
     * puts an element into the session
     * @param <T> generic type of element
     * @param key the elements' key
     * @param element the element of the shell session
     * @param type the elements' type
     */
    public <T> void put(String key, T element, Class<T> type) {
        elements.put(key, new StoredElement<T>(type, element));
    }

    /**
     * removes a specific variable of the shell session
     * @param key the variables' key
     * @return non-null previous value if the removal was successful
     */
    public Optional<?> remove(String key) {
        return Optional.ofNullable(elements.remove(key));
    }
    /**
     * removes all elements of the session
     */
    public void clear() {
        elements.clear();
    }

    /**
     * @return the number of elements in the session
     */
    public int getSize() {
        return elements.size();
    }
    /**
     * checks if the shell session contains a variable with a specific key
     * @param key the elements' key
     * @return true if a variable with given key is present
     */
    public boolean containsKey(String key) {
        return elements.containsKey(key);
    }

    /**
     * checks if the shell session is empty
     * @return true if no element is present
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }
    /**
     * prints a single if there is a matching key
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
     * prints all in the session present variables
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
