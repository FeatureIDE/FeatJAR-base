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
package de.featjar.base.log;

import de.featjar.base.computation.Progress;

/**
 * A message supplier that shows the current and total steps of a {@link Progress progress} object and its name.
 * The output format is: {@code current/total (name)}.
 *
 * @author Sebastian Krieter
 */
public final class AbsoluteProgressMessage implements IMessage {

    private final Progress progress;

    /**
     * Constructs a new message supplier with the given progress object.
     * @param progress the progress object
     */
    public AbsoluteProgressMessage(Progress progress) {
        this.progress = progress;
    }

    public String get() {
        return String.format("%d/%d (%s)", progress.getCurrentStep(), progress.getTotalSteps(), progress.getName());
    }
}
