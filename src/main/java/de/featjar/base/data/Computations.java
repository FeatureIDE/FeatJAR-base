package de.featjar.base.data;

public class Computations {
    public static <T> Result<T> awaitResult(Computation<T> computation) {
        return computation.getResult();
    }

    public static <T> T await(Computation<T> computation) {
        return awaitResult(computation).get();
    }

    public static <T> Computation<T> async(T t) {
        return Computation.of(t);
    }

    // async?
}
