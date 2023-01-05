package de.featjar.base.computation;


import de.featjar.base.FeatJAR;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;

import java.util.function.Supplier;

public class Progress implements Supplier<Double> {
    protected final Range range;

    public Progress() {
        this(Range.atLeast(0));
    }

    protected Progress(Range range) {
        this.range = range;
    }

    public static Progress completed(int steps) {
        return new Progress(Range.exactly(steps == 0 ? 1 : steps));
    }

    /**
     * {@return the progress's current step}
     */
    public int getCurrentStep() {
        return range.getLowerBound().get();
    }

    /**
     * Sets the progress's current step.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(int currentStep) {
        if (getTotalSteps().isPresent() && getTotalSteps().get() < currentStep)
            range.setUpperBound(currentStep);
        range.setLowerBound(currentStep);
    }

    /**
     * Increases the progress's current step by one and checks for cancellation.
     */
    public void incrementCurrentStep() {
        addCurrentSteps(1);
    }

    /**
     * Increases the progress's current step by an amount and checks for cancellation.
     *
     * @param steps the steps
     */
    public void addCurrentSteps(int steps) {
        setCurrentStep(getCurrentStep() + steps);
    }

    /**
     * {@return the progress's total number of steps}
     */
    public Result<Integer> getTotalSteps() {
        return range.getUpperBound();
    }

    /**
     * Sets the progress's total number of steps.
     * Can be accurate or a preliminary estimate.
     *
     * @param totalSteps the total steps
     */
    public void setTotalSteps(Integer totalSteps) {
        if (totalSteps != null && totalSteps == 0)
            throw new IllegalArgumentException();
        if (totalSteps == null)
            range.setUpperBound(null);
        else
            range.setUpperBound(Math.max(getCurrentStep(), totalSteps));
    }

    /**
     * {@return this progress' percentage (i.e., the current step divided by the total number of steps)}
     */
    public Double get() {
        if (getCurrentStep() == 0)
            return 0.0;
        if (getTotalSteps().isEmpty())
            return 0.5;
        return (double) getCurrentStep() / getTotalSteps().get();
    }

    @Override
    public String toString() {
        return range.toString();
    }
}
