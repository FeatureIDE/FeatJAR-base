package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.IMonitor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

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
     * @param result the result
     * @param monitor the monitor
     * @param <T> the type of the future result
     */
    public static <T> FutureResult<T> ofResult(Result<T> result, IMonitor monitor) {
        FutureResult<T> futureResult = new FutureResult<>(monitor);
        futureResult.complete(result);
        return futureResult;
    }

    /**
     * {@return a completed future result of the given object}
     *
     * @param object the object
     * @param monitor the monitor
     * @param <T> the type of the future result
     */
    public static <T> FutureResult<T> of(T object, IMonitor monitor) {
        return ofResult(Result.of(object), monitor);
    }

    /**
     * {@return an empty completed future result}
     *
     * @param monitor the monitor
     */
    public static FutureResult<Void> empty(IMonitor monitor) {
        return of(null, monitor); // careful, is considered erroneous
    }

    /**
     * {@return a future result of the given completable future}
     *
     * @param completableFuture the completable future
     * @param <T> the type of the future result
     */
    public static <T> FutureResult<T> ofCompletableFuture(CompletableFuture<T> completableFuture) {
        return empty(new CancelableMonitor()).thenComputeFromResult(((o, monitor1) -> {
            try {
                return Result.of(completableFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                return Result.empty(e);
            }
        }));
    }

    /**
     * {@return a future result that composes this future result with the given function}
     *
     * @param fn the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenCompute(BiFunction<T, IMonitor, U> fn) {
        return thenComputeFromResult(liftArgumentAndReturnValue(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that returns a result}
     *
     * @param fn the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenComputeResult(BiFunction<T, IMonitor, Result<U>> fn) {
        return thenComputeFromResult(liftArgument(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that operates on results}
     *
     * @param fn the function
     * @param <U> the type of the future result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U> FutureResult<U> thenComputeFromResult(BiFunction<Result<T>, IMonitor, Result<U>> fn) {
        return (FutureResult) super.thenApply(tResult -> fn.apply(tResult, monitor));
    }

    protected static <T, U> BiFunction<Result<T>, IMonitor, Result<U>> liftArgument(BiFunction<T, IMonitor, Result<U>> fn) {
        return (tResult, monitor) -> tResult.isPresent() ? fn.apply(tResult.get(), monitor) : Result.empty(tResult);
    }

    protected static <T, U> BiFunction<Result<T>, IMonitor, Result<U>> liftArgumentAndReturnValue(BiFunction<T, IMonitor, U> fn) {
        return (tResult, monitor) -> tResult.isPresent() ? Result.of(fn.apply(tResult.get(), monitor)) : Result.empty(tResult);
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
        } catch (InterruptedException e) {
            return Result.empty(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Result.empty(e);
        }
    }
}
