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
package de.featjar.base.data;

import de.featjar.base.log.IndentFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Maps a collection of at most n objects to the range of natural numbers [1,
 * n]. Typically maps n objects one-to-one onto the range [1, n], but can
 * contain definition gaps if needed. TODO: currently, the edge case of an empty
 * range is not handled gracefully (with optionals everywhere). maybe this can
 * be solved in a better way.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class RangeMap<T> implements Cloneable {
    protected final ArrayList<T> indexToObject = new ArrayList<>();
    protected final LinkedHashMap<T, Integer> objectToIndex = Maps.empty();

    /**
     * Creates an empty range map.
     */
    public RangeMap() {
        indexToObject.add(null);
    }

    /**
     * Creates a range map for [1, n] from a collection with n elements.
     *
     * @param collection the collection
     */
    public RangeMap(Collection<T> collection) {
        indexToObject.add(null);
        indexToObject.addAll(collection);
        updateObjectToIndex();
    }

    /**
     * Copies a range map.
     *
     * @param rangeMap the map
     */
    protected RangeMap(RangeMap<T> rangeMap) {
        this(rangeMap.getObjects());
    }

    /**
     * Merges two range maps into this range map. Joins on common objects and does
     * not necessarily preserve indices. If one map is empty, creates a clone of the
     * other.
     *
     * @param rangeMap1 the first map
     * @param rangeMap2 the second map
     */
    public RangeMap(RangeMap<T> rangeMap1, RangeMap<T> rangeMap2) {
        SortedSet<T> objects = new TreeSet<>(rangeMap1.getObjects());
        objects.addAll(rangeMap2.getObjects());
        indexToObject.add(null);
        indexToObject.addAll(objects);
        updateObjectToIndex();
    }

    public int maxIndex() {
        return indexToObject.size() - 1;
    }

    /**
     * {@return the range of valid indices in this range map}
     */
    public Result<Range> getValidIndexRange() {
        return indexToObject.size() == 1 ? Result.empty() : Result.of(Range.of(1, indexToObject.size() - 1));
    }

    protected Result<Integer> getMinimumIndex() {
        return getValidIndexRange().map(Range::getLowerBound);
    }

    protected Result<Integer> getMaximumIndex() {
        return getValidIndexRange().map(Range::getUpperBound);
    }

    protected boolean isValidIndex(int index) {
        return getValidIndexRange().map(range -> range.test(index)).orElse(false);
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
     * @param index     the index
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
     * @param index  the index
     * @param object the object
     * @throws IllegalArgumentException if the index or object are invalid or
     *                                  already mapped
     */
    public void add(int index, T object) {
        if (index < -1 || index == 0) {
            throw new IllegalArgumentException("index is invalid");
        } else if (index == -1) {
            index = getMaximumIndex().map(i -> i + 1).orElse(1);
        } else if (isValidIndex(index) && indexToObject.get(index) != null) {
            throw new IllegalArgumentException("element with the index " + index + " already mapped");
        }
        if (objectToIndex.containsKey(object)) {
            throw new IllegalArgumentException("element with the object " + object + " already mapped");
        }
        for (int i = getMaximumIndex().orElse(0); i < index; i++) {
            indexToObject.add(null);
        }
        objectToIndex.put(object, index);
        indexToObject.set(index, object);
    }

    /**
     * Maps the next free index to an object.
     *
     * @param object the object
     */
    public void add(T object) {
        add(-1, object);
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
            if (index.equals(getMaximumIndex().get())) {
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
            if (index == getMaximumIndex().get()) {
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
     * {@return whether this range map has gaps} That is, whether not all indices in
     * {@link #getValidIndexRange()} are mapped.
     */
    public boolean hasGaps() {
        return objectToIndex.size() != indexToObject.size();
    }

    /**
     * {@return whether this range map also maps all objects mapped by a given range
     * map}
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
        return !isEmpty()
                ? indexToObject.subList(
                        getMinimumIndex().get(), getMaximumIndex().get() + 1)
                : new ArrayList<>();
    }

    /**
     * {@return the object an index is mapped to by this range map}
     *
     * @param index the index
     */
    public Result<T> get(int index) {
        return isValidIndex(index) ? Result.ofNullable(indexToObject.get(index)) : Result.empty();
    }

    /**
     * {@return the index an object is mapped to by this range map}
     *
     * @param object the object
     */
    public Result<Integer> get(T object) {
        return Result.ofNullable(objectToIndex.get(object));
    }

    /**
     * {@return all objects mapped by this range map}
     */
    public Stream<Pair<Integer, T>> stream() {
        return objectToIndex.entrySet().stream().map(Pair::of).map(Pair::flip);
    }

    /**
     * {@return a stream of objects that are mapped to the given indices}
     *
     * @param indices a list of indices
     */
    public Stream<T> stream(IntegerList indices) {
        return indices.stream().filter(this::isValidIndex).mapToObj(indexToObject::get);
    }

    /**
     * {@return a stream of indices that are mapped to the given objects}
     *
     * @param objects a list of objects
     */
    public Stream<Integer> stream(List<T> objects) {
        return objects.stream().map(objectToIndex::get);
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
        if (!isEmpty()) {
            int min = getMinimumIndex().get();
            int max = getMaximumIndex().get();
            objectToIndex.clear();
            for (int i = min; i <= max; i++) {
                T object = indexToObject.get(i);
                if (object != null) {
                    objectToIndex.put(object, i);
                }
            }
        }
    }

    /**
     * Randomizes the indices of this range map.
     *
     * @param random the random number generator
     */
    public void randomize(Random random) {
        if (!isEmpty()) {
            Collections.shuffle(
                    indexToObject.subList(
                            getMinimumIndex().get(), getMaximumIndex().get()),
                    random);
            updateObjectToIndex();
        }
    }

    /**
     * Normalizes the indices of this range map by removing any gaps.
     */
    public void normalize() {
        if (!isEmpty()) {
            int normalizedIndex = 1;
            for (int i = 1; i < indexToObject.size(); i++) {
                T o = indexToObject.get(i);
                if (o != null) {
                    indexToObject.set(normalizedIndex, o);
                    normalizedIndex++;
                }
            }
            updateObjectToIndex();
        }
    }

    /**
     * Adds objects of another map to this map, excluding duplicates.
     */
    public void addAll(RangeMap<T> other) {
        for (T variable : other.getObjects()) {
            if (!has(variable)) {
                add(variable);
            }
        }
    }

    public boolean isEmpty() {
        return getValidIndexRange().isEmpty();
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
        return IndentFormatter.formatList("RangeMap", indexToObject.subList(1, indexToObject.size()));
    }

    @Override
    public RangeMap<T> clone() {
        return new RangeMap<>(this);
    }
}
