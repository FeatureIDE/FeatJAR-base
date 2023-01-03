package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.task.IMonitor;
import de.featjar.base.tree.structure.ITree;

import java.util.List;

/**
 * A computation that computes all its dependencies.
 * If any dependency fails to compute, the entire computation fails.
 *
 * @author Elias Kuiter
 */
public class ComputeAllOf extends AComputation<List<?>> {
    public ComputeAllOf(IComputation<?>... computations) {
        super(computations);
    }

    @Override
    public Result<List<?>> computeResult(List<?> results, IMonitor monitor) {
        return Result.of(results);
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputeAllOf();
    }
}
