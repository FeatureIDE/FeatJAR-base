package de.featjar.base.data;

import java.util.function.Function;

/**
 * Async returns into the Computation monad, await extracts from it.
 * Can be used to switch easily between (a)synchronous computation modes.
 */
public class Computations {
    public static <T> Computation<T> async(T t) {
        return Computation.of(t);
    }

    public static <T, U> Computation<Pair<T, U>> async(T t, U u) {
        return Computation.of(async(t), async(u));
    }

    public static <T> Computation<T> async(Result<T> tResult) {
        return tResult.map(Computation::of).orElseThrow(); // todo: better error handling?
    }

    public static <T> Computation<T> async(Computation<T> tComputation) {
        return tComputation;
    }

    public static <T, U> Computation<Pair<T, U>> async(Computation<T> tComputation, Computation<U> uComputation) {
        return Computation.of(tComputation, uComputation);
    }

    public static <T, U> Function<Computation<T>, Computation<U>> async(Function<T, U> fn) {
        return tComputation -> tComputation.mapResult(fn);
    }

    public static <T> T await(T t) {
        return t;
    }

    public static <T> T await(Result<T> tResult) {
        return tResult.orElseThrow(); // todo: better error handling?
    }

    public static <T> T await(Computation<T> tComputation) {
        return await(tComputation.getResult());
    }

    public static <T, U> Function<T, U> await(Function<Computation<T>, Computation<U>> fn) {
        return t -> await(fn.apply(Computation.of(t)));
    }

    public static <T, U> T getKey(Pair<T, U> tuPair) {
        return tuPair.getKey();
    }

    public static <T, U> Computation<T> getKey(Computation<Pair<T, U>> tuPair) {
        return () -> tuPair.compute().thenCompute((pair, monitor) -> pair.getKey());
    }

    public static <T, U> U getValue(Pair<T, U> tuPair) {
        return tuPair.getValue();
    }

    public static <T, U> Computation<U> getValue(Computation<Pair<T, U>> tuPair) {
        return () -> tuPair.compute().thenCompute((pair, monitor) -> pair.getValue());
    }
}
