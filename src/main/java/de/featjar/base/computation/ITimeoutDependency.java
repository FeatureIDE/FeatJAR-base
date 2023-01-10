package de.featjar.base.computation;

import de.featjar.base.data.Result;

/**
 * A potentially long-running computation that can be canceled if a given time has passed.
 * This computation terminates with an empty {@link Result} when it has
 * not terminated until the timeout passes.
 * Assumes that the implementing class can be cast to {@link IComputation}.
 *
 * @author Elias Kuiter
 */
public
interface ITimeoutDependency { // todo: how to handle partial results (i.e., to return a lower bound for counting)?
    /**
     * The default timeout returned by {@link #getTimeout()}, if not specified otherwise.
     * Specifies that no timeout should be set; that is, the analysis runs until it completes.
     */
    // todo: refactor to Duration (where Duration.ZERO is no timeout)
    long DEFAULT_TIMEOUT = -1;

    /**
     * {@return the timeout dependency of this computation}
     */
    Dependency<Long> getTimeoutDependency();

    /**
     * {@return the timeout computation in milliseconds of this computation}
     */
    default IComputation<Long> getTimeout() {
        return getTimeoutDependency().get((IComputation<?>) this);
    }

    /**
     * Sets the timeout computation in milliseconds of this computation.
     *
     * @param timeout the timeout computation in milliseconds, if any
     */
    default void setTimeout(IComputation<Long> timeout) {
        getTimeoutDependency().set((IComputation<?>) this, timeout);
    }
}
