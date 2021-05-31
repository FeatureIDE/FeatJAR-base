/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.tree;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.spldev.util.tree.structure.*;
import org.spldev.util.tree.visitor.*;

public class TreesTest {

	SimpleTree<String> emptyRoot, root1, root2, root3;
	List<SimpleTree<String>> preOrderList, postOrderList, innerList;

	@BeforeEach
	public void setUp() {
		emptyRoot = new SimpleTree<>("EmpytRoot");
		root1 = new SimpleTree<>("Root");
		SimpleTree<String> a = new SimpleTree<>("A");
		SimpleTree<String> b = new SimpleTree<>("B");
		SimpleTree<String> c = new SimpleTree<>("C");
		SimpleTree<String> b1 = new SimpleTree<>("B1");
		SimpleTree<String> b2 = new SimpleTree<>("B2");
		SimpleTree<String> b3 = new SimpleTree<>("B3");
		SimpleTree<String> b1a = new SimpleTree<>("B1A");
		SimpleTree<String> b1b = new SimpleTree<>("B1B");
		SimpleTree<String> b1c = new SimpleTree<>("B1C");
		SimpleTree<String> b3a = new SimpleTree<>("B3A");
		SimpleTree<String> b3b = new SimpleTree<>("B3B");
		SimpleTree<String> c1 = new SimpleTree<>("C1");
		SimpleTree<String> c1a = new SimpleTree<>("C1A");
		SimpleTree<String> c1b = new SimpleTree<>("C1B");
		SimpleTree<String> c1c = new SimpleTree<>("C1C");
		SimpleTree<String> c1d = new SimpleTree<>("C1D");

		root1.setChildren(Arrays.asList(a, b, c));
		b.setChildren(Arrays.asList(b1, b2, b3));
		c.setChildren(Arrays.asList(c1));
		b1.setChildren(Arrays.asList(b1a, b1b, b1c));
		b3.setChildren(Arrays.asList(b3a, b3b));
		c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));

		preOrderList = Arrays.asList(root1, a, b, b1, b1a, b1b, b1c, b2, b3, b3a, b3b, c, c1, c1a, c1b, c1c, c1d);
		postOrderList = Arrays.asList(a, b1a, b1b, b1c, b1, b2, b3a, b3b, b3, b, c1a, c1b, c1c, c1d, c1, c, root1);
		innerList = Arrays.asList(a, root1, b1a, b1, b1b, b1, b1c, b, b3a, b3, b3b, root1,
			c1a, c1, c1b, c1, c1c, c1, c1d, c);

		root2 = new SimpleTree<>("Root");
		a = new SimpleTree<>("A");
		b = new SimpleTree<>("B");
		c = new SimpleTree<>("C");
		b1 = new SimpleTree<>("B1");
		b2 = new SimpleTree<>("B2");
		b3 = new SimpleTree<>("B3");
		b1a = new SimpleTree<>("B1A");
		b1b = new SimpleTree<>("B1B");
		b1c = new SimpleTree<>("B1C");
		b3a = new SimpleTree<>("B3A");
		b3b = new SimpleTree<>("B3B");
		c1 = new SimpleTree<>("C1");
		c1a = new SimpleTree<>("C1A");
		c1b = new SimpleTree<>("C1B");
		c1c = new SimpleTree<>("C1C");
		c1d = new SimpleTree<>("C1D");

		root2.setChildren(Arrays.asList(a, b, c));
		b.setChildren(Arrays.asList(b1, b2, b3));
		c.setChildren(Arrays.asList(c1));
		b1.setChildren(Arrays.asList(b1a, b1b, b1c));
		b3.setChildren(Arrays.asList(b3a, b3b));
		c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));

		root3 = new SimpleTree<>("Root");
		a = new SimpleTree<>("A");
		b = new SimpleTree<>("B");
		c = new SimpleTree<>("C");
		b1 = new SimpleTree<>("B1");
		b2 = new SimpleTree<>("B2");
		b3 = new SimpleTree<>("B3");
		b1a = new SimpleTree<>("B1A");
		b1b = new SimpleTree<>("B1B");
		b1c = new SimpleTree<>("B1C");
		b3a = new SimpleTree<>("B3A");
		b3b = new SimpleTree<>("B3B");
		c1 = new SimpleTree<>("C1");
		c1a = new SimpleTree<>("C1A");
		c1b = new SimpleTree<>("C1B");
		c1c = new SimpleTree<>("C1C");
		c1d = new SimpleTree<>("C1D");

		root3.setChildren(Arrays.asList(a, b, c));
		b.setChildren(Arrays.asList(b3, b2, b1));
		c.setChildren(Arrays.asList(c1));
		b1.setChildren(Arrays.asList(b1a, b1b, b1c));
		b3.setChildren(Arrays.asList(b3a, b3b));
		c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));
	}

	@Test
	public void preOrderList() {
		assertEquals(preOrderList, Trees.getPreOrderList(root1));
		assertEquals(Collections.emptyList(), Trees.getPreOrderList(null));
	}

	@Test
	public void preOrderStream() {
		assertEquals(preOrderList, Trees.preOrderStream(root1).collect(Collectors.toList()));
		assertEquals(Collections.emptyList(), Trees.preOrderStream(null).collect(Collectors.toList()));
	}

	@Test
	public void postOrderStream() {
		assertEquals(postOrderList, Trees.postOrderStream(root1).collect(Collectors.toList()));
		assertEquals(Collections.emptyList(), Trees.postOrderStream(null).collect(Collectors.toList()));
	}

	@Test
	public void equals() {
		assertTrue(Trees.equals(null, null));
		assertTrue(Trees.equals(emptyRoot, emptyRoot));
		assertTrue(Trees.equals(root1, root1));
		assertTrue(Trees.equals(root1, root2));
		assertTrue(Trees.equals(root2, root1));
		assertFalse(Trees.equals(root1, root3));
		assertFalse(Trees.equals(root3, root1));
		assertFalse(Trees.equals(root1, emptyRoot));
		assertFalse(Trees.equals(root3, emptyRoot));
		assertFalse(Trees.equals(emptyRoot, root1));
		assertFalse(Trees.equals(emptyRoot, root3));
		assertFalse(Trees.equals(emptyRoot, null));
		assertFalse(Trees.equals(null, emptyRoot));
	}

	@Test
	public void cloneTree() {
		assertTrue(Trees.equals(emptyRoot, Trees.cloneTree(emptyRoot)));
		assertTrue(Trees.equals(root1, Trees.cloneTree(root1)));
	}

	@Test
	public void traversePrePost() {
		final LinkedHashSet<String> actualCallOrder = new LinkedHashSet<>();
		final LinkedHashSet<String> expectedCallOrder = new LinkedHashSet<>();
		expectedCallOrder.add("reset");
		expectedCallOrder.add("first");
		expectedCallOrder.add("last");
		expectedCallOrder.add("result");

		final ArrayList<SimpleTree<String>> preOrderCollect = new ArrayList<>();
		final ArrayList<SimpleTree<String>> postOrderCollect = new ArrayList<>();

		final Optional<Void> result = Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<String>>() {
			@Override
			public Void getResult() {
				final Void result = TreeVisitor.super.getResult();
				assertNull(result);
				actualCallOrder.add("result");
				return result;
			}

			@Override
			public void reset() {
				TreeVisitor.super.reset();
				actualCallOrder.add("reset");
			}

			@Override
			public VistorResult firstVisit(List<SimpleTree<String>> path) {
				assertEquals(VistorResult.Continue, TreeVisitor.super.firstVisit(path));
				actualCallOrder.add("first");
				preOrderCollect.add(TreeVisitor.getCurrentNode(path));
				return VistorResult.Continue;
			}

			@Override
			public VistorResult lastVisit(List<SimpleTree<String>> path) {
				assertEquals(VistorResult.Continue, TreeVisitor.super.lastVisit(path));
				actualCallOrder.add("last");
				postOrderCollect.add(TreeVisitor.getCurrentNode(path));
				return VistorResult.Continue;
			}
		});

		assertFalse(result.isPresent());
		assertEquals(preOrderList, preOrderCollect);
		assertEquals(postOrderList, postOrderCollect);
		assertEquals(expectedCallOrder, actualCallOrder);

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult firstVisit(List<SimpleTree<?>> path) {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult lastVisit(List<SimpleTree<?>> path) {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<?>>() {
			@Override
			public Void getResult() {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<?>>() {
			@Override
			public void reset() {
				throw new RuntimeException();
			}
		}));

		assertFalse(Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult firstVisit(List<SimpleTree<?>> path) {
				return VistorResult.Fail;
			}
		}).isPresent());

		assertFalse(Trees.traverse(root1, new TreeVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult lastVisit(List<SimpleTree<?>> path) {
				return VistorResult.Fail;
			}
		}).isPresent());
	}

	@Test
	public void traverseDfs() {
		final LinkedHashSet<String> actualCallOrder = new LinkedHashSet<>();
		final LinkedHashSet<String> expectedCallOrder = new LinkedHashSet<>();
		expectedCallOrder.add("reset");
		expectedCallOrder.add("first");
		expectedCallOrder.add("visit");
		expectedCallOrder.add("last");
		expectedCallOrder.add("result");

		final ArrayList<SimpleTree<String>> preOrderCollect = new ArrayList<>();
		final ArrayList<SimpleTree<String>> postOrderCollect = new ArrayList<>();
		final ArrayList<SimpleTree<String>> innerCollect = new ArrayList<>();

		final Optional<Void> result = Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<String>>() {
			@Override
			public Void getResult() {
				final Void result = DfsVisitor.super.getResult();
				assertNull(result);
				actualCallOrder.add("result");
				return result;
			}

			@Override
			public void reset() {
				DfsVisitor.super.reset();
				actualCallOrder.add("reset");
			}

			@Override
			public VistorResult firstVisit(List<SimpleTree<String>> path) {
				assertEquals(VistorResult.Continue, DfsVisitor.super.firstVisit(path));
				actualCallOrder.add("first");
				preOrderCollect.add(TreeVisitor.getCurrentNode(path));
				return VistorResult.Continue;
			}

			@Override
			public VistorResult visit(List<SimpleTree<String>> path) {
				assertEquals(VistorResult.Continue, DfsVisitor.super.visit(path));
				actualCallOrder.add("visit");
				innerCollect.add(TreeVisitor.getCurrentNode(path));
				return VistorResult.Continue;
			}

			@Override
			public VistorResult lastVisit(List<SimpleTree<String>> path) {
				assertEquals(VistorResult.Continue, DfsVisitor.super.lastVisit(path));
				actualCallOrder.add("last");
				postOrderCollect.add(TreeVisitor.getCurrentNode(path));
				return VistorResult.Continue;
			}
		});

		assertFalse(result.isPresent());
		assertEquals(preOrderList, preOrderCollect);
		assertEquals(postOrderList, postOrderCollect);
		assertEquals(expectedCallOrder, actualCallOrder);

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult firstVisit(List<SimpleTree<?>> path) {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult visit(List<SimpleTree<?>> path) {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult lastVisit(List<SimpleTree<?>> path) {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public Void getResult() {
				throw new RuntimeException();
			}
		}));

		assertThrows(RuntimeException.class, () -> Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public void reset() {
				throw new RuntimeException();
			}
		}));

		assertFalse(Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult firstVisit(List<SimpleTree<?>> path) {
				return VistorResult.Fail;
			}
		}).isPresent());

		assertFalse(Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult visit(List<SimpleTree<?>> path) {
				return VistorResult.Fail;
			}
		}).isPresent());

		assertFalse(Trees.traverse(root1, new DfsVisitor<Void, SimpleTree<?>>() {
			@Override
			public VistorResult lastVisit(List<SimpleTree<?>> path) {
				return VistorResult.Fail;
			}
		}).isPresent());
	}

}
