package de.featjar.base.data;

import static de.featjar.base.computation.Computations.async;
import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.*;
import de.featjar.base.tree.structure.ITree;
import org.junit.jupiter.api.Test;

class IComputationTest {
    @Test
    void simpleComputation() {
        {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(FeatJAR.cache().has(computation));
        }
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        FeatJAR.run(fj -> {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.getResult().get());
            assertTrue(FeatJAR.cache().has(computation));
            assertFalse(FeatJAR.cache().getCachedComputations().isEmpty());
        });

        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
        {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(FeatJAR.cache().has(computation));
        }
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
    }

    static class ComputeIsEven extends AComputation<Boolean> implements IInputDependency<Integer> {
        protected static Dependency<Integer> INPUT = newRequiredDependency();

        public ComputeIsEven(IComputation<Integer> input) {
            dependOn(INPUT);
            setInput(input);
        }

        @Override
        public Dependency<Integer> getInputDependency() {
            return INPUT;
        }

        @Override
        public Result<Boolean> compute(DependencyList dependencyList, Progress progress) {
            return Result.of(dependencyList.get(INPUT) % 2 == 0);
        }

        @Override
        public ITree<IComputation<?>> cloneNode() {
            return new ComputeIsEven(getInput());
        }
    }

    @Test
    void chainedComputation() {
        IComputation<Integer> computation = Computations.of(42);
        IComputation<Boolean> isEvenComputation = new ComputeIsEven(async(42));
        assertTrue(isEvenComputation.getResult().get());
        assertTrue(computation.map(ComputeIsEven::new).getResult().get());
        assertTrue(computation.map(ComputeIsEven::new).getResult().get());
    }

    static class ComputeIsParity extends AComputation<Boolean> implements IInputDependency<Integer> {
        @Override
        public ITree<IComputation<?>> cloneNode() {
            return null;
        }

        enum Parity {
            EVEN,
            ODD
        }

        protected static Dependency<Integer> INPUT = newRequiredDependency();
        protected static Dependency<Parity> PARITY = newRequiredDependency();

        public ComputeIsParity(IComputation<Integer> input, IComputation<Parity> parity) {
            dependOn(INPUT, PARITY);
            setInput(input);
            setDependency(PARITY, parity);
        }

        @Override
        public Dependency<Integer> getInputDependency() {
            return INPUT;
        }

        @Override
        public Result<Boolean> compute(DependencyList dependencyList, Progress progress) {
            return Result.of(
                    dependencyList.get(PARITY) == Parity.EVEN
                            ? INPUT.get(dependencyList) % 2 == 0
                            : INPUT.get(dependencyList) % 2 == 1);
        }
    }

    @Test
    void computationWithArguments() {
        IComputation<Integer> computation = Computations.of(42);
        assertTrue(new ComputeIsParity(computation, async(ComputeIsParity.Parity.EVEN))
                .getResult()
                .get());
        assertFalse(new ComputeIsParity(computation, async(ComputeIsParity.Parity.ODD))
                .getResult()
                .get());
        assertTrue(computation
                .map(c -> new ComputeIsParity(c, async(ComputeIsParity.Parity.EVEN)))
                .getResult()
                .get());
    }

    @Test
    void allOfSimple() {
        Pair<Integer, Integer> r = Computations.of(Computations.of(1), Computations.of(2))
                .getResult()
                .get();
        assertEquals(1, r.getKey());
        assertEquals(2, r.getValue());
    }

    @Test
    void allOfComplex() {
        IComputation<Integer> c1 = Computations.of(42);
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computations.of(c1, c2).getResult().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }

    @Test
    void allOfSleep() {
        IComputation<Integer> c1 = new AComputation<>() {
            @Override
            public Result<Integer> compute(DependencyList dependencyList, Progress progress) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return Result.of(42);
            }

            @Override
            public ITree<IComputation<?>> cloneNode() {
                return null;
            }
        };
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computations.of(c1, c2).getResult().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }
}
