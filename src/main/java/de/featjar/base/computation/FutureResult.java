package de.featjar.base.computation;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import net.tascalate.concurrent.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A result that will become available in the future.
 * A {@link FutureResult} combines a {@link java.util.concurrent.Future} (which allows for asynchronous
 * calculations) with a {@link Result} (which tracks errors).
 * It wraps a {@link Result} that is not available yet, but will become available in the future.
 * It can be converted into a {@link Result} by calling {@link #get()}, which blocks until the result is available.
 * It can be chained with other calculations by calling {@link #thenFromResult(BiFunction)}}.
 * Once the result is available, it is cached indefinitely and can be retrieved with {@link #get()}.
 *
 * @param <T> the type of the result
 * @author Elias Kuiter
 */
public class FutureResult<T> implements Supplier<Result<T>> {
    protected final static ExecutorService executor = ForkJoinPool.commonPool();

    protected final Promise<Result<T>> promise;
    protected final Progress progress = new Progress();

    /**
     * Creates a future result completed with a given result.
     *
     * @param result the result
     */
    public FutureResult(Result<T> result) {
        this(CompletableTask.completed(result, executor));
    }

    /**
     * Creates a future result from a given promise.
     *
     * @param promise the promise
     */
    public FutureResult(Promise<Result<T>> promise) {
        this.promise = promise;
    }

    /**
     * {@return a future result from given future results that resolves when all given future results are resolved}
     *
     * @param futureResults the future results
     * @param resultMerger the result merger
     */
    public static FutureResult<List<?>> allOf(List<FutureResult<?>> futureResults, Function<List<? extends Result<?>>, Result<List<?>>> resultMerger) {
        List<Promise<? extends Result<?>>> promises = futureResults.stream().map(FutureResult::getPromise).collect(Collectors.toList());
        Promise<Result<List<?>>> promise = Promises.all(promises).thenApplyAsync(list ->
                resultMerger.apply(futureResults.stream().map(FutureResult::get).collect(Collectors.toList())), executor);
        return new FutureResult<>(promise);
    }

    /**
     * {@return a future result from given future results that resolves when all given future results are resolved}
     * Resolves to a non-empty result only when all future results resolve to non-empty results.
     *
     * @param futureResults the future results
     */
    public static FutureResult<List<?>> allOf(List<FutureResult<?>> futureResults) {
        return allOf(futureResults, Result::mergeAll);
    }

    /**
     * {@return this future result's promise}
     */
    public Promise<Result<T>> getPromise() {
        return promise;
    }

    /**
     * {@return a future result that composes this future result with the given function}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> then(BiFunction<T, Progress, U> fn) {
        return thenFromResult(mapArgumentAndReturnValue(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that returns a result}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenResult(BiFunction<T, Progress, Result<U>> fn) {
        return thenFromResult(mapArgument(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that operates on results}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenFromResult(BiFunction<Result<T>, Progress, Result<U>> fn) {
        return new FutureResult<>(promise.thenApplyAsync(tResult -> {
            try {
                return tResult.merge(fn.apply(tResult, progress));
            } catch (Exception e) {
                return Result.empty(e);
            }
        }, executor));
    }

    protected static <T, U> BiFunction<Result<T>, Progress, Result<U>> mapArgument(BiFunction<T, Progress, Result<U>> fn) {
        return (tResult, progress) -> tResult.isPresent()
                ? fn.apply(tResult.get(), progress)
                : Result.empty();
    }

    protected static <T, U> BiFunction<Result<T>, Progress, Result<U>> mapArgumentAndReturnValue(BiFunction<T, Progress, U> fn) {
        return (tResult, progress) -> tResult.isPresent()
                ? Result.ofNullable(fn.apply(tResult.get(), progress))
                : Result.empty();
    }

    /**
     * {@return this future result's result}
     * Blocks synchronously until the result is available.
     */
    public Result<T> get() {
        try {
            return promise.get();
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            return Result.empty(e);
        }
    }

    /**
     * Cancels the execution of this future result's promise when a given duration has passed.
     * Discards any partially computed result.
     *
     * @param duration the duration
     */
    public void cancelAfter(Duration duration) {
        promise.orTimeout(duration); // todo: get partial result?
    }

    /**
     * Cancels the execution of this future result's promise.
     * Discards any partially computed result.
     */
    public void cancel() {
        promise.cancel(true);
    }

    /**
     * Runs a function when a given duration has passed and the promise is not resolved yet.
     *
     * @param duration the duration
     * @param fn the function
     */
    public void peekAfter(Duration duration, Runnable fn) {
        Result<T> result = Result.empty(new Problem("timeout occurred"));
        promise.onTimeout(result, duration, false).thenApplyAsync(r -> {
            if (result.equals(r)) {
                fn.run();
            }
            return null;
        });
    }

    /**
     * Runs a function regularly at a given interval until the promise is resolved.
     *
     * @param interval the interval
     * @param fn the function
     */
    public void peekEvery(Duration interval, Runnable fn) {
        peekAfter(interval, () -> {
            fn.run();
            peekAfter(interval, fn);
        });
    }
}
