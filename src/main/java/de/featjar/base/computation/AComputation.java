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
    protected Cache cache = FeatJAR.cache();

    protected AComputation(IComputation<?>... computations) {
        if (computations.length > 0) super.setChildren(Arrays.asList(computations));
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

    @Override
    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    /**
     * Declares all dependencies of this computation class.
     * This method must be called once per computation class at the top of its constructor.
     * Each passed dependency must be a static member of this computation class.
     * Each dependency is then assigned an ascending index into the children of this computation, viewed as a tree.
     *
     * @param dependencies the dependencies
     */
    protected void dependOn(Dependency<?>... dependencies) {
        dependOn(List.of(dependencies));
    }

    /**
     * Declares all dependencies of this computation class.
     * This method must be called once per computation class at the top of its constructor.
     * Each passed dependency must be a static member of this computation class.
     * Each dependency is then assigned an ascending index into the children of this computation, viewed as a tree.
     *
     * @param dependencies the dependencies
     */
    protected void dependOn(List<Dependency<?>> dependencies) {
        if (!dependencies.isEmpty() && dependencies.get(0).getIndex() == -1) {
            for (int i = 0; i < dependencies.size(); i++) {
                dependencies.get(i).setIndex(i);
            }
        }
        dependencies.forEach(dependency -> dependency.setToDefaultValue(this));
    }

    /**
     * {@return a new required dependency for this computation class}
     * Should only be called in a static context to avoid creating unnecessary objects.
     * A required dependency must be set in the constructor of the computation class.
     *
     * @param <U> the type of the dependency's computation result
     */
    protected static <U> Dependency<U> newRequiredDependency() {
        return new Dependency<>();
    }

    /**
     * {@return a new optional dependency for this computation class with a given default value}
     * Should only be called in a static context to avoid creating unnecessary objects.
     * An optional dependency should not be set in the constructor of the computation class.
     *
     * @param defaultValue the default value
     * @param <U> the type of the dependency's computation result
     */
    protected static <U> Dependency<U> newOptionalDependency(U defaultValue) {
        return new Dependency<>(defaultValue);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
