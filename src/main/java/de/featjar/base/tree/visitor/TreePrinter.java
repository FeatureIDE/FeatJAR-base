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
package de.featjar.base.tree.visitor;

import de.featjar.base.tree.structure.Traversable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Prints a tree as a string.
 * Useful for debugging.
 * Implemented as a preorder traversal.
 *
 * @author Sebastian Krieter
 */
public class TreePrinter implements TreeVisitor<String, Traversable<?>> {
    private final StringBuilder treeStringBuilder = new StringBuilder();
    private String indentation = "  ";
    private Predicate<Traversable<?>> filter = null;
    private Function<Traversable<?>, String> toStringFunction = Object::toString;

    public String getIndentation() {
        return indentation;
    }

    public Predicate<Traversable<?>> getFilter() {
        return filter;
    }

    public Function<Traversable<?>, String> getToStringFunction() {
        return toStringFunction;
    }

    public void setIndentation(String indentation) {
        this.indentation = indentation;
    }

    public void setFilter(Predicate<Traversable<?>> filter) {
        this.filter = filter;
    }

    public void setToStringFunction(Function<Traversable<?>, String> toStringFunction) {
        this.toStringFunction = toStringFunction;
    }

    @Override
    public void reset() {
        treeStringBuilder.delete(0, treeStringBuilder.length());
    }

    @Override
    public Optional<String> getResult() {
        return Optional.of(treeStringBuilder.toString());
    }

    @Override
    public TraversalAction firstVisit(List<Traversable<?>> path) {
        final Traversable<?> currentNode = getCurrentNode(path);
        if ((filter == null) || filter.test(currentNode)) {
            try {
                treeStringBuilder.append(String.valueOf(indentation).repeat(Math.max(0, path.size() - 1)));
                treeStringBuilder.append(toStringFunction.apply(currentNode));
                treeStringBuilder.append('\n');
            } catch (final Exception e) {
                return TraversalAction.SKIP_ALL;
            }
        }
        return TraversalAction.CONTINUE;
    }
}
