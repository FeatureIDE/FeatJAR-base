package de.featjar.base.tree.structure;

import de.featjar.base.data.Result;

public interface IRootedTree<T extends IRootedTree<T>> extends ITree<T> {
    /**
     * {@return the parent node of this node, if any}
     */
    Result<T> getParent();

    /**
     * Sets the parent node of this node.
     *
     * @param newParent the new parent node
     */
    void setParent(T newParent);

    /**
     * {@return whether this node has a parent node}
     */
    default boolean hasParent() {
        return getParent().isPresent();
    }

    /**
     * {@return whether the given node is an ancestor of this node}
     *
     * @param node the node
     */
    default boolean isAncestor(IRootedTree<T> node) {
        Result<T> currentParent = getParent();
        while (currentParent.isPresent()) {
            if (node == currentParent.get()) {
                return true;
            }
            currentParent = currentParent.get().getParent();
        }
        return false;
    }

    /**
     * {@return the root node of this tree}
     */
    @SuppressWarnings("unchecked")
    default T getRoot() {
        T currentTree = (T) this;
        while (currentTree.getParent().isPresent()) {
            currentTree = currentTree.getParent().get();
        }
        return currentTree;
    }

    /**
     * {@return the index of this node in its parent's list of children, if any}
     */
    @SuppressWarnings("unchecked")
    default Result<Integer> getIndex() {
        return getParent().flatMap(parent -> parent.getChildIndex((T) this));
    }
}
