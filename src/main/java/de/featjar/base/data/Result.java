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

import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

    private final List<Problem> problems = new LinkedList<>();

    protected Result(T object, List<Problem> problems) {
        this.object = object;
        problems = problems == null
                ? null
                : problems.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (problems != null && !problems.isEmpty()) {
            this.problems.addAll(problems);
        }
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
    }

    /**
     * {@return a result of a nullable object}
     *
     * @param object   the nullable object
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofNullable(T object) {
        return new Result<>(object, null);
    }

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
    public static <T> Result<T> ofOptional(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional) {
        return ofNullable(optional.orElse(null));
    }

    public static <T> Result<T> ofOptional(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional,
            Supplier<Problem> potentialProblem) {
        T value = optional.orElse(null);
        return value != null ? new Result<>(value, null) : empty(potentialProblem.get());
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

    public static Stream<Result<?>> nonNull(List<? extends Result<?>> results) {
        return results.stream().map(result -> result == null ? Result.empty() : result);
    }

    public static List<Result<?>> replaceNull(List<? extends Result<?>> results) {
        return nonNull(results).collect(Collectors.toList());
    }

    public static <T extends List<Object>> Result<T> mergeAll(
            List<? extends Result<?>> results, Supplier<T> listFactory) {
        List<Problem> problems = getProblems(results);
        return nonNull(results).noneMatch(Result::isEmpty)
                ? Result.of(results.stream().map(Result::get).collect(Collectors.toCollection(listFactory)), problems)
                : Result.empty(problems);
    }

    public static Result<ArrayList<Object>> mergeAll(List<? extends Result<?>> results) {
        return mergeAll(results, ArrayList::new);
    }

    public static <T extends List<Object>> Result<T> mergeAllNullable(
            List<? extends Result<?>> results, Supplier<T> listFactory) {
        List<Problem> problems = getProblems(results);
        return Result.of(
                nonNull(results).map(r -> r.orElse(null)).collect(Collectors.toCollection(listFactory)), problems);
    }

    public static Result<ArrayList<Object>> mergeAllNullable(List<? extends Result<?>> results) {
        return mergeAllNullable(results, ArrayList::new);
    }

    public static Result<?> mergeLast(List<Result<?>> results) {
        results = results.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (results.isEmpty()) return of(new ArrayList<>());
        List<Problem> problems = getProblems(results);
        return Result.ofNullable(results.get(results.size() - 1).orElse(null), problems);
    }

    @SuppressWarnings("unchecked")
    public <U> Result<U> merge(Result<U> other) {
        return (Result<U>) mergeLast(Arrays.asList(this, other));
    }

    @SuppressWarnings("unchecked")
    public <U> Result<U> unwrap() {
        Result<?> innerResult = this;
        Object innerObject = object;
        while (innerObject instanceof Result) {
            innerResult = (Result<?>) innerObject;
            innerObject = innerResult.orElse(null);
        }
        return (Result<U>) innerResult;
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
     * Consumes the problem list of this result, if the object is {@code null}.
     * This list is guaranteed to be non-null and read-only.
     *
     * @param problemHandler the problem list consumer
     */
    public void ifEmpty(Consumer<List<Problem>> problemHandler) {
        if (object != null) {
            problemHandler.accept(Collections.unmodifiableList(problems));
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
     * Filters the object in this result using a given {@link Predicate}
     * If the predicate evaluates to {@code false} the value of this result is set to {@code null}.
     *
     * @param predicate the predicate
     * @return A new result with the original object or an empty result.
     */
    public Result<T> filter(Predicate<T> predicate) {
        if (object != null) {
            try {
                if (!predicate.test(object)) {
                    return Result.empty(problems);
                }
            } catch (final Exception e) {
                return merge(Result.empty(e));
            }
        }
        return this;
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
     * {@return this result or an alternative result if this result is empty}
     * If this result is empty, the problems of this result are added to the alternative.
     *
     *
     * @param alternative the alternative result
     */
    public Result<T> or(Result<T> alternative) {
        Objects.requireNonNull(alternative);
        if (object != null) {
            return this;
        }
        alternative.problems.addAll(problems);
        return alternative;
    }

    /**
     * {@return this result or an alternative result if this result is empty}
     * If this result is empty, the problems of this result are added to the alternative.
     *
     *
     * @param alternativeSupplier the supplier for the alternative result
     */
    public Result<T> orGet(Supplier<? extends Result<T>> alternativeSupplier) {
        if (object != null) {
            return this;
        }
        Result<T> alternative = alternativeSupplier.get();
        alternative.problems.addAll(problems);
        return alternative;
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
        return orElseThrow(problems -> {
            problems = problems.stream()
                    .filter(problem -> problem.getSeverity().equals(Problem.Severity.ERROR))
                    .collect(Collectors.toList());
            if (problems.size() == 0) {
                return new RuntimeException("an unknown error occurred");
            }
            Problem problem = problems.get(0);
            if (problems.size() == 1) {
                return new RuntimeException(problem.getMessage(), problem.getException());
            } else {
                return new RuntimeException(
                        problem.getMessage() + " (and " + (problems.size() - 1) + " other problems)",
                        problem.getException());
            }
        });
    }

    /**
     * {@return this result's problems}
     * The returned list is guaranteed to be non-null and read-only.
     */
    public List<Problem> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    /**
     * {@return a supplier that prints all problems of this result to a string}
     */
    public String printProblems() {
        return Problem.printProblems(problems);
    }

    /**
     * {@return whether this result has problems}
     */
    public boolean hasProblems() {
        return !problems.isEmpty();
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
        return Objects.equals(object, ((Result<?>) o).object);
    }

    @Override
    public int hashCode() {
        // the problem is ignored as it cannot be hashed, and it only carries metadata for the user
        return Objects.hash(object);
    }

    @Override
    public String toString() {
        return "Result{" + object + ", " + problems + "}";
    }

    /**
     * {@return {@code true} if a value is present and it equals {@code otherValue}, {@code false} otherwise}
     * @param otherValue the value that is being compared to
     */
    public boolean valueEquals(T otherValue) {
        return isPresent() && Objects.equals(get(), otherValue);
    }

    public IComputation<T> toComputation() {
        return Computations.of(orElseThrow());
    }
}
