/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.base.data;

import java.util.*;

/**
 * Maps a collection of at most n objects to the range of natural numbers [1..n].
 * Typically maps n objects one-to-one onto the range [1..n], but can contain definition gaps if needed.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class RangeMap<T> {
    protected final ArrayList<T> indexToObject = new ArrayList<>();
    protected final LinkedHashMap<T, Integer> objectToIndex = new LinkedHashMap<>();

    /**
     * Creates an empty range map.
     */
    public RangeMap() {
        clear();
    }

    /**
     * Merges two range maps into this range map.
     * Joins on common objects and does not necessarily preserve indices.
     * If one map is empty, creates a clone of the other.
     */
    public RangeMap(RangeMap<T> map1, RangeMap<T> map2) {
        SortedSet<T> objects = new TreeSet<>(map1.getObjects());
        objects.addAll(map2.getObjects());
        clear();
        indexToObject.addAll(objects);
        updateObjectToIndex();
    }

    /**
     * {@return the range of valid indices in this range map}
     */
    public Range getValidIndexRange() {
        return Range.of(1, indexToObject.size() - 1);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected int getMinimumIndex() {
        return getValidIndexRange().getLowerBound().get();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected int getMaximumIndex() {
        return getValidIndexRange().getUpperBound().get();
    }

    private boolean isValidIndex(int index) {
        return getValidIndexRange().test(index);
    }

    /**
     * {@return whether the given index is mapped by this range map}
     *
     * @param index the index
     */
    public boolean has(int index) {
        return isValidIndex(index) && indexToObject.get(index) != null;
    }

    /**
     * {@return whether the given object is mapped by this range map}
     *
     * @param object the object
     */
    public boolean has(T object) {
        Objects.requireNonNull(object);
        return objectToIndex.containsKey(object);
    }

    /**
     * Sets a new object for an index mapped by this range map.
     *
     * @param index the index
     * @param newObject the new object
     */
    public void setNewObject(int index, T newObject) {
        Objects.requireNonNull(newObject);
        if (isValidIndex(index)) {
            T object = indexToObject.get(index);
            if (object != null) {
                indexToObject.set(index, newObject);
                objectToIndex.remove(object);
                objectToIndex.put(newObject, index);
            } else {
                throw new NoSuchElementException(String.valueOf(index));
            }
        } else {
            throw new NoSuchElementException(String.valueOf(index));
        }
    }

    /**
     * Sets a new object for an object mapped by this range map.
     *
     * @param oldObject the old object
     * @param newObject the new object
     */
    public void setNewObject(T oldObject, T newObject) {
        Objects.requireNonNull(oldObject);
        Objects.requireNonNull(newObject);
        Integer index = objectToIndex.get(oldObject);
        if (index != null) {
            indexToObject.set(index, newObject);
            objectToIndex.remove(oldObject);
            objectToIndex.put(newObject, index);
        } else {
            throw new NoSuchElementException(String.valueOf(oldObject));
        }
    }

    /**
     * Maps an index to an object.
     *
     * @param index the index
     * @param object the object
     * @throws IllegalArgumentException if the index or object are invalid or already mapped
     */
    public void add(int index, T object) {
        if (index < -1 || index == 0) {
            throw new IllegalArgumentException("index is invalid");
        } else if (index == -1) {
            index = getMaximumIndex() + 1;
        } else if (isValidIndex(index) && indexToObject.get(index) != null) {
            throw new IllegalArgumentException("element with the index " + index + " already mapped");
        }
        if (objectToIndex.containsKey(object)) {
            throw new IllegalArgumentException("element with the object " + object + " already mapped");
        }
        for (int i = getMaximumIndex(); i < index; i++) {
            indexToObject.add(null);
        }
        objectToIndex.put(object, index);
        indexToObject.set(index, object);
    }

    /**
     * Removes an object mapped by this range map.
     *
     * @param object the object
     * @return whether the object was removed from this range map
     */
    public boolean remove(T object) {
        Integer index = objectToIndex.get(object);
        if (index != null) {
            if (index == getMaximumIndex()) {
                indexToObject.remove(index.intValue());
            } else {
                indexToObject.set(index, null);
            }
            objectToIndex.remove(object);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes an index mapped by this range map.
     *
     * @param index the index
     * @return whether the index was removed from this range map
     */
    public boolean remove(int index) {
        if (isValidIndex(index)) {
            T object = indexToObject.get(index);
            if (object != null) {
                objectToIndex.remove(object);
            }
            if (index == getMaximumIndex()) {
                indexToObject.remove(index);
            } else {
                indexToObject.set(index, null);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@return whether this range map has gaps}
     * That is, whether not all indices in {@link #getValidIndexRange()} are mapped.
     */
    public boolean hasGaps() {
        return objectToIndex.size() != indexToObject.size();
    }

    /**
     * {@return whether this range map also maps all objects mapped by a given range map}
     *
     * @param rangeMap the range map
     */
    public boolean containsAllObjects(RangeMap<T> rangeMap) {
        return objectToIndex.keySet().containsAll(rangeMap.objectToIndex.keySet());
    }

    /**
     * {@return all objects mapped by this range map}
     */
    public List<T> getObjects() {
        return indexToObject.subList(
                getMinimumIndex(),
                getMaximumIndex() + 1);
    }

    /**
     * {@return the object an index is mapped to by this range map}
     *
     * @param index the index
     */
    public Optional<T> get(int index) {
        return isValidIndex(index) ? Optional.ofNullable(indexToObject.get(index)) : Optional.empty();
    }

    /**
     * {@return the index an object is mapped to by this range map}
     *
     * @param object the object
     */
    public Optional<Integer> get(T object) {
        return Optional.ofNullable(objectToIndex.get(object));
    }

    /**
     * Clears this range map.
     */
    public void clear() {
        objectToIndex.clear();
        indexToObject.clear();
        indexToObject.add(null);
    }

    private void updateObjectToIndex() {
        for (int i = getMinimumIndex(); i <= getMaximumIndex(); i++) {
            T object = indexToObject.get(i);
            if (object != null) {
                objectToIndex.put(object, i);
            }
        }
    }

    /**
     * Randomizes the indices of this range map.
     *
     * @param random the random number generator
     */
    public void randomize(Random random) {
        Collections.shuffle(indexToObject.subList(getMinimumIndex(), getMaximumIndex()), random);
        updateObjectToIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(indexToObject);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        return Objects.equals(indexToObject, ((RangeMap) obj).indexToObject);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Map\n");
        for (int i = 1; i < indexToObject.size(); i++) {
            sb.append('\t');
            sb.append(indexToObject.get(i));
            sb.append('\n');
        }
        return sb.toString();
    }
}
