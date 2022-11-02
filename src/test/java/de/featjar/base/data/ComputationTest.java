package de.featjar.base.data;

import de.featjar.base.Feat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


class ComputationTest {
    @Test
    void simpleComputation() {
        {
            Computation<Integer> computation = Computation.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(Feat.store().has(computation));
        }
        assertTrue(Feat.store().computationMap.isEmpty());

        Feat.run(fj -> {
            Computation<Integer> computation = Computation.of(42);
            assertEquals(42, computation.getResult().get());
            assertTrue(Feat.store().has(computation));
            assertFalse(Feat.store().computationMap.isEmpty());
        });

        assertTrue(Feat.store().computationMap.isEmpty());
        {
            Computation<Integer> computation = Computation.of(42);
            assertEquals(42, computation.getResult().get());
            assertFalse(Feat.store().has(computation));
        }
        assertTrue(Feat.store().computationMap.isEmpty());
    }

    static class IsEvenComputation implements Computation<Boolean> {
        Computation<Integer> inputComputation;

        public IsEvenComputation(Computation<Integer> inputComputation) {
            this.inputComputation = inputComputation;
        }

        @Override
        public FutureResult<Boolean> compute() {
            return inputComputation.compute().thenCompute((integer, monitor) -> integer % 2 == 0);
        }
    }

    @Test
    void chainedComputation() {
        Computation<Integer> computation = Computation.of(42);
        Computation<Boolean> isEvenComputation = new Computation<>() {
            @Override
            public FutureResult<Boolean> compute() {
                return computation.compute().thenCompute((integer, monitor) -> integer % 2 == 0);
            }
        };
        assertTrue(isEvenComputation.getResult().get());
        assertTrue(computation.then(IsEvenComputation.class).getResult().get());
        assertTrue(computation.then(IsEvenComputation::new).getResult().get());
    }

    static class IsParityComputation implements Computation<Boolean> {
        enum Parity { EVEN, ODD }
        Computation<Integer> inputComputation;
        Parity parity;

        public IsParityComputation(Computation<Integer> inputComputation, Parity parity) {
            this.inputComputation = inputComputation;
            this.parity = parity;
        }

        @Override
        public FutureResult<Boolean> compute() {
            return inputComputation.compute().thenCompute(
                    (integer, monitor) -> parity == Parity.EVEN ? integer % 2 == 0 : integer % 2 == 1);
        }
    }

    @Test
    void computationWithArguments() {
        Computation<Integer> computation = Computation.of(42);
        assertTrue(new IsParityComputation(computation, IsParityComputation.Parity.EVEN).getResult().get());
        assertFalse(new IsParityComputation(computation, IsParityComputation.Parity.ODD).getResult().get());
        assertTrue(computation.then(IsParityComputation.class, IsParityComputation.Parity.EVEN).getResult().get());
        assertTrue(computation.then(c -> new IsParityComputation(c, IsParityComputation.Parity.EVEN)).getResult().get());
    }

    @Test
    void allOfSimple() {
        List<?> r = Computation.allOf(Computation.of(1), Computation.of(2)).getResult().get();
        assertEquals(1, r.get(0));
        assertEquals(2, r.get(1));
    }

    @Test
    void allOfComplex() {
        Computation<Integer> c1 = Computation.of(42);
        Computation<Boolean> c2 = c1.then(IsEvenComputation.class);
        List<?> r = Computation.allOf(c1, c2).getResult().get();
        assertEquals(42, r.get(0));
        assertEquals(true, r.get(1));
    }

    @Test
    void allOfSleep() {
        Computation<Integer> c1 = new Computation<>() {
            @Override
            public FutureResult<Integer> compute() {
                return FutureResult.wrap(CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return 42;
                }));
            }
        };
        Computation<Boolean> c2 = c1.then(IsEvenComputation.class);
        List<?> r = Computation.allOf(c1, c2).getResult().get();
        System.out.println(r);
        assertEquals(42, r.get(0));
        assertEquals(true, r.get(1));
    }
}