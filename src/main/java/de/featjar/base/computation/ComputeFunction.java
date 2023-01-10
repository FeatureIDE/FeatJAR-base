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
import de.featjar.base.tree.structure.ITree;
import java.util.Objects;
import java.util.function.Function;

/**
 * A computation that maps one value to another.
 * As functions cannot be reliably checked for equality or hashed, an identifier must be explicitly passed.
 * The caller must guarantee that this identifier is unique.
 *
 * @param <T> the type of the mapped value
 * @param <U> the type of the mapped result
 * @author Elias Kuiter
 */
public class ComputeFunction<T, U> extends AComputation<U> implements IAnalysis<T, U> {
    protected static final Dependency<?> INPUT = newRequiredDependency();
    protected final Class<?> klass;
    protected final String scope;
    protected final Function<T, Result<U>> function;

    /**
     * Creates a function computation.
     *
     * @param input    the input computation
     * @param klass    the calling class
     * @param scope    the calling scope
     * @param function the mapper function
     */
    public ComputeFunction(IComputation<T> input, Class<?> klass, String scope, Function<T, Result<U>> function) {
        dependOn(INPUT);
        setInput(input);
        this.klass = klass;
        this.scope = scope;
        this.function = function;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dependency<T> getInputDependency() {
        return (Dependency<T>) INPUT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<U> compute(DependencyList dependencyList, Progress progress) {
        T input = (T) dependencyList.get(INPUT);
        return function.apply(input);
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return super.equalsNode(other)
                && Objects.equals(klass, ((ComputeFunction<?, ?>) other).klass)
                && Objects.equals(scope, ((ComputeFunction<?, ?>) other).scope); // todo:monitor?
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), klass, scope); // todo: monitor?
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputeFunction<>(getInput(), klass, scope, function);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", super.toString(), klass.getSimpleName(), scope);
    }
}
