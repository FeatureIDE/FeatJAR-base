package de.featjar.base.data;

import de.featjar.base.Feat;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.FutureResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


class ComputationTest {
    @Test
    void simpleComputation() {
        {
            IComputation<Integer> computation = IComputation.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(Feat.cache().has(computation));
        }
        assertTrue(Feat.cache().computationMap.isEmpty());

        Feat.run(fj -> {
            IComputation<Integer> computation = IComputation.of(42);
            assertEquals(42, computation.getResult().get());
            assertTrue(Feat.cache().has(computation));
            assertFalse(Feat.cache().computationMap.isEmpty());
        });

        assertTrue(Feat.cache().computationMap.isEmpty());
        {
            IComputation<Integer> computation = IComputation.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(Feat.cache().has(computation));
        }
        assertTrue(Feat.cache().computationMap.isEmpty());
    }

    static class ComputeIsEven implements IComputation<Boolean> {
        IComputation<Integer> input;

        public ComputeIsEven(IComputation<Integer> input) {
            this.input = input;
        }

        @Override
        public FutureResult<Boolean> compute() {
            return input.get().thenCompute((integer, monitor) -> integer % 2 == 0);
        }
    }

    @Test
    void chainedComputation() {
        IComputation<Integer> computation = IComputation.of(42);
        IComputation<Boolean> isEvenComputation = () -> computation.get().thenCompute((integer, monitor) -> integer % 2 == 0);
        assertTrue(isEvenComputation.getResult().get());
        assertTrue(computation.map(ComputeIsEven::new).getResult().get());
        assertTrue(computation.map(ComputeIsEven::new).getResult().get());
    }

    static class ComputeIsParity implements IComputation<Boolean> {
        enum Parity { EVEN, ODD }
        IComputation<Integer> input;
        Parity parity;

        public ComputeIsParity(IComputation<Integer> input, Parity parity) {
            this.input = input;
            this.parity = parity;
        }

        @Override
        public FutureResult<Boolean> compute() {
            return input.get().thenCompute(
                    (integer, monitor) -> parity == Parity.EVEN ? integer % 2 == 0 : integer % 2 == 1);
        }
    }

    @Test
    void computationWithArguments() {
        IComputation<Integer> computation = IComputation.of(42);
        assertTrue(new ComputeIsParity(computation, ComputeIsParity.Parity.EVEN).getResult().get());
        assertFalse(new ComputeIsParity(computation, ComputeIsParity.Parity.ODD).getResult().get());
        assertTrue(computation.map(c -> new ComputeIsParity(c, ComputeIsParity.Parity.EVEN)).getResult().get());
    }

    @Test
    void allOfSimple() {
        Pair<Integer, Integer> r = IComputation.of(IComputation.of(1), IComputation.of(2)).getResult().get();
        assertEquals(1, r.getKey());
        assertEquals(2, r.getValue());
    }

    @Test
    void allOfComplex() {
        IComputation<Integer> c1 = IComputation.of(42);
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = IComputation.of(c1, c2).getResult().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }

    @Test
    void allOfSleep() {
        IComputation<Integer> c1 = () -> FutureResult.ofCompletableFuture(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 42;
        }));
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = IComputation.of(c1, c2).getResult().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }
}