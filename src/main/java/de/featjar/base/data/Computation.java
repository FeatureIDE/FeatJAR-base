/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.base.data;

import de.featjar.base.Feat;
import de.featjar.base.extension.Extension;
import de.featjar.base.task.CancelableMonitor;
import de.featjar.base.task.Monitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * A {@link Computation} does not contain the computation result itself, which is only computed on demand.
 * If computed with {@link #get()} or {@link #compute()}, the result is returned as an
 * asynchronous {@link FutureResult}, which can be shared, cached, and waited for.
 * When computed with {@link #get()}, results are possibly cached in a {@link Cache}; {@link #compute()} does not cache.
 * Computation progress can optionally be reported with a {@link Monitor}.
 * Computations can depend on other computations by assigning them to fields and calling {@link #allOf(Computation[])}
 * or {@link FutureResult#thenCompute(BiFunction)} in {@link #compute()}.
 * To ensure the determinism required by caching, all parameters of a computation must be stored in fields.
 * Whether a parameter of type T should be stored as T or Computation&lt;T&gt; depends on whether the parameter
 * is expected to depend on other computation's results.
 * Implementors can choose whether to pass parameters in the constructor (for few parameters) or
 * using dedicated setters (for many parameters).
 * In case of dedicated setters, the computation itself should be returned to allow for fluent configuration.
 * TODO: a validation scheme (e.g., against a simple feature model) and serialization scheme
 *  (e.g., to sensibly compare and cache computations based on their parameters and hash code) are missing for now.
 *  javadoc is also missing.
 *  monitor and store should be injected once (see notes in {@link Monitor}, and then not worried about any further.
 *  hash code computation is completely missing, so caching does not work well at all right now.
 *
 * @param <T> the type of the computation result
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Computation<T> extends Supplier<FutureResult<T>>, Extension {
    /**
     * {@return the (newly computed) asynchronous result of this computation}
     * The result is returned asynchronously; that is, as a {@link FutureResult}.
     * Calling this function directly is discouraged, as the result is forced to be re-computed.
     * Usually, you should call {@link #get()} instead to leverage cached results.
     * Do not rename this method, as the {@link Cache.CachingPolicy} performs
     * reflection that depends on its name to detect nested computations.
     */
    FutureResult<T> compute();

    /**
     * {@return the (possibly cached) asynchronous result of this computation}
     * Behaves just like {@link #compute()}, but tries to hit the {@link Cache} first.
     */
    @Override
    default FutureResult<T> get() {
        return Feat.cache().compute(this);
    }

    /**
     * {@return the (possibly cached) synchronous result of this computation}
     * The result is returned synchronously; that is, as a {@link Result}.
     * Like {@link #get()}, tries to hit the {@link Cache} before calling {@link #compute()}.
     */
    default Result<T> getResult() {
        return get().get();
    }

    static <T> Computation<T> of(T object, Monitor monitor) {
        return () -> FutureResult.of(object, monitor);
    }

    static <T> Computation<T> of(T object) {
        return of(object, new CancelableMonitor());
    }

    static <T> Computation<T> empty() {
        return of(null, new CancelableMonitor());
    }

    static Computation<List<?>> allOf(Computation<?>... computations) {
        // TODO: hashcode/caching of anonymous computation?
        return () -> {
            List<FutureResult<?>> futureResults =
                    Arrays.stream(computations).map(Computation::compute).collect(Collectors.toList());
            return FutureResult.wrap(FutureResult.allOf(futureResults.toArray(CompletableFuture[]::new)))
                    .thenComputeFromResult((unused, monitor) -> {
                        List<?> x = futureResults.stream()
                                .map(FutureResult::get)
                                .map(Result::get)
                                .collect(Collectors.toList());
                        return x.stream().noneMatch(Objects::isNull) ? Result.of(x) : Result.empty();
                    });

        };
    }

    // TODO: anyOf?

    default <U extends Computation<?>> U then(Function<Computation<T>, U> computationFunction) {
        return computationFunction.apply(this);
    }

    /*
     TODO: keep this?
      pro: also usable with non-final variables, does not require understanding of lambdas
      con: args not type-checked
    */
    default <U> Computation<U> then(Class<? extends Computation<U>> computationClass, Object... args) {
        List<Object> arguments = new ArrayList<>();
        List<Class<?>> argumentClasses = new ArrayList<>();
        arguments.add(this);
        argumentClasses.add(Computation.class);
        arguments.addAll(List.of(args));
        argumentClasses.addAll(Arrays.stream(args).map(Object::getClass).collect(Collectors.toList()));
        try {
            Constructor<? extends Computation<U>> constructor = computationClass.getConstructor(argumentClasses.toArray(Class[]::new));
            return constructor.newInstance(arguments.toArray(Object[]::new));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    default <U> Computation<U> map(Function<T, U> computationFunction) {
        // TODO: what is the hash code? probably Computation.this + the identity of computationFunction??
        return () -> Computation.this.get().thenCompute((t, monitor) -> computationFunction.apply(t));
    }

    // TODO: hashcode depends on all inputs. is there a default hashcode implementation?
    //  use in equals + hashcode. requires that c1.serialize() == c2.serialize ==> same computation result. could abstract away complex identities to improve caching.
    // void serialize();

    // TODO: validate whether a computation is sensible. maybe by encoding valid computations in a feature model.
    // boolean validate();

    // TODO: besides using feature modeling to "magically" complete computations, it may be nice to denote THE canonical best input for a computation.
    //  maybe this can also be done with alternative constructors or something?
//    /**
//     * {@return the preferred computation for the input of this computation}
//     * Can be used to specify the recommended input for this computation.
//     * @param <S> the type of the input of the preferred input computation
//     */
//    default <S> Optional<Computation<S, T>> getPreferredInputComputation() {
//        return Optional.empty();
//    }
}
