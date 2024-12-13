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
package de.featjar.base.log;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Progress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Sebastian Krieter
 */
public final class ProgressBar {

    public static class ProgressThread extends Thread implements AutoCloseable {
        private final List<Supplier<String>> messageSuppliers;
        private final int refreshRate;

        private boolean running = true;

        private ProgressThread(List<Supplier<String>> messageSuppliers, int refreshRate) {
            super();
            this.messageSuppliers = messageSuppliers != null ? new ArrayList<>(messageSuppliers) : List.of();
            this.refreshRate = refreshRate;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    StringBuilder sb = new StringBuilder();
                    for (Supplier<String> m : messageSuppliers) {
                        sb.append(m.get());
                        sb.append(" | ");
                    }
                    if (sb.length() > 0) {
                        sb.delete(sb.length() - 3, sb.length());
                    }
                    FeatJAR.log().progress(sb.toString());
                    Thread.sleep(refreshRate);
                }
            } catch (InterruptedException e) {
                FeatJAR.log().error(e);
            }
        }

        public void shutdown() {
            running = false;
        }

        public int getRefreshRate() {
            return refreshRate;
        }

        @Override
        public void close() throws Exception {
            shutdown();
        }
    }

    public static ProgressThread startProgressThread(Progress progress) {
        return startProgressThread(
                1000,
                new ActivityMessage(),
                new ProgressMessage(progress),
                new PassedTimeMessage(),
                new UsedMemoryMessage());
    }

    @SafeVarargs
    public static ProgressThread startProgressThread(Supplier<String>... messageSuppliers) {
        return startProgressThread(1000, messageSuppliers);
    }

    @SafeVarargs
    public static ProgressThread startProgressThread(int refreshRate, Supplier<String>... messageSuppliers) {
        ProgressThread thread = new ProgressThread(Arrays.asList(messageSuppliers), refreshRate);
        thread.start();
        return thread;
    }
}
