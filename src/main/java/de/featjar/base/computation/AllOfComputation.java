package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.task.IMonitor;
import de.featjar.base.tree.structure.ITree;

import java.util.List;
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
    public Result<List<?>> computeResult(List<?> results, IMonitor monitor) {
        return Result.of(results);
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new AllOfComputation();
    }
}
