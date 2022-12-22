package de.featjar.base.computation;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.task.Monitor;
import de.featjar.base.tree.structure.LeafNode;
import de.featjar.base.tree.structure.Traversable;
import de.featjar.base.tree.structure.Tree;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * Primary implementation of {@link IComputation}.
 *
 * @param <T> the type of the computation result
 * @author Elias Kuiter
 */
public abstract class AComputation<T> extends Tree<IComputation<?>> implements IComputation<T> {
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

    /**
     * A constant computation.
     * Always computes the same value.
     * The leaves of a computation tree are precisely its constant computations.
     *
     * @param <T> the type of the computed value
     */
    public static class Constant<T> extends LeafNode<IComputation<?>> implements IComputation<T> {
        protected final T value;
        protected final Monitor monitor;

        /**
         * Creates a constant computation.
         *
         * @param value the value
         * @param monitor the monitor
         */
        public Constant(T value, Monitor monitor) {
            this.value = value;
            this.monitor = monitor;
        }

        @Override
        public FutureResult<T> compute() {
            return FutureResult.of(value, monitor);
        }

        @Override
        public boolean equalsNode(IComputation<?> other) {
            return getClass() == other.getClass() && Objects.equals(value, ((Constant<?>) other).value); //todo:monitor?
        }

        @Override
        public int hashCodeNode() {
            return Objects.hash(getClass(), value); //todo: monitor?
        }

        @Override
        public Traversable<IComputation<?>> cloneNode() {
            return new Constant<>(value, monitor);
        }
    }

    /**
     * A computation that maps one value to another.
     * As functions cannot be reliably checked for equality or hashed, an identifier must be explicitly passed.
     * The caller must guarantee that this identifier is unique.
     *
     * @param <T> the type of the mapped value
     * @param <U> the type of the mapped result
     */
    public static class Mapper<T, U> extends AComputation<U> implements IAnalysis<T, U> {
        protected static final Dependency<?> INPUT = newDependency();
        protected final String identifier;
        protected final Function<T, Result<U>> function;

        /**
         * Creates a constant computation.
         *
         * @param input the input computation
         * @param identifier the unique identifier
         * @param function the mapper function
         */
        public Mapper(IComputation<T> input, String identifier, Function<T, Result<U>> function) {
            dependOn(INPUT);
            setInput(input);
            this.identifier = identifier;
            this.function = function;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Dependency<T> getInputDependency() {
            return (Dependency<T>) INPUT;
        }

        @Override
        public FutureResult<U> compute() {
            return getInput().get().thenComputeResult((t, monitor) -> function.apply(t));
        }

        @Override
        public boolean equalsNode(IComputation<?> other) {
            return super.equalsNode(other) && Objects.equals(identifier, ((Mapper<?, ?>) other).identifier); //todo:monitor?
        }

        @Override
        public int hashCodeNode() {
            return Objects.hash(super.hashCodeNode(), identifier); //todo: monitor?
        }

        @Override
        public Traversable<IComputation<?>> cloneNode() {
            return new Mapper<>(getInput(), identifier, function);
        }
    }

    /**
     * A computation that computes all its dependencies.
     * If any dependency fails to compute, the entire computation fails.
     */
    public static class AllOf extends AComputation<List<?>> {
        public AllOf(IComputation<?>... computations) {
            super(computations);
        }

        @Override
        public FutureResult<List<?>> compute() {
            System.out.println("compute all of " + getChildren());
            List<FutureResult<?>> futureResults =
                    getChildren().stream().map(IComputation::compute).collect(Collectors.toList());
            return FutureResult.ofCompletableFuture(FutureResult.allOf(futureResults.toArray(CompletableFuture[]::new)))
                    .thenComputeFromResult((unused, monitor) -> {
                        System.out.println("computing all of " + futureResults);
                        List<?> x = futureResults.stream()
                                .map(FutureResult::get)
                                .map(Result::get)
                                .collect(Collectors.toList());
                        System.out.println(x);
                        return x.stream().noneMatch(Objects::isNull) ? Result.of(x) : Result.empty();
                    });
        }

        @Override
        public Traversable<IComputation<?>> cloneNode() {
            return new AllOf();
        }
    }
}
