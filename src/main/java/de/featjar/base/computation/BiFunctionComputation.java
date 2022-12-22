package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;

import java.util.Objects;
import java.util.function.Function;

/**
 * A computation that maps two values to a third.
 * As functions cannot be reliably checked for equality or hashed, an identifier must be explicitly passed.
 * The caller must guarantee that this identifier is unique.
 *
 * @param <T> the type of the first mapped value
 * @param <U> the type of the second mapped value
 * @param <V> the type of the mapped result
 */
public class BiFunctionComputation<T, U, V> {//  implements IComputation<V> {
//    protected static final Dependency<?> INPUT = newDependency();
//    protected final String identifier;
//    protected final Function<T, Result<U>> function;
//
//    /**
//     * Creates a function computation.
//     *
//     * @param input      the input computation
//     * @param identifier the unique identifier
//     * @param function   the mapper function
//     */
//    public BiFunctionComputation(IComputation<T> input, String identifier, Function<T, Result<U>> function) {
//        dependOn(INPUT);
//        setInput(input);
//        this.identifier = identifier;
//        this.function = function;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Dependency<T> getInputDependency() {
//        return (Dependency<T>) INPUT;
//    }
//
//    @Override
//    public FutureResult<U> compute() {
//        return getInput().get().thenComputeResult((t, monitor) -> function.apply(t));
//    }
//
//    @Override
//    public boolean equalsNode(IComputation<?> other) {
//        return super.equalsNode(other) && Objects.equals(identifier, ((BiFunctionComputation<?, ?>) other).identifier); //todo:monitor?
//    }
//
//    @Override
//    public int hashCodeNode() {
//        return Objects.hash(super.hashCodeNode(), identifier); //todo: monitor?
//    }
//
//    @Override
//    public ITree<IComputation<?>> cloneNode() {
//        return new BiFunctionComputation<>(getInput(), identifier, function);
//    }
//
//    @Override
//    public String toString() {
//        return String.format("%s(%s)", super.toString(), identifier);
//    }
}
