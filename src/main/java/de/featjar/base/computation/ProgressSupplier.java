/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

import de.featjar.base.log.ActivityMessage;
import de.featjar.base.log.Log;
import de.featjar.base.log.PassedTimeMessage;
import de.featjar.base.log.ProgressMessage;
import de.featjar.base.log.ProgressThread;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Creates a progress and informs the caller of a computation.
 *
 * @author Sebastian Krieter
 */
public class ProgressSupplier implements Supplier<Progress> {

    private Consumer<Progress> callback;

    public static final class LogThreadCallBack implements Consumer<Progress> {

        private ProgressThread currentThread;

        @Override
        public void accept(Progress progress) {
            if (currentThread != null) {
                currentThread.shutdown();
            }
            currentThread = Log.startProgressThread(
                    progress, 1000, new ActivityMessage(), new PassedTimeMessage(), new ProgressMessage(progress));
        }
    }

    public ProgressSupplier() {
        this.callback = new LogThreadCallBack();
    }

    public ProgressSupplier(Consumer<Progress> callback) {
        this.callback = callback;
    }

    public Progress get() {
        Progress progress = new Progress();
        if (callback != null) {
            callback.accept(progress);
        }
        return progress;
    }

    public void setCallback(Consumer<Progress> callback) {
        this.callback = callback;
    }
}
