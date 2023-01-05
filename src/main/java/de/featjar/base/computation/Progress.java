package de.featjar.base.computation;


import de.featjar.base.FeatJAR;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;

import java.util.function.Supplier;

public class Progress implements Supplier<Double> {
    protected final Range range;

    public Progress() {
        this.range = Range.of(0, null);
    }

    /**
     * {@return the progress's current step}
     */
    public int getCurrentStep() {
        return range.getLowerBound().get();
    }

    /**
     * Sets the progress's total number of steps.
     * Can be accurate or a preliminary estimate.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(int currentStep) {
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
        return range.getLargerBound();
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
        range.setUpperBound(totalSteps);
    }

    /**
     * {@return the progress percentage (i.e., the current step divided by the total number of steps)}
     */
    public Double get() {
        if (getCurrentStep() == 0)
            return 0.0;
        if (getTotalSteps().isEmpty())
            return 0.5;
        return (double) getCurrentStep() / getTotalSteps().get();
    }

    /**
     * Logs the progress percentage.
     */
    public void log() {
        FeatJAR.log().progress(((Math.floor(get() * 1000)) / 10.0) + "%");
    }
}
