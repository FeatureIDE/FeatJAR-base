package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.task.IMonitor;
import de.featjar.base.tree.structure.ITree;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A computation that maps one value to another.
 * As functions cannot be reliably checked for equality or hashed, an identifier must be explicitly passed.
 * The caller must guarantee that this identifier is unique.
 *
 * @param <T> the type of the mapped value
 * @param <U> the type of the mapped result
 * @author Elias Kuiter
 */
public class ComputeFunction<T, U> extends AComputation<U> implements IAnalysis<T, U> {
    protected static final Dependency<?> INPUT = newRequiredDependency();
    protected final Class<?> klass;
    protected final String scope;
    protected final Function<T, Result<U>> function;

    /**
     * Creates a function computation.
     *
     * @param input    the input computation
     * @param klass    the calling class
     * @param scope    the calling scope
     * @param function the mapper function
     */
    public ComputeFunction(IComputation<T> input, Class<?> klass, String scope, Function<T, Result<U>> function) {
        dependOn(INPUT);
        setInput(input);
        this.klass = klass;
        this.scope = scope;
        this.function = function;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dependency<T> getInputDependency() {
        return (Dependency<T>) INPUT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<U> computeResult(List<?> results, IMonitor monitor) {
        T input = (T) INPUT.get(results);
        return function.apply(input);
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return super.equalsNode(other)
                && Objects.equals(klass, ((ComputeFunction<?, ?>) other).klass)
                && Objects.equals(scope, ((ComputeFunction<?, ?>) other).scope); //todo:monitor?
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), klass, scope); //todo: monitor?
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputeFunction<>(getInput(), klass, scope, function);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", super.toString(), klass.getSimpleName(), scope);
    }
}
