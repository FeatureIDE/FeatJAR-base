package de.featjar.base.computation;

import de.featjar.base.tree.structure.Tree;

import static de.featjar.base.computation.Computations.async;

/**
 * A dependency of a computation.
 *
 * @param <U> the type of the dependency's computation result
 */
public class Dependency<U> extends Tree.Entry<IComputation<?>, IComputation<U>> {
    /**
     * Creates a new dependency.
     */
    public Dependency() {
        this(null);
    }

    /**
     * Creates a new dependency with a given default value.
     *
     * @param defaultValue the default value
     */
    public Dependency(U defaultValue) {
        super(async(defaultValue));
    }
}
