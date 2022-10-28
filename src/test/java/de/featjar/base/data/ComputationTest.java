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
        Feat.store().clear();
        Computation<Integer> computation = Computation.of(42);
        assertEquals(42, computation.computeResult().get());
        assertEquals(42, computation.computeResult().get());
        assertFalse(Feat.store().has(computation));
        assertEquals(42, computation.getResult().get());
        assertEquals(42, computation.getResult().get());
        assertTrue(Feat.store().has(computation));
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
        assertTrue(isEvenComputation.computeResult().get());
        assertTrue(computation.then(IsEvenComputation.class).computeResult().get());
        assertTrue(computation.then(IsEvenComputation::new).computeResult().get());
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
        assertTrue(new IsParityComputation(computation, IsParityComputation.Parity.EVEN).computeResult().get());
        assertFalse(new IsParityComputation(computation, IsParityComputation.Parity.ODD).computeResult().get());
        assertTrue(computation.then(IsParityComputation.class, IsParityComputation.Parity.EVEN).computeResult().get());
        assertTrue(computation.then(c -> new IsParityComputation(c, IsParityComputation.Parity.EVEN)).computeResult().get());
    }

    @Test
    void allOfSimple() {
        List<?> r = Computation.allOf(Computation.of(1), Computation.of(2)).computeResult().get();
        assertEquals(1, r.get(0));
        assertEquals(2, r.get(1));
    }

    @Test
    void allOfComplex() {
        Computation<Integer> c1 = Computation.of(42);
        Computation<Boolean> c2 = c1.then(IsEvenComputation.class);
        List<?> r = Computation.allOf(c1, c2).computeResult().get();
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
        List<?> r = Computation.allOf(c1, c2).computeResult().get();
        System.out.println(r);
        assertEquals(42, r.get(0));
        assertEquals(true, r.get(1));
    }
}