package de.featjar.base.data;

import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.Monitor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;

// ComputationFuture? // computable future?
public class FutureResult<T> extends CompletableFuture<Result<T>> {
    protected Monitor monitor; // todo: how does this interact with the monitor's cancel, done ...? only report progress? does future's cancel work sensibly?

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
        return of(null, monitor);
    }

    public static <T> FutureResult<T> wrap(CompletableFuture<T> completableFuture) {
        return empty(new CancelableMonitor()).thenComputeResult(((o, monitor1) -> {
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

    public <U> FutureResult<U> thenComputeFromResult(BiFunction<Result<T>, Monitor, Result<U>> fn) {
        return (FutureResult) super.thenApply(tResult -> fn.apply(tResult, monitor));
    }

    protected static <T, U> BiFunction<Result<T>, Monitor, Result<U>> liftArgument(BiFunction<T, Monitor, Result<U>> fn) {
        return (tResult, monitor) -> tResult.isPresent() ? fn.apply(tResult.get(), monitor) : Result.empty(tResult);
    }

    protected static <T, U> BiFunction<Result<T>, Monitor, Result<U>> liftArgumentAndReturnValue(BiFunction<T, Monitor, U> fn) {
        return (tResult, monitor) -> tResult.isPresent() ? Result.of(fn.apply(tResult.get(), monitor)) : Result.empty(tResult);
    }

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

    // todo: override static allOf, anyOf ...

    //        @Override
//        public boolean complete(T i) {
//            store.put(key);
//            return super.complete(i);
//        }
//
//        @Override
//        public T get() throws InterruptedException, ExecutionException {
//            FeatJAR.store().
//            return super.get();
//        }

    //complete(result, [key])
    // key = getclass()+inputcomputation(s)+parameters

    //merge monitor and future?

    //monitor
    //result

}
