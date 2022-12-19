package de.featjar.base.data;

import de.featjar.base.Feat;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


class ComputationTest {
    @Test
    void simpleComputation() {
        {
            Computation<Integer> computation = Computation.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(Feat.cache().has(computation));
        }
        assertTrue(Feat.cache().computationMap.isEmpty());

        Feat.run(fj -> {
            Computation<Integer> computation = Computation.of(42);
            assertEquals(42, computation.getResult().get());
            assertTrue(Feat.cache().has(computation));
            assertFalse(Feat.cache().computationMap.isEmpty());
        });

        assertTrue(Feat.cache().computationMap.isEmpty());
        {
            Computation<Integer> computation = Computation.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(Feat.cache().has(computation));
        }
        assertTrue(Feat.cache().computationMap.isEmpty());
    }

    static class ComputeIsEven implements Computation<Boolean> {
        Computation<Integer> input;

        public ComputeIsEven(Computation<Integer> input) {
            this.input = input;
        }

        @Override
        public FutureResult<Boolean> compute() {
            return input.get().thenCompute((integer, monitor) -> integer % 2 == 0);
        }
    }

    @Test
    void chainedComputation() {
        Computation<Integer> computation = Computation.of(42);
        Computation<Boolean> isEvenComputation = () -> computation.get().thenCompute((integer, monitor) -> integer % 2 == 0);
        assertTrue(isEvenComputation.getResult().get());
        assertTrue(computation.map(ComputeIsEven::new).getResult().get());
        assertTrue(computation.map(ComputeIsEven::new).getResult().get());
    }

    static class ComputeIsParity implements Computation<Boolean> {
        enum Parity { EVEN, ODD }
        Computation<Integer> input;
        Parity parity;

        public ComputeIsParity(Computation<Integer> input, Parity parity) {
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
        Computation<Integer> computation = Computation.of(42);
        assertTrue(new ComputeIsParity(computation, ComputeIsParity.Parity.EVEN).getResult().get());
        assertFalse(new ComputeIsParity(computation, ComputeIsParity.Parity.ODD).getResult().get());
        assertTrue(computation.map(c -> new ComputeIsParity(c, ComputeIsParity.Parity.EVEN)).getResult().get());
    }

    @Test
    void allOfSimple() {
        Pair<Integer, Integer> r = Computation.of(Computation.of(1), Computation.of(2)).getResult().get();
        assertEquals(1, r.getKey());
        assertEquals(2, r.getValue());
    }

    @Test
    void allOfComplex() {
        Computation<Integer> c1 = Computation.of(42);
        Computation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computation.of(c1, c2).getResult().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }

    @Test
    void allOfSleep() {
        Computation<Integer> c1 = () -> FutureResult.wrap(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 42;
        }));
        Computation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computation.of(c1, c2).getResult().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }
}