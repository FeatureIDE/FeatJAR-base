/*
 * Copyright (C) 2023 Sebastian Krieter
 *
 * This file is part of FeatJAR-util-evaluation.
 *
 * util-evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util-evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util-evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.base.env;

public class ProcessResult<R> {

    public static long INVALID_TIME = -1;

    private boolean terminatedInTime = false;
    private boolean noError = false;
    private long time = INVALID_TIME;
    private R result = null;

    public boolean isTerminatedInTime() {
        return terminatedInTime;
    }

    public void setTerminatedInTime(boolean terminatedInTime) {
        this.terminatedInTime = terminatedInTime;
    }

    public boolean isNoError() {
        return noError;
    }

    public void setNoError(boolean noError) {
        this.noError = noError;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }
}
