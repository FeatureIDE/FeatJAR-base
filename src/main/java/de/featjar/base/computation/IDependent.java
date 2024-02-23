/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import java.util.List;
import java.util.function.Supplier;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * An {@link IDependent} does not contain the computation result itself, it only computes it on demand as a {@link Supplier}.
 * There are several modes for computing the result of an {@link IDependent}:
 * The computation mode can be either synchronous (e.g., {@link IComputation#computeResult()}) or asynchronous,
 * returning a {@link Result} or {@link FutureResult}, respectively.
 * An asynchronous {@link FutureResult} can be shared between different threads.
 * It can be waited for, it can be cancelled, and its progress can be tracked, so it is well-suited for a graphical user interface.
 * A synchronous {@link Result} is computed on the current thread in a blocking fashion.
 * Synchronous computation modes are well-suited for a command-line interface.
 * In addition, the computation mode can either leverage results stored in a {@link Cache} (e.g., the global cache in {@link FeatJAR#cache()}).
 * Caching computation modes are well-suited for implementing knowledge compilation, incremental analyses, and evolution operators.
 * Computations can depend on other computations by declaring a {@link Dependency} of type {@code T}.
 * Thus, every computation is a tree of computations, where the dependencies of the computation are its children.
 * When a child is {@link Result#empty(Problem...)}, this signals an unrecoverable error by default, this behavior can be overridden with {@link IComputation#mergeResults(List)}.
 * Thus, every required dependency must be set to a non-null value in the constructor, and every optional dependency must have a non-null default value.
 * To ensure the determinism required by caching, all parameters of a computation must be depended on (including sources of randomness).
 * Also, all used data structures must be deterministic (e.g., by using {@link de.featjar.base.data.Maps} and {@link de.featjar.base.data.Sets}).
 * Implementors should pass mandatory parameters in the constructor and optional parameters using dedicated setters.
 * This can be facilitated by using specializations of {@link IDependent}.
 * Though not necessary, it is recommended to implement this interface by subclassing {@link AComputation}, which provides a mechanism for declaring dependencies.
 * It is strongly discouraged to implement this interface anonymously to ensure correct caching
 * with {@link Cache.CachePolicy#CACHE_TOP_LEVEL} and correct hash code and equality computations.
 * To compose anonymous computations, consider using {@link ComputeFunction} instead.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public interface IDependent extends ITree<IComputation<?>> {

    // TODO inline
    static <U> U getValue(Dependency<U> dependency, List<Object> values) {
        return dependency.getValue(values);
    }

    /**
     * Sets the computation for a given dependency of this computation.
     *
     * @param dependency  the dependency
     * @param computation the computation
     * @param <U>         the type of the computation result
     */
    // TODO rename to setDependencyComputation
    default <U> IDependent setDependencyComputation(Dependency<U> dependency, IComputation<? extends U> computation) {
        replaceChild(dependency.getIndex(), computation);
        return this;
    }

    // TODO rename to getDependencyComputation
    @SuppressWarnings("unchecked")
    default <U> Result<IComputation<U>> getDependency(Dependency<U> dependency) {
        return getChild(dependency.getIndex()).map(c -> (IComputation<U>) c);
    }
}
