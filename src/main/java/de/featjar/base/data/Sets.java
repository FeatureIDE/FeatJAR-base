package de.featjar.base.data;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Sets {
    public static <T> LinkedHashSet<T> empty() {
        return new LinkedHashSet<>();
    }

    @SafeVarargs
    public static <T> LinkedHashSet<T> of(T... objects) {
        return new LinkedHashSet<>(Set.of(objects));
    }

    public static <T>
    Collector<T, ?, LinkedHashSet<T>> toSet() {
        return Collectors.toCollection(LinkedHashSet::new);
    }
}
