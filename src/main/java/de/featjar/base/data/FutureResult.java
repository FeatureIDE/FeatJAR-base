package de.featjar.base.data;

import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.Monitor;

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
 * TODO: javadoc is mostly missing
 *
 * @param <T> the type of the result
 * @author Elias Kuiter
 */
public class FutureResult<T> extends CompletableFuture<Result<T>> {
    protected Monitor monitor;

    protected FutureResult(Monitor monitor) {
        this.monitor = monitor;
    }

    public static <T> FutureResult<T> ofResult(Result<T> result, Monitor monitor) {
        FutureResult<T> futureResult = new FutureResult<>(monitor);
        futureResult.complete(result);
        return futureResult;
    }

    public static <T> FutureResult<T> of(T object, Monitor monitor) {
        return ofResult(Result.of(object), monitor);
    }

    public static FutureResult<Void> empty(Monitor monitor) {
        return of(null, monitor); // careful, is considered erroneous
    }

    public static <T> FutureResult<T> wrap(CompletableFuture<T> completableFuture) {
        return empty(new CancelableMonitor()).thenComputeFromResult(((o, monitor1) -> {
            try {
                return Result.of(completableFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                return Result.empty(e);
            }
        }));
    }

    public <U> FutureResult<U> thenCompute(BiFunction<T, Monitor, U> fn) {
        return thenComputeFromResult(liftArgumentAndReturnValue(fn));
    }

    public <U> FutureResult<U> thenComputeResult(BiFunction<T, Monitor, Result<U>> fn) {
        return thenComputeFromResult(liftArgument(fn));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U> FutureResult<U> thenComputeFromResult(BiFunction<Result<T>, Monitor, Result<U>> fn) {
        return (FutureResult) super.thenApply(tResult -> fn.apply(tResult, monitor));
    }

    protected static <T, U> BiFunction<Result<T>, Monitor, Result<U>> liftArgument(BiFunction<T, Monitor, Result<U>> fn) {
        return (tResult, monitor) -> tResult.isPresent() ? fn.apply(tResult.get(), monitor) : Result.empty(tResult);
    }

    protected static <T, U> BiFunction<Result<T>, Monitor, Result<U>> liftArgumentAndReturnValue(BiFunction<T, Monitor, U> fn) {
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
