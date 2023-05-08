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
package de.featjar.base.data;

import static de.featjar.base.computation.Computations.async;
import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.*;
import de.featjar.base.tree.structure.ITree;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class IComputationTest {
    @Test
    void simpleComputation() {
        {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.get().get());
            assertFalse(FeatJAR.cache().has(computation));
        }
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        FeatJAR.run(fj -> {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.get().get());
            assertTrue(FeatJAR.cache().has(computation));
            assertFalse(FeatJAR.cache().getCachedComputations().isEmpty());
        });

        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
        {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.get().get());
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
        assertTrue(isEvenComputation.get().get());
        assertTrue(computation.map(ComputeIsEven::new).get().get());
        assertTrue(computation.map(ComputeIsEven::new).get().get());
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
                .get()
                .get());
        assertFalse(new ComputeIsParity(computation, async(ComputeIsParity.Parity.ODD))
                .get()
                .get());
        assertTrue(computation
                .map(c -> new ComputeIsParity(c, async(ComputeIsParity.Parity.EVEN)))
                .get()
                .get());
    }

    @Test
    void allOfSimple() {
        Pair<Integer, Integer> r =
                Computations.of(Computations.of(1), Computations.of(2)).get().get();
        assertEquals(1, r.getKey());
        assertEquals(2, r.getValue());
    }

    @Test
    void allOfComplex() {
        IComputation<Integer> c1 = Computations.of(42);
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computations.of(c1, c2).get().get();
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
        Pair<Integer, Boolean> r = Computations.of(c1, c2).get().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }

    <T> void testCaching(Supplier<IComputation<T>> computationSupplier) {
        // computationSupplier.get().get()
        // cache should have changed
        // computationSupplier.get().get()
        // cache should not have changed
    }
}
