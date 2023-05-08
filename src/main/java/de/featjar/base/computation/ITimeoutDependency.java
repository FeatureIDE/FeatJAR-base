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
import java.time.Duration;

/**
 * A potentially long-running computation that can be canceled if a given time has passed.
 * This computation terminates with an empty {@link Result} when it has
 * not terminated until the timeout passes.
 * Assumes that the implementing class can be cast to {@link IComputation}.
 *
 * @author Elias Kuiter
 */
public interface ITimeoutDependency {
    /**
     * The default timeout returned by {@link #getTimeout()}, if not specified otherwise.
     * Specifies that no timeout should be set; that is, the analysis runs until it completes.
     */
    Duration DEFAULT_TIMEOUT = Duration.ZERO;

    /**
     * {@return the timeout dependency of this computation}
     */
    Dependency<Duration> getTimeoutDependency();

    /**
     * {@return the timeout computation of this computation}
     */
    default IComputation<Duration> getTimeout() {
        return getTimeoutDependency().get((IComputation<?>) this);
    }

    /**
     * Sets the timeout computation of this computation.
     *
     * @param timeout the timeout computation, if any
     */
    default void setTimeout(IComputation<Duration> timeout) {
        getTimeoutDependency().set((IComputation<?>) this, timeout);
    }
}
