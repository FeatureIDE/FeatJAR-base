package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.IMonitor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A result that will become available in the future.
 * A {@link FutureResult} combines a {@link java.util.concurrent.Future} (which allows for asynchronous
 * calculations) with a {@link Result} (which tracks errors).
 * It wraps a {@link Result} that is not available yet, but will become available in the future.
 * It can be converted into a {@link Result} by calling {@link #get()}, which blocks until the result is available.
 * It can be chained with other calculations by calling {@link #thenCompute(BiFunction)} (and similar methods).
 * Once the result is available, it is cached indefinitely and can be retrieved with {@link #get()}.
 *
 * @param <T> the type of the result
 * @author Elias Kuiter
 */
public class FutureResult<T> extends CompletableFuture<Result<T>> {
    protected IMonitor monitor;

    protected FutureResult(IMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * {@return a completed future result of the given result}
     *
     * @param result  the result
     * @param monitor the monitor
     * @param <T>     the type of the future result
     */
    public static <T> FutureResult<T> ofResult(Result<T> result, IMonitor monitor) {
        FutureResult<T> futureResult = new FutureResult<>(monitor);
        futureResult.complete(result);
        return futureResult;
    }

    /**
     * {@return a completed future result of the given object}
     *
     * @param object  the object
     * @param monitor the monitor
     * @param <T>     the type of the future result
     */
    public static <T> FutureResult<T> of(T object, IMonitor monitor) {
        return ofResult(Result.of(object), monitor);
    }

    /**
     * {@return a future result of the given completable future}
     * If the given completable future completes with {@code null}, returns a {@link de.featjar.base.data.Void} result.
     *
     * @param completableFuture the completable future
     * @param <T>               the type of the future result
     */
    public static <T> FutureResult<T> ofCompletableFuture(CompletableFuture<T> completableFuture) {
        return ofResult(Result.ofVoid(), new CancelableMonitor())
                .thenComputeFromResult(((o, _monitor) -> {
                    try {
                        return Result.ofNullable(completableFuture.get());
                    } catch (InterruptedException | ExecutionException e) {
                        return Result.empty(e);
                    }
                }));
    }

    public static FutureResult<List<?>> allOf(List<FutureResult<?>> futureResults) {
            return ofCompletableFuture(allOf(futureResults.toArray(CompletableFuture[]::new)))
                    .thenComputeFromResult((_void, monitor) -> Result.mergeAll(futureResults.stream()
                            .map(FutureResult::get)
                            .collect(Collectors.toList())));
    }

    /**
     * {@return a future result that composes this future result with the given function}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenCompute(BiFunction<T, IMonitor, U> fn) {
        return thenComputeFromResult(mapArgumentAndReturnValue(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that returns a result}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenComputeResult(BiFunction<T, IMonitor, Result<U>> fn) {
        return thenComputeFromResult(mapArgument(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that operates on results}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U> FutureResult<U> thenComputeFromResult(BiFunction<Result<T>, IMonitor, Result<U>> fn) {
        return (FutureResult) super.thenApply(tResult -> {
            try {
                return fn.apply(tResult, monitor);
            } catch (Exception e) {
                return Result.empty(e);
            }
        });
    }

    protected static <T, U> BiFunction<Result<T>, IMonitor, Result<U>> mapArgument(BiFunction<T, IMonitor, Result<U>> fn) {
        return (tResult, monitor) -> tResult.isPresent()
                ? tResult.merge(fn.apply(tResult.get(), monitor))
                : tResult.merge(Result.empty());
    }

    protected static <T, U> BiFunction<Result<T>, IMonitor, Result<U>> mapArgumentAndReturnValue(BiFunction<T, IMonitor, U> fn) {
        return (tResult, monitor) -> tResult.isPresent()
                ? tResult.merge(Result.ofNullable(fn.apply(tResult.get(), monitor)))
                : tResult.merge(Result.empty());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return (CompletableFuture) new FutureResult<>(monitor);
    }

    @Override
    public Result<T> get() {
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException e) {
            return Result.empty(e);
        }
    }
}
