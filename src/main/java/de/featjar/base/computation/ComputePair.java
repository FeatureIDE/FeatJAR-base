package de.featjar.base.computation;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;

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
        return getDependency((Dependency<T>) KEY_COMPUTATION);
    }

    @SuppressWarnings("unchecked")
    public void setKeyComputation(IComputation<T> key) {
        setDependency((Dependency<T>) KEY_COMPUTATION, key);
    }

    @SuppressWarnings("unchecked")
    public IComputation<U> getValueComputation() {
        return getDependency((Dependency<U>) VALUE_COMPUTATION);
    }

    @SuppressWarnings("unchecked")
    public void setValueComputation(IComputation<U> value) {
        setDependency((Dependency<U>) VALUE_COMPUTATION, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<Pair<T, U>> compute(DependencyList dependencyList, Progress progress) {
        return Result.of(
                new Pair<>((T) dependencyList.get(KEY_COMPUTATION), (U) dependencyList.get(VALUE_COMPUTATION)));
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputePair<>(getKeyComputation(), getValueComputation());
    }
}
