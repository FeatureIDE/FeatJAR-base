package de.featjar.base.computation;

import de.featjar.base.data.Result;
import de.featjar.base.task.Monitor;
import de.featjar.base.tree.structure.Traversable;
import de.featjar.base.tree.structure.Tree;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Computation<T> extends Tree<Computable<?>> implements Computable<T> {
    protected Computation(Computable<?>... dependencies) {
        if (dependencies.length > 0)
            super.setChildren(Arrays.asList(dependencies));
    }

    protected Computation(List<? extends Computable<?>> dependencies) {
        super.setChildren(dependencies);
    }

    @Override
    public boolean equalsNode(Computable<?> other) {
        return (getClass() == other.getClass());
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getClass());
    }

    @SuppressWarnings("unchecked")
    protected void dependOn(Dependency<?>... dependencies) {
        Dependency.register((Class<? extends Computable<?>>) getClass(), dependencies);
    }

    protected static <U> Dependency<U> newDependency() {
        return new Dependency<>();
    }

    protected static <U> Dependency<U> newDependency(U defaultValue) {
        return new Dependency<>(defaultValue);
    }

    public static class Constant<T> extends Computation<T> {
        protected final T value;
        protected final Monitor monitor;

        public Constant(T value, Monitor monitor) {
            this.value = value;
            this.monitor = monitor;
        }

        @Override
        public FutureResult<T> compute() {
            return FutureResult.of(value, monitor);
        }

        @Override
        public boolean equalsNode(Computable<?> other) {
            return super.equalsNode(other) && Objects.equals(value, ((Constant<?>) other).value); //todo:monitor?
        }

        @Override
        public int hashCodeNode() {
            return Objects.hash(super.hashCodeNode(), value); //todo: monitor?
        }

        @Override
        public Traversable<Computable<?>> cloneNode() {
            return new Constant<>(value, monitor);
        }
    }

    public static class Mapper<T, U> extends Computation<U> implements Analysis<T, U> {
        protected static final Dependency<?> INPUT = newDependency();
        protected final String identifier;
        protected final Function<T, Result<U>> function;

        public Mapper(Computable<T> input, String identifier, Function<T, Result<U>> function) {
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
        public boolean equalsNode(Computable<?> other) {
            return super.equalsNode(other) && Objects.equals(identifier, ((Mapper<?, ?>) other).identifier); //todo:monitor?
        }

        @Override
        public int hashCodeNode() {
            return Objects.hash(super.hashCodeNode(), identifier); //todo: monitor?
        }

        @Override
        public Traversable<Computable<?>> cloneNode() {
            return new Mapper<>(getInput(), identifier, function);
        }
    }

    public static class AllOf extends Computation<List<?>> {
        public AllOf(Computable<?>... dependencies) {
            super(dependencies);
        }

        @Override
        public FutureResult<List<?>> compute() {
            System.out.println("compute all of " + getChildren());
            List<FutureResult<?>> futureResults =
                    getChildren().stream().map(Computable::compute).collect(Collectors.toList());
            return FutureResult.wrap(FutureResult.allOf(futureResults.toArray(CompletableFuture[]::new)))
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
        public Traversable<Computable<?>> cloneNode() {
            return new AllOf();
        }
    }
}
