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

import de.featjar.base.data.Problem.Severity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Wraps a nullable object, usually the return value of some function.
 * In contrast to {@link Optional}, can also store any {@link Problem} that occurred during execution of the function.
 *
 * @param <T> the type of the result's object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Result<T> {
    private final T object;

    private final List<Problem> problems;

    /**
     * {@return a result of a nullable object}
     *
     * @param object the nullable object
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> of(T object) {
        return new Result<>(object, null);
    }

    /**
     * {@return a result of a nullable object with problems}
     *
     * @param object the nullable object
     * @param problems the problems
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> of(T object, List<Problem> problems) {
        return new Result<>(object, problems);
    }

    /**
     * {@return a result of an {@link Optional}}
     *
     * @param optional the optional
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> ofOptional(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional) {
        //noinspection OptionalGetWithoutIsPresent
        return new Result<>(optional.get(), null);
    }

    /**
     * {@return an empty result with problems}
     *
     * @param problems the problems
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> empty(List<Problem> problems) {
        return new Result<>(null, problems);
    }

    /**
     * {@return an empty result with problems}
     *
     * @param problems the problems
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> empty(Problem... problems) {
        return new Result<>(null, Arrays.asList(problems));
    }

    /**
     * {@return an empty result with problems}
     *
     * @param exceptions the exceptions
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> empty(Exception... exceptions) {
        return new Result<>(null, Arrays.stream(exceptions).map(Problem::new).collect(Collectors.toList()));
    }

    /**
     * {@return an empty result}
     *
     * @param <T> the type of the result's object
     */
    public static <T> Result<T> empty() {
        return new Result<>(null, null);
    }

    private Result(T object, List<Problem> problems) {
        this.object = object;
        this.problems = (problems == null) || problems.isEmpty() ? Collections.emptyList() : new ArrayList<>(problems);
    }

    /**
     * {@return whether this result is empty}
     */
    public boolean isEmpty() {
        return object == null;
    }

    /**
     * {@return whether this result's object is present}
     */
    public boolean isPresent() {
        return object != null;
    }

    /**
     * {@return this result's object}
     */
    public T get() {
        return object;
    }

    /**
     * {@return an {@link Optional} of this result's object}
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(object);
    }

    /**
     * Maps the object in this result to another object using a mapper function.
     *
     * @param mapper the mapper function
     * @return A new result with the mapped object or an empty result, if any
     * exceptions occur during the mapping or if this result was empty before.
     * @param <R> the type of the mapped object
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        if (object != null) {
            try {
                return Result.of(mapper.apply(object), problems);
            } catch (final Exception e) {
                return Result.empty(e);
            }
        } else {
            return Result.empty(problems);
        }
    }

    /**
     * Maps the object in this result to another object using a mapper function that also returns a {@link Result}.
     *
     * @param mapper the mapper function
     * @return A new result with the mapped object or an empty result, if any
     * exceptions occur during the mapping or if this result was empty before.
     * @param <R> the type of the mapped object
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        return object != null ? mapper.apply(object) : Result.empty(problems);
    }

    /**
     * Peeks at this result's object.
     *
     * @param consumer the consumer function
     * @return this result
     */
    public Result<T> peek(Consumer<T> consumer) {
        if (object != null) {
            try {
                consumer.accept(object);
            } catch (final Exception ignored) {
            }
        }
        return this;
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
    public T orElse(Supplier<T> alternativeSupplier) {
        return object != null ? object : alternativeSupplier.get();
    }

    /**
     * {@return this result's object or peeks at this result's problems}
     *
     * @param errorHandler the error handler
     */
    public T orElse(Consumer<List<Problem>> errorHandler) {
        if (object != null) {
            return object;
        } else {
            errorHandler.accept(problems);
            return null;
        }
    }

    /**
     * {@return this result's object or an alternative object, peeking at this result's problems}
     *
     * @param alternative the alternative object
     * @param errorHandler the error handler
     */
    public T orElse(T alternative, Consumer<List<Problem>> errorHandler) {
        if (object != null) {
            return object;
        } else {
            errorHandler.accept(problems);
            return alternative;
        }
    }

    /**
     * {@return this result's object or an alternative object, peeking at this result's problems}
     *
     * @param alternativeSupplier the supplier function
     * @param errorHandler the error handler
     */
    public T orElse(Supplier<T> alternativeSupplier, Consumer<List<Problem>> errorHandler) {
        if (object != null) {
            return object;
        } else {
            errorHandler.accept(problems);
            return alternativeSupplier.get();
        }
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
            throw errorHandler.apply(problems);
        }
    }

    /**
     * {@return this result's object or throws this result's problems}
     */
    public T orElseThrow() throws RuntimeException {
        if (object != null) {
            return object;
        } else {
            throw problems.stream() //
                    .filter(p -> p.getSeverity() == Severity.ERROR) //
                    .findFirst() //
                    .map(this::getException) //
                    .orElseGet(RuntimeException::new);
        }
    }

    /**
     * {@return an exception for a problem}
     *
     * @param p the problem
     */
    private RuntimeException getException(Problem p) {
        return new RuntimeException(p.toException());
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
     * Consumes this result's object, if any, otherwise consumes its problems.
     *
     * @param resultHandler the object consumer
     * @param errorHandler  the problem consumer
     */
    public void ifPresentOrElse(Consumer<T> resultHandler, Consumer<List<Problem>> errorHandler) {
        if (object != null) {
            resultHandler.accept(object);
        } else {
            errorHandler.accept(problems);
        }
    }

    /**
     * {@return this result's problems}
     */
    public List<Problem> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    /**
     * {@return whether this result has any problems}
     */
    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    /**
     * {@return an {@link Optional} of a given index}
     *
     * @param index the index
     */
    public static Optional<Integer> indexToOptional(int index) {
        return index == -1 ? Optional.empty() : Optional.of(index);
    }

    /**
     * {@return a wrapped function that converts its results into {@link Optional}}
     *
     * @param function the function
     */
    public static <U, V> Function<U, Optional<V>> wrapInOptional(Function<U, V> function) {
        return t -> Optional.ofNullable(function.apply(t));
    }

    @Override
    public String toString() {
        return "Result{" + get() + "}";
    }
}