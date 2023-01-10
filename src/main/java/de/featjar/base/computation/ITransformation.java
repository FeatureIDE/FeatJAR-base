package de.featjar.base.computation;

/**
 * A computation that transforms its input into an object of the same type.
 * Can be used to compute a long-running {@link java.util.function.Function} to its own type (i.e., an automorphism).
 *
 * @param <T> the type of the input and output
 * @author Elias Kuiter
 */
public interface ITransformation<T> extends IAnalysis<T, T> {}
