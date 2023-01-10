package de.featjar.base.computation;

import static de.featjar.base.computation.Computations.async;

import de.featjar.base.tree.structure.ATree;
import java.util.List;
import java.util.Objects;

/**
 * A dependency of a computation.
 * Describes the dependency without storing its actual value, which is passed in a {@link DependencyList} to {@link IComputation#compute(DependencyList, Progress)}.
 *
 * @param <U> the type of the dependency's computation result
 * @author Elias Kuiter
 */
public class Dependency<U> extends ATree.Entry<IComputation<?>, IComputation<U>> { // todo: U extends Serializable?
    /**
     * Creates a new required dependency.
     */
    public Dependency() {}

    /**
     * Creates a new optional dependency with a given default value.
     *
     * @param defaultValue the default value
     */
    public Dependency(U defaultValue) {
        super(async(Objects.requireNonNull(defaultValue, "default value must not be null")));
    }

    @SuppressWarnings("unchecked")
    public U get(List<?> list) {
        if (index < 0 || index >= list.size()) throw new IllegalArgumentException();
        return (U) list.get(index);
    }
}
