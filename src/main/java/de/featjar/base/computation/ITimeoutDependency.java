/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
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
