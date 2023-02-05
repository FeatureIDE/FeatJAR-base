/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.logging;

/**
 * Prepends log output with an appropriate number of tab symbols.
 */
public class TabFormatter implements Formatter {

    private int tabLevel = 0;
    private String tabSymbol = "  ";

    public void incTabLevel() {
        tabLevel++;
    }

    public void decTabLevel() {
        tabLevel--;
    }

    public int getTabLevel() {
        return tabLevel;
    }

    public void setTabLevel(int tabLevel) {
        this.tabLevel = tabLevel;
    }

    public String getTabSymbol() {
        return tabSymbol;
    }

    public void setTabSymbol(String tabSymbol) {
        this.tabSymbol = tabSymbol;
    }

    @Override
    public void format(StringBuilder message) {
        for (int i = 0; i < tabLevel; i++) {
            message.append(tabSymbol);
        }
    }
}
