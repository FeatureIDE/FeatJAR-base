package de.featjar.base.computation;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A computation that computes two computations.
 * If any dependency fails to compute, the entire computation fails.
 *
 * @author Elias Kuiter
 */
public class PairComputation<T, U> extends AComputation<Pair<T, U>> {
    protected static Dependency<IComputation<?>> FIRST_COMPUTATION = newDependency();
    protected static Dependency<IComputation<?>> SECOND_COMPUTATION = newDependency();

    public PairComputation(IComputation<T> firstComputation, IComputation<U> secondComputation) {
        dependOn(FIRST_COMPUTATION, SECOND_COMPUTATION);
        setFirstComputation(firstComputation);
        setSecondComputation(secondComputation);
    }

    @SuppressWarnings("unchecked")
    public IComputation<T> getFirstComputation() {
        return ((Dependency<T>) FIRST_COMPUTATION).get(this);
    }

    @SuppressWarnings("unchecked")
    public void setFirstComputation(IComputation<T> firstComputation) {
        ((Dependency<T>) FIRST_COMPUTATION).set(this, firstComputation);
    }

    @SuppressWarnings("unchecked")
    public IComputation<U> getSecondComputation() {
        return ((Dependency<U>) SECOND_COMPUTATION).get(this);
    }

    @SuppressWarnings("unchecked")
    public void setSecondComputation(IComputation<U> secondComputation) {
        ((Dependency<U>) SECOND_COMPUTATION).set(this, secondComputation);
    }

    @Override
    public FutureResult<Pair<T, U>> compute() {
        FutureResult<T> firstFutureResult = getFirstComputation().get();
        FutureResult<U> secondFutureResult = getSecondComputation().get();
        return FutureResult.ofCompletableFuture(
                FutureResult.allOf(firstFutureResult, secondFutureResult))
                .thenComputeFromResult((unused, monitor) ->
                        firstFutureResult.get().isEmpty() || secondFutureResult.get().isEmpty()
                        ? Result.empty()
                        : Result.of(new Pair<>(firstFutureResult.get().get(), secondFutureResult.get().get())));
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new PairComputation<>(getFirstComputation(), getSecondComputation());
    }
}
