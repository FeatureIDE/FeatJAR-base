/*
 * Copyright (C) 2023 Sebastian Krieter, Elias Kuiter
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
package de.featjar.util.tree.structure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.util.tree.Trees;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleTreeTest {

    SimpleTree<String> emptyRoot, root, childA, childB, childC, childD, childE, childF;
    List<SimpleTree<String>> firstChildren, secondChildren, thirdChildren, fourthChildren, fifthChildren;

    @BeforeEach
    public void setUp() {
        emptyRoot = new SimpleTree<>("EmptyRoot");
        root = new SimpleTree<>("Root");
        childA = new SimpleTree<>("A");
        childB = new SimpleTree<>("B");
        childC = new SimpleTree<>("C");
        childD = new SimpleTree<>("D");
        childE = new SimpleTree<>("E");
        childF = new SimpleTree<>("F");

        firstChildren = Arrays.asList(childA);
        secondChildren = Arrays.asList(childB, childC);
        thirdChildren = Arrays.asList(childA, childB, childC);
        fourthChildren = Arrays.asList(childD);
        fifthChildren = Arrays.asList(childE, childF);

        root.setChildren(thirdChildren);
    }

    @Test
    public void createTreeWithoutData() {
        final String treeToString = "SimpleTree [null]";

        final SimpleTree<String> newRoot = new SimpleTree<>();
        assertNull(newRoot.getData());
        assertFalse(newRoot.hasChildren());
        assertNotNull(newRoot.getChildren());
        assertTrue(newRoot.getChildren().isEmpty());
        assertEquals(treeToString, newRoot.toString());
    }

    @Test
    public void createTreeWithData() {
        final String newData = "NewRoot";
        final String treeToString = "SimpleTree [" + newData + "]";

        final SimpleTree<String> newRoot = new SimpleTree<>(newData);
        assertEquals(newData, newRoot.getData());
        assertFalse(newRoot.hasChildren());
        assertNotNull(newRoot.getChildren());
        assertTrue(newRoot.getChildren().isEmpty());
        assertEquals(treeToString, newRoot.toString());
    }

    @Test
    public void setData() {
        final String exampleData = "Example";
        final String treeToString = "SimpleTree [" + exampleData + "]";

        final SimpleTree<String> newRoot = new SimpleTree<>();
        newRoot.setData(exampleData);
        assertEquals(exampleData, newRoot.getData());
        assertEquals(treeToString, newRoot.toString());
    }

    @Test
    public void replaceData() {
        final String exampleData = "Example";
        final String treeToString = "SimpleTree [" + exampleData + "]";

        root.setData(exampleData);
        assertEquals(exampleData, root.getData());
        assertEquals(treeToString, root.toString());
    }

    @Test
    public void setChildrenOnce() {
        emptyRoot.setChildren(firstChildren);
        assertTrue(emptyRoot.hasChildren());
        assertEquals(firstChildren, emptyRoot.getChildren());
    }

    @Test
    public void setChildrenTwice() {
        emptyRoot.setChildren(firstChildren);
        emptyRoot.setChildren(secondChildren);
        assertTrue(emptyRoot.hasChildren());
        assertEquals(secondChildren, emptyRoot.getChildren());
    }

    @Test
    public void setChildrenThrice() {
        emptyRoot.setChildren(firstChildren);
        emptyRoot.setChildren(secondChildren);
        emptyRoot.setChildren(thirdChildren);
        assertTrue(emptyRoot.hasChildren());
        assertEquals(thirdChildren, emptyRoot.getChildren());
    }

    @Test
    public void modifyChildrenIllegally() {
        assertThrows(UnsupportedOperationException.class, () -> {
            emptyRoot.getChildren().add(null);
        });
        emptyRoot.setChildren(firstChildren);
        assertThrows(UnsupportedOperationException.class, () -> {
            emptyRoot.getChildren().add(null);
        });
    }

    @Test
    public void removeChildren() {
        root.setChildren(Collections.emptyList());
        assertFalse(root.hasChildren());
        assertEquals(Collections.emptyList(), root.getChildren());
    }

    @Test
    public void removeChildrenIllegally() {
        assertThrows(NullPointerException.class, () -> {
            emptyRoot.setChildren(null);
        });
        emptyRoot.setChildren(firstChildren);
        assertThrows(NullPointerException.class, () -> {
            emptyRoot.setChildren(null);
        });
    }

    @Test
    public void cloneEmptyTree() {
        final SimpleTree<String> clonedRoot = Trees.cloneTree(emptyRoot);

        assertEquals(emptyRoot.getClass(), clonedRoot.getClass());
        assertEquals(0, emptyRoot.getChildren().size());
        assertEquals(0, clonedRoot.getChildren().size());
    }

    @Test
    public void cloneTree() {
        final SimpleTree<String> clonedRoot = Trees.cloneTree(root);

        assertEquals(root.getData(), clonedRoot.getData());
        assertEquals(thirdChildren.size(), root.getChildren().size());
        assertEquals(thirdChildren.size(), clonedRoot.getChildren().size());
        final Iterator<? extends SimpleTree<String>> iterator =
                root.getChildren().iterator();
        final Iterator<? extends SimpleTree<String>> clonedIterator =
                clonedRoot.getChildren().iterator();
        while (iterator.hasNext()) {
            assertEquals(iterator.next().getData(), clonedIterator.next().getData());
        }
    }

    @Test
    public void replaceChildrenInEmptyTree() {
        final List<?> children = emptyRoot.children;

        emptyRoot.mapChildren(oldChild -> new SimpleTree<>());

        assertTrue(children == emptyRoot.children);
        assertTrue(emptyRoot.getChildren().isEmpty());
    }

    @Test
    public void replaceAllChildrenWithNull() {
        final List<?> children = root.children;
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.mapChildren(oldChild -> null);

        assertTrue(children == root.children);
        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildren().size());
        assertEquals(childA, iterator.next());
        assertEquals(childB, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceAllChildrenWithThemselves() {
        final List<?> children = root.children;
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.mapChildren(oldChild -> oldChild);

        assertTrue(children == root.children);
        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildren().size());
        assertEquals(childA, iterator.next());
        assertEquals(childB, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceAllChildrenWithOneOtherChild() {
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.mapChildren(oldChild -> childD);

        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildren().size());
        assertEquals(childD, iterator.next());
        assertEquals(childD, iterator.next());
        assertEquals(childD, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceOnlyOneChildWithAnother() {
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.mapChildren(oldChild -> oldChild == childB ? childD : null);

        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildren().size());
        assertEquals(childA, iterator.next());
        assertEquals(childD, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceChildrenInEmptyTreeWithList() {
        final List<?> children = emptyRoot.children;

        emptyRoot.flatMapChildren(oldChild -> Arrays.asList(new SimpleTree<>()));

        assertTrue(children == emptyRoot.children);
        assertTrue(emptyRoot.getChildren().isEmpty());
    }

    @Test
    public void replaceAllChildrenWithNullList() {
        final List<?> children = root.children;
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.flatMapChildren(oldChild -> null);

        assertTrue(children == root.children);
        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildren().size());
        assertEquals(childA, iterator.next());
        assertEquals(childB, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceAllChildrenWithEmptyList() {
        final List<?> children = root.children;
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.flatMapChildren(oldChild -> Collections.emptyList());

        assertTrue(children == root.children);
        assertThrows(ConcurrentModificationException.class, () -> {
            failFastIterator.next();
        });
        assertTrue(emptyRoot.getChildren().isEmpty());
    }

    @Test
    public void replaceAllChildrenWithOtherLists() {
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.flatMapChildren(oldChild -> oldChild == childB ? fifthChildren : fourthChildren);

        assertThrows(ConcurrentModificationException.class, () -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(
                (2 * fourthChildren.size()) + fifthChildren.size(),
                root.getChildren().size());
        assertEquals(childD, iterator.next());
        assertEquals(childE, iterator.next());
        assertEquals(childF, iterator.next());
        assertEquals(childD, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceChildrenUsingAnIllegalReplacer() {
        assertThrows(NullPointerException.class, () -> {
            root.mapChildren(null);
        });
        assertThrows(NullPointerException.class, () -> {
            root.flatMapChildren(null);
        });
    }
}
