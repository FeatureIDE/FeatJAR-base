package de.featjar.base.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Maps {
    public static <T, U> LinkedHashMap<T, U> empty() {
        return new LinkedHashMap<>();
    }

    public static <T, U> LinkedHashMap<T, U> of(T key, U value) {
        return new LinkedHashMap<>(Map.of(key, value));
    }

    public static <T, K, U>
    Collector<T, ?, LinkedHashMap<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
                             Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (k, v) -> {
                    throw new IllegalStateException(java.lang.String.format("duplicate key %s", k));
                },
                LinkedHashMap::new);
    }
}
