package de.featjar.base.computation;

import de.featjar.base.FeatJAR;
import de.featjar.base.tree.structure.ATree;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * Primary implementation of {@link IComputation}.
 *
 * @param <T> the type of the computation result
 * @author Elias Kuiter
 */
public abstract class AComputation<T> extends ATree<IComputation<?>> implements IComputation<T> {
    protected AComputation(IComputation<?>... computations) {
        if (computations.length > 0)
            super.setChildren(Arrays.asList(computations));
    }

    protected AComputation(List<? extends IComputation<?>> computations) {
        super.setChildren(computations);
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return (getClass() == other.getClass());
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getClass());
    }

    /**
     * Declares all dependencies of this computation class.
     * This method must be called once per computation class at the top of its constructor.
     * Each dependency is then assigned an ascending index into the children of this computation, viewed as a tree.
     *
     * @param dependencies the dependencies
     */
    @SuppressWarnings("unchecked")
    protected void dependOn(Dependency<?>... dependencies) {
        FeatJAR.dependencyManager().register((Class<? extends IComputation<?>>) getClass(), dependencies);
    }

    /**
     * {@return a new dependency for this computation class}
     * Should only be called in a static context to avoid creating unnecessary objects.
     *
     * @param <U> the type of the dependency's computation result
     */
    protected static <U> Dependency<U> newDependency() {
        return new Dependency<>();
    }

    /**
     * {@return a new dependency for this computation class with a given default value}
     * Should only be called in a static context to avoid creating unnecessary objects.
     *
     * @param defaultValue the default value
     * @param <U> the type of the dependency's computation result
     */
    protected static <U> Dependency<U> newDependency(U defaultValue) {
        return new Dependency<>(defaultValue);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
