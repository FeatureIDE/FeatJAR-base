/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
package de.featjar.base.computation;

import de.featjar.base.data.Range;
import java.util.function.Supplier;

/**
 * Tracks progress of an asynchronous computation.
 * Attached to a {@link FutureResult}.
 *
 * @author Elias Kuiter
 */
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
        return range.getLowerBound();
    }

    /**
     * Sets the progress's current step.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(int currentStep) {
        int totalSteps = getTotalSteps();
        if (totalSteps >= 0 && totalSteps < currentStep) range.setUpperBound(currentStep);
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
    public int getTotalSteps() {
        int upperBound = range.getUpperBound();
        return upperBound != Range.OPEN ? upperBound : -1;
    }

    /**
     * Sets the progress's total number of steps.
     * Can be accurate or a preliminary estimate.
     *
     * @param totalSteps the total steps
     */
    public void setTotalSteps(Integer totalSteps) {
        if (totalSteps != null && totalSteps == 0) throw new IllegalArgumentException(String.valueOf(totalSteps));
        if (totalSteps == null) range.setUpperBound(Range.OPEN);
        else range.setUpperBound(Math.max(getCurrentStep(), totalSteps));
    }

    /**
     * {@return this progress' percentage (i.e., the current step divided by the total number of steps)}
     */
    public Double get() {
        int currentStep = getCurrentStep();
        if (currentStep == 0) return 0.0;
        int totalSteps = getTotalSteps();
        if (totalSteps == -1) return 0.5;
        return (double) currentStep / totalSteps;
    }

    @Override
    public String toString() {
        return range.toString();
    }

    public static class Null extends Progress {
        public static final Null NULL = new Null();

        @Override
        public void setCurrentStep(int currentStep) {}

        @Override
        public void incrementCurrentStep() {}

        @Override
        public void addCurrentSteps(int steps) {}

        @Override
        public void setTotalSteps(Integer totalSteps) {}
    }
}
