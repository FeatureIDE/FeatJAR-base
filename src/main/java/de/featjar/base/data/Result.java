/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.data;

import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An optional object that may be present with or without a problem,
 * absent with intention, or absent due to some unintended problem.
 * Similar to Java's {@link Optional}, but also stores any {@link Problem} associated when trying to obtain an object.
 * Usually, a {@link Result} wraps the result of a {@link IComputation} or other potentially complex operation,
 * such as parsing a {@link IFormat}.
 * Instead of throwing exceptions, consider using a {@link Result} if there is some object to return.
 * For void methods, throwing checked exceptions may be more reasonable than returning a {@link Result} object.
 *
 * @param <T> the type of the result's object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Result<T> implements Supplier<T> {
    protected static final Result<?> EMPTY = new Result<>(null, null);

    private final T object;

    private final List<Problem> problems;

    protected Result(T object, List<Problem> problems) {
        this.object = object;
        problems = problems == null
                ? null
                : problems.stream().filter(Objects::nonNull).collect(Collectors.toList());
        this.problems = problems != null && !problems.isEmpty() ? problems : null;
    }

    /**
     * {@return a result of a non-null object}
     *
     * @param object   the non-null object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> of(T object, Problem... problems) {
        return of(object, Arrays.asList(problems));
    }

    /**
     * {@return a result of a non-null object}
     *
     * @param object   the non-null object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> of(T object, List<Problem> problems) {
        Objects.requireNonNull(object, "tried to create non-empty result with null");
        return new Result<>(object, problems);
    } // todo: immediately log warning/error here?

    /**
     * {@return a result of a nullable object}
     *
     * @param object   the nullable object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofNullable(T object, Problem... problems) {
        return ofNullable(object, Arrays.asList(problems));
    }

    /**
     * {@return a result of a nullable object}
     *
     * @param object   the nullable object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofNullable(T object, List<Problem> problems) {
        return new Result<>(object, problems);
    }

    /**
     * {@return a result of an {@link Optional}}
     *
     * @param optional the optional
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofOptional(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional) {
        return ofNullable(optional.orElse(null));
    }

    /**
     * {@return an empty result}
     *
     * @param exception the exception
     * @param <T>       the type of the result's object
     */
    public static <T> Result<T> empty(Exception exception) {
        return empty(new Problem(exception));
    }

    /**
     * {@return an empty result}
     *
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> empty(Problem... problems) {
        return empty(Arrays.asList(problems));
    }

    /**
     * {@return an empty result}
     *
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> empty(List<Problem> problems) {
        return new Result<>(null, problems);
    }

    /**
     * {@return a void result}
     *
     * @param exception the exception
     */
    public static Result<Void> ofVoid(Exception exception) {
        return of(Void.VOID, new Problem(exception));
    }

    /**
     * {@return a void result}
     *
     * @param problems the problems
     */
    public static Result<Void> ofVoid(Problem... problems) {
        return ofVoid(Arrays.asList(problems));
    }

    /**
     * {@return a void result}
     *
     * @param problems the problems
     */
    public static Result<Void> ofVoid(List<Problem> problems) {
        return of(Void.VOID, problems);
    }

    public static List<Problem> getProblems(Result<?>... results) {
        return getProblems(Arrays.asList(results));
    }

    public static List<Problem> getProblems(List<? extends Result<?>> results) {
        return results.stream()
                .filter(Objects::nonNull)
                .flatMap(r -> r.getProblems().stream())
                .collect(Collectors.toList());
    }

    protected static List<Result<?>> replaceNull(List<? extends Result<?>> results) {
        return results.stream().map(result -> result == null ? Result.empty() : result).collect(Collectors.toList());
    }

    public static <T extends List<Object>> Result<T> mergeAll(List<? extends Result<?>> results, Supplier<T> listFactory) {
        List<Problem> problems = getProblems(results);
        return replaceNull(results).stream().noneMatch(Result::isEmpty)
                ? Result.of(results.stream().map(Result::get).collect(Collectors.toCollection(listFactory)), problems)
                : Result.empty(problems);
    }

    public static Result<ArrayList<Object>> mergeAll(List<? extends Result<?>> results) {
        return mergeAll(results, ArrayList::new);
    }

    public static <T extends List<Object>> Result<T> mergeAllNullable(List<? extends Result<?>> results, Supplier<T> listFactory) {
        List<Problem> problems = getProblems(results);
        return Result.of(
                replaceNull(results).stream()
                        .map(r -> r.orElse(null))
                        .collect(Collectors.toCollection(listFactory)),
                problems);
    }

    public static Result<ArrayList<Object>> mergeAllNullable(List<? extends Result<?>> results) {
        return mergeAllNullable(results, ArrayList::new);
    }

    public static Result<?> mergeLast(List<Result<?>> results) {
        results = results.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (results.isEmpty())
            return of(new ArrayList<>());
        List<Problem> problems = getProblems(results);
        return Result.ofNullable(results.get(results.size() - 1).orElse(null), problems);
    }

    @SuppressWarnings("unchecked")
    public <U> Result<U> merge(Result<U> other) {
        return (Result<U>) mergeLast(Arrays.asList(this, other));
    }

    /**
     * {@return this result's object}
     * As a side effect, logs all problems that have occurred.
     *
     * @throws NoSuchElementException if no object is present
     */
    public T get() {
        if (object == null) {
            throw new NoSuchElementException("no object present");
        }
        return object;
    }

    /**
     * {@return whether this result's object is present}
     */
    public boolean isPresent() {
        return object != null;
    }

    /**
     * {@return whether this result is empty}
     */
    public boolean isEmpty() {
        return object == null;
    }

    /**
     * Consumes this result's object, if any.
     *
     * @param resultHandler the object consumer
     */
    public void ifPresent(Consumer<T> resultHandler) {
        if (object != null) {
            resultHandler.accept(object);
        }
    }

    /**
     * Maps the object in this result to another object using a mapper function.
     *
     * @param mapper the mapper function
     * @param <R>    the type of the mapped object
     * @return A new result with the mapped object or an empty result, if any
     * exceptions occur during the mapping or if this result was empty before.
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        if (object != null) {
            try {
                return Result.of(mapper.apply(object), problems);
            } catch (final Exception e) {
                return merge(Result.empty(e));
            }
        } else {
            return Result.empty(problems);
        }
    }

    /**
     * Maps the object in this result to another object using a mapper function that also returns a {@link Result}.
     *
     * @param mapper the mapper function
     * @param <R>    the type of the mapped object
     * @return A new result with the mapped object or an empty result, if any
     * exceptions occur during the mapping or if this result was empty before.
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return object != null ? mapper.apply(object) : Result.empty(problems);
    }

    /**
     * {@return a sequential {@link Stream} containing only this result's object, if any}
     */
    public Stream<T> stream() {
        if (!isPresent()) {
            return Stream.empty();
        } else {
            return Stream.of(object);
        }
    }

    /**
     * {@return this result's object or an alternative object}
     *
     * @param alternative the alternative object
     */
    public T orElse(T alternative) {
        return object != null ? object : alternative;
    }

    /**
     * {@return this result's object or an alternative object}
     *
     * @param alternativeSupplier the supplier function
     */
    public T orElseGet(Supplier<? extends T> alternativeSupplier) {
        return object != null ? object : alternativeSupplier.get();
    }

    /**
     * {@return this result's object or throws this result's problems}
     *
     * @param errorHandler the error handler
     */
    public <E extends Exception> T orElseThrow(Function<List<Problem>, E> errorHandler) throws E {
        if (object != null) {
            return object;
        } else {
            throw errorHandler.apply(getProblems());
        }
    }

    /**
     * {@return this result's object or throws this result's problems}
     */
    public T orElseThrow() {
        return orElseThrow(problems -> new RuntimeException(problems.stream().map(Problem::toString).collect(Collectors.joining(", "))));
    }

    /**
     * {@return this result's problems}
     */
    public List<Problem> getProblems() {
        return problems != null ? problems : List.of();
    }

    /**
     * {@return whether this result has problems}
     */
    public boolean hasProblems() {
        return problems != null;
    }

    /**
     * {@return an {@link Optional} of this result's object}
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(object);
    }

    /**
     * {@return an {@link Optional} of a given index}
     *
     * @param index the index
     */
    public static Result<Integer> ofIndex(int index) {
        return index == -1 ? empty() : of(index);
    }

    /**
     * {@return a wrapped function that converts its results into {@link Optional}}
     *
     * @param function the function
     */
    public static <U, V> Function<U, Result<V>> mapReturnValue(Function<U, V> function) {
        return t -> Result.of(function.apply(t));
    }

    @Override
    public boolean equals(Object o) {
        // the problem is ignored as it cannot be compared for equality, and it only carries metadata for the user
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result<?> result = (Result<?>) o;
        return Objects.equals(object, result.object);
    }

    @Override
    public int hashCode() {
        // the problem is ignored as it cannot be hashed, and it only carries metadata for the user
        return Objects.hash(object);
    }

    @Override
    public String toString() {
        return "Result{" + orElse(null) + ", " + problems + "}";
    }
}
