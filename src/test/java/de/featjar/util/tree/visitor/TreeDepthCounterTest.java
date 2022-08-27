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

import de.featjar.util.tree.Trees;
import de.featjar.util.tree.structure.LabeledTree;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreeDepthCounterTest {
    @Test
    public void bla() {
        LabeledTree<String> root1 = new LabeledTree<>("Root");
        LabeledTree<String> a = new LabeledTree<>("A");
        LabeledTree<String> b = new LabeledTree<>("B");
        LabeledTree<String> c = new LabeledTree<>("C");
        LabeledTree<String> b1 = new LabeledTree<>("B1");
        LabeledTree<String> b2 = new LabeledTree<>("B2");
        LabeledTree<String> b3 = new LabeledTree<>("B3");
        LabeledTree<String> b1a = new LabeledTree<>("B1A");
        LabeledTree<String> b1b = new LabeledTree<>("B1B");
        LabeledTree<String> b1c = new LabeledTree<>("B1C");
        LabeledTree<String> b3a = new LabeledTree<>("B3A");
        LabeledTree<String> b3b = new LabeledTree<>("B3B");
        LabeledTree<String> c1 = new LabeledTree<>("C1");
        LabeledTree<String> c1a = new LabeledTree<>("C1A");
        LabeledTree<String> c1b = new LabeledTree<>("C1B");
        LabeledTree<String> c1c = new LabeledTree<>("C1C");
        LabeledTree<String> c1d = new LabeledTree<>("C1D");
        root1.setChildren(Arrays.asList(a, b, c));
        b.setChildren(Arrays.asList(b1, b2, b3));
        c.setChildren(Arrays.asList(c1));
        b1.setChildren(Arrays.asList(b1a, b1b, b1c));
        b3.setChildren(Arrays.asList(b3a, b3b));
        c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));
        root1.traverse(new InOrderTreeVisitor<Void, LabeledTree<String>>() {

            @Override
            public TraversalAction firstVisit(List<LabeledTree<String>> path) {
                //System.out.println(getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }

            @Override
            public TraversalAction visit(List<LabeledTree<String>> path) {
                System.out.println(getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }

            @Override
            public TraversalAction lastVisit(List<LabeledTree<String>> path) {
                //System.out.println(getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }
        });

        System.out.println(root1.preOrderStream().count());
    }
}
