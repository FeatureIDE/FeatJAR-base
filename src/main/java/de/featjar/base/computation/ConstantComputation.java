package de.featjar.base.computation;

import de.featjar.base.task.IMonitor;
import de.featjar.base.tree.structure.ALeafNode;
import de.featjar.base.tree.structure.ITree;

import java.util.Objects;

/**
 * A constant computation.
 * Always computes the same value.
 * The leaves of a computation tree are precisely its constant computations.
 *
 * @param <T> the type of the computed value
 */
public class ConstantComputation<T> extends ALeafNode<IComputation<?>> implements IComputation<T> {
    protected final T value;
    protected final IMonitor monitor;

    /**
     * Creates a constant computation.
     *
     * @param value   the value
     * @param monitor the monitor
     */
    public ConstantComputation(T value, IMonitor monitor) {
        this.value = value;
        this.monitor = monitor;
    }

    @Override
    public FutureResult<T> compute() {
        return FutureResult.of(value, monitor);
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return getClass() == other.getClass() && Objects.equals(value, ((ConstantComputation<?>) other).value); //todo:monitor?
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getClass(), value); //todo: monitor?
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ConstantComputation<>(value, monitor);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", getClass().getSimpleName(), value.getClass().getSimpleName(), value);
    }
}
