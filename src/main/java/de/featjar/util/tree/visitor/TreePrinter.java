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
package de.featjar.util.tree.visitor;

import de.featjar.util.tree.structure.Traversable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TreePrinter implements TreeVisitor<String, Traversable<?>> {

    private String indentation = "  ";

    private StringBuilder treeStringBuilder = new StringBuilder();
    private Predicate<Traversable<?>> filter = null;
    private Function<Traversable<?>, String> toStringFunction = Object::toString;

    @Override
    public void reset() {
        treeStringBuilder.delete(0, treeStringBuilder.length());
    }

    @Override
    public Optional<String> getResult() {
        return Optional.of(treeStringBuilder.toString());
    }

    public String getIndentation() {
        return indentation;
    }

    public void setIndentation(String indentation) {
        this.indentation = indentation;
    }

    @Override
    public TraversalAction firstVisit(List<Traversable<?>> path) {
        final Traversable<?> currentNode = TreeVisitor.getCurrentNode(path);
        if ((filter == null) || filter.test(currentNode)) {
            try {
                for (int i = 1; i < path.size(); i++) {
                    treeStringBuilder.append(indentation);
                }
                treeStringBuilder.append(toStringFunction.apply(currentNode));
                treeStringBuilder.append('\n');
            } catch (final Exception e) {
                return TraversalAction.SKIP_ALL;
            }
        }
        return TraversalAction.CONTINUE;
    }

    public void setFilter(Predicate<Traversable<?>> filter) {
        this.filter = filter;
    }

    public void setToStringFunction(Function<Traversable<?>, String> toStringFunction) {
        this.toStringFunction = toStringFunction;
    }
}
