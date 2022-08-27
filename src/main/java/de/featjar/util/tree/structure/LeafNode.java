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
 * See <https://github.com/FeatureIDE/FeatJAR-util> for further information.
 */
package de.featjar.util.tree.structure;

import java.util.Collections;
import java.util.List;

/**
 * A leaf node of a tree.
 * A leaf does not have any children.
 * Nonetheless, it captures a children type, such that it can be added as a child to a non-leaf node.
 *
 * @param <T> type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 */
public abstract class LeafNode<T extends Traversable<T>> implements Traversable<T> {

    /**
     * {@return an empty list of children}
     */
    @Override
    public List<? extends T> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Does nothing.
     *
     * @param children ignored
     */
    @Override
    public void setChildren(List<? extends T> children) {}
}
