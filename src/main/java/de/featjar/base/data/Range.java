package de.featjar.base.data;

import java.util.Optional;
import java.util.function.Function;

/**
 * A range of integers limited by a lower and upper bound.
 * Both bounds may be open, in which case they are not checked.
 *
 * @author Elias Kuiter
 */
public class Range implements Function<Integer, Boolean> {
    protected Integer lowerBound;
    protected Integer upperBound;

    protected Range(Integer lowerBound, Integer upperBound) {
        if ((lowerBound != null && lowerBound < 0) ||
                (upperBound != null && upperBound < 0) ||
                (lowerBound != null && upperBound != null && lowerBound > upperBound)) {
            throw new IllegalArgumentException(String.format("invalid bounds %d, %d", lowerBound, upperBound));
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public static Range of(Integer lowerBound, Integer upperBound) {
        return new Range(lowerBound, upperBound);
    }

    public static Range open() {
        return new Range(null, null);
    }

    public static Range atLeast(Integer minimum) {
        return new Range(minimum, null);
    }

    public static Range atMost(Integer maximum) {
        return new Range(null, maximum);
    }

    public static Range exactly(Integer bound) {
        return new Range(bound, bound);
    }

    public Optional<Integer> getLowerBound() {
        return Optional.ofNullable(lowerBound);
    }

    public void setLowerBound(Integer lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Optional<Integer> getUpperBound() {
        return Optional.ofNullable(upperBound);
    }

    public void setUpperBound(Integer upperBound) {
        this.upperBound = upperBound;
    }

    public Optional<Integer> getSmallerBound() {
        return Optional.ofNullable(getLowerBound().orElseGet(() -> getUpperBound().orElse(null)));
    }

    public Optional<Integer> getLargerBound() {
        return Optional.ofNullable(getUpperBound().orElseGet(() -> getLowerBound().orElse(null)));
    }

    public boolean testLowerBound(int integer) {
        return getLowerBound().map(lowerBound -> lowerBound <= integer).orElse(true);
    }

    public boolean testUpperBound(int integer) {
        return getUpperBound().map(upperBound -> integer <= upperBound).orElse(true);
    }

    public boolean test(int integer) {
        return testLowerBound(integer) && testUpperBound(integer);
    }

    @Override
    public Boolean apply(Integer integer) {
        return test(integer);
    }

    @Override
    public String toString() {
        return String.format("Range[%d, %d]", lowerBound, upperBound);
    }
}