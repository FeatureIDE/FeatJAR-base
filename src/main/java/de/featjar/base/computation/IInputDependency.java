package de.featjar.base.computation;

/**
 * A computation that depends on some (primary, typically mandatory) input.
 * Assumes that the implementing class can be cast to {@link IComputation}.
 *
 * @param <T> the type of the input
 * @author Elias Kuiter
 */
public interface IInputDependency<T> {
    /**
     * {@return the input dependency of this computation}
     */
    Dependency<T> getInputDependency();

    /**
     * {@return the input computation of this computation}
     */
    default IComputation<T> getInput() {
        return getInputDependency().get((IComputation<?>) this);
    }

    /**
     * Sets the input computation of this computation.
     *
     * @param input the input computation
     */
    default void setInput(IComputation<T> input) {
        getInputDependency().set((IComputation<?>) this, input);
    }
}
