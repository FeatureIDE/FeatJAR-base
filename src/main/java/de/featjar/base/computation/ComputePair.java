package de.featjar.base.computation;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.task.IMonitor;
import de.featjar.base.tree.structure.ITree;

import java.util.List;

/**
 * A computation that computes two computations.
 * If any dependency fails to compute, the entire computation fails.
 *
 * @author Elias Kuiter
 */
public class ComputePair<T, U> extends AComputation<Pair<T, U>> {
    protected static Dependency<?> KEY_COMPUTATION = newRequiredDependency();
    protected static Dependency<?> VALUE_COMPUTATION = newRequiredDependency();

    public ComputePair(IComputation<T> key, IComputation<U> value) {
        dependOn(KEY_COMPUTATION, VALUE_COMPUTATION);
        setKeyComputation(key);
        setValueComputation(value);
    }

    @SuppressWarnings("unchecked")
    public IComputation<T> getKeyComputation() {
        return ((Dependency<T>) KEY_COMPUTATION).get(this);
    }

    @SuppressWarnings("unchecked")
    public void setKeyComputation(IComputation<T> key) {
        ((Dependency<T>) KEY_COMPUTATION).set(this, key);
    }

    @SuppressWarnings("unchecked")
    public IComputation<U> getValueComputation() {
        return ((Dependency<U>) VALUE_COMPUTATION).get(this);
    }

    @SuppressWarnings("unchecked")
    public void setValueComputation(IComputation<U> value) {
        ((Dependency<U>) VALUE_COMPUTATION).set(this, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<Pair<T, U>> computeResult(List<?> results, IMonitor monitor) {
        return Result.of(new Pair<>((T) KEY_COMPUTATION.get(results), (U) VALUE_COMPUTATION.get(results)));
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputePair<>(getKeyComputation(), getValueComputation());
    }
}
