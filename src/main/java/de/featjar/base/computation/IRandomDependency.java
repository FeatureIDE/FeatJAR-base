package de.featjar.base.computation;

import java.util.Random;

/**
 * An analysis that may need to generate pseudorandom numbers.
 * Assumes that the implementing class can be cast to {@link IComputation}.
 */
public interface IRandomDependency {
    /**
     * The default seed for the pseudorandom number generator returned by {@link #getRandom()}, if not specified otherwise.
     */
    long DEFAULT_RANDOM_SEED = 0;// todo: needed?

    /**
     * {@return the random dependency of this computation}
     */
    Dependency<Random> getRandomDependency();

    /**
     * {@return the pseudorandom number generator computation of this computation}
     */
    default IComputation<Random> getRandom() {
        return getRandomDependency().get((IComputation<?>) this);
    }

    /**
     * Sets the pseudorandom number generator computation of this computation.
     *
     * @param random the pseudorandom number generator computation
     */
    default void setRandom(IComputation<Random> random) {
        getRandomDependency().set((IComputation<?>) this, random);
    }

    /**
     * Sets the pseudorandom number generator computation of this computation based on a given seed.
     * Uses Java's default pseudorandom number generator implementation.
     * If no seed is given, uses the default seed. (todo: not currently true)
     *
     * @param seed the seed
     */
    default void setRandomSeed(IComputation<Long> seed) {
        setRandom(seed.mapResult(IRandomDependency.class, "setRandom", Random::new));
    }
}
