package de.featjar.base.data;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A range of integers limited by a lower and upper bound.
 * Both bounds may be open, in which case they are not checked.
 * TODO: javadoc is missing
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

    public Result<Integer> getLowerBound() {
        return Result.ofNullable(lowerBound);
    }

    public boolean isLowerBoundOpen() {
        return lowerBound == null;
    }

    public void setLowerBound(Integer lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Result<Integer> getUpperBound() {
        return Result.ofNullable(upperBound);
    }

    public boolean isUpperBoundOpen() {
        return upperBound == null;
    }

    public void setUpperBound(Integer upperBound) {
        this.upperBound = upperBound;
    }

    public boolean isOpen() {
        return isLowerBoundOpen() && isUpperBoundOpen();
    }

    public Result<Integer> getSmallerBound() {
        return Result.ofNullable(getLowerBound().orElseGet(() -> getUpperBound().orElse(null)));
    }

    public Result<Integer> getLargerBound() {
        return Result.ofNullable(getUpperBound().orElseGet(() -> getLowerBound().orElse(null)));
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

    /**
     * {@return a finite integer stream for this range}
     */
    public Result<IntStream> stream() {
        return lowerBound != null && upperBound != null
                ? Result.of(IntStream.rangeClosed(lowerBound, upperBound))
                : Result.empty();
    }

    @Override
    public Boolean apply(Integer integer) {
        return test(integer);
    }

    @Override
    public String toString() {
        return String.format("Range[%d, %d]", lowerBound, upperBound);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return Objects.equals(lowerBound, range.lowerBound) && Objects.equals(upperBound, range.upperBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }
}