package de.featjar.base.computation;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Async returns into the Computation monad, await extracts from it.
 * Can be used to switch easily between (a)synchronous computation modes.
 */
public class Computations {
    public static <T> Computable<T> async(T t) {
        return Computable.of(t);
    }

    public static <T> Computable<T> async(Result<T> tResult) {
        return tResult.map(Computable::of).orElseThrow(); // todo: better error handling?
    }

    public static <T> Computable<T> async(Computable<T> tComputable) {
        return tComputable;
    }

    public static <T, U> Computable<Pair<T, U>> async(T t, U u) {
        return Computable.of(async(t), async(u));
    }

    public static <T, U> Computable<Pair<T, U>> async(Computable<T> tComputable, Computable<U> uComputable) {
        return Computable.of(tComputable, uComputable);
    }

    public static <T, U> Computable<List<?>> async(Object... dependencies) {
        return async(Arrays.stream(dependencies).map(Computations::async).toArray(Computable[]::new));
    }

    public static Computable<List<?>> async(Computable<?>... dependencies) {
        return Computable.allOf(dependencies);
    }

    public static <T, U> Function<Computable<T>, Computable<U>> map(Class<?> klass, String scope, Function<T, U> fn) {
        return tComputation -> tComputation.mapResult(klass, scope, fn);
    }

    public static <T, U> Function<Computable<T>, Computable<U>> flatMap(Class<?> klass, String scope, Function<T, Result<U>> fn) {
        return tComputation -> tComputation.flatMapResult(klass, scope, fn);
    }

    public static <T> T await(T t) {
        return t;
    }

    public static <T> T await(Result<T> tResult) {
        return tResult.orElseThrow(); // todo: better error handling?
    }

    public static <T> T await(Computable<T> tComputable) {
        return await(tComputable.getResult());
    }

    public static <T, U> Function<T, U> await(Function<Computable<T>, Computable<U>> fn) {
        return t -> await(fn.apply(Computable.of(t)));
    }

    public static <T, U> T getKey(Pair<T, U> tuPair) {
        return tuPair.getKey();
    }

    public static <T, U> Computable<T> getKey(Computable<Pair<T, U>> tuPair) {
        return tuPair.mapResult(Computations.class, "getKey", Pair::getKey);
    }

    public static <T, U> U getValue(Pair<T, U> tuPair) {
        return tuPair.getValue();
    }

    public static <T, U> Computable<U> getValue(Computable<Pair<T, U>> tuPair) {
        return tuPair.mapResult(Computations.class, "getValue", Pair::getValue);
    }
}
