package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A computation that computes all its dependencies.
 * If any dependency fails to compute, the entire computation fails.
 */
public class AllOfComputation extends AComputation<List<?>> {
    public AllOfComputation(IComputation<?>... computations) {
        super(computations);
    }

    @Override
    public FutureResult<List<?>> compute() {
        List<FutureResult<?>> futureResults =
                getChildren().stream().map(IComputation::get).collect(Collectors.toList());
        return FutureResult.ofCompletableFuture(FutureResult.allOf(futureResults.toArray(CompletableFuture[]::new)))
                .thenComputeFromResult((unused, monitor) -> {
                    List<?> results = futureResults.stream()
                            .map(FutureResult::get)
                            .map(Result::get)
                            .collect(Collectors.toList());
                    return results.stream().noneMatch(Objects::isNull) ? Result.of(results) : Result.empty();
                });
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new AllOfComputation();
    }
}
