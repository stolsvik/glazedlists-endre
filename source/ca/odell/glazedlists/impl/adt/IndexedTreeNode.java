/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.adt;

// for specifying a sorting order
import java.util.*;

/**
 * A tree node that can be accessed either in sorted order or by
 * index.
 *
 * <p>This tree-node uses AVL-Trees to ensure that access is always
 * logarithmic in terms of the size of the tree. AVL Trees use
 * rotations (single and double) when the height of a pair of
 * subtrees do not match in order to guarantee a bound on the
 * difference in their height. This bound can be shown to provide
 * an overall bound on the access time on the tree.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class IndexedTreeNode {

    /** the parent node, used to delete from leaf up */
    private IndexedTreeNode parent;

    /** the left and right child nodes */
    private IndexedTreeNode left = null;
    private IndexedTreeNode right = null;

    /** the size of the left and right subtrees */
    private int leftSize = 0;
    private int rightSize = 0;

    /** the height of this subtree */
    private int height = 0;

    /** the value of this node, assuming it is a leaf */
    private Object value;

    /**
     * Creates a new IndexedTreeNode with the specified parent node.
     */
    IndexedTreeNode(IndexedTreeNode parent) {
        this.parent = parent;
    }

    /**
     * Gets the value of this tree node.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this tree node. <strong>Warning:</strong> changing
     * the value of a node in a sorted tree may cause sorting to break
     * miserably.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the object with the specified index in the tree.
     */
    IndexedTreeNode getNodeWithIndex(int index) {
        // recurse to the left
        if(index < leftSize) {
            return left.getNodeWithIndex(index);

        // recurse on the right side
        } else if(index > leftSize) {
            return right.getNodeWithIndex(index - (leftSize + 1));

        // return this node's root
        } else {
            return this;
        }
    }

    /**
     * Gets the object with the specified value in the tree.
     */
    IndexedTreeNode getNodeByValue(Comparator comparator, Object searchValue) {
        int sortSide = comparator.compare(searchValue, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return null;
            return left.getNodeByValue(comparator, searchValue);

        // if it sorts on the right side, search there
        } else if(sortSide > 0) {
            if(right == null) return null;
            return right.getNodeByValue(comparator, searchValue);

        // if it equals this node, return this
        } else {
            return this;
        }
    }

    /**
     * Retrieves the size of this subtree.
     */
    int size() {
        return leftSize + 1 + rightSize;
    }

    /**
     * Creates an iterator starting at this node and continuing from here
     * through the tree.
     */
    Iterator iterator() {
        return new IndexedTreeIterator(this);
    }

    /**
     * Retrieves the height of this subtree.
     */
    int height() {
        return height;
    }

    /**
     * Retrieves the subtree node with the largest value.
     */
    IndexedTreeNode getLargestChildNode() {
        if(rightSize > 0) return right.getLargestChildNode();
        else return this;
    }
    /**
     * Retrieves the subtree node with the smallest value.
     */
    IndexedTreeNode getSmallestChildNode() {
        if(leftSize > 0) return left.getSmallestChildNode();
        else return this;
    }


    /**
     * Gets the index of the current node, based on a recurrsive
     * path up the tree.
     */
    public int getIndex() {
        return getIndex(null);
    }
    private int getIndex(IndexedTreeNode child) {
        // if the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getIndex(this);
            return 0;

        // if there is no child, get the index of the current node
        } else if(child == null) {
            if(parent != null) return parent.getIndex(this) + leftSize;
            return leftSize;

        // if the child is on the right, return the index recursively
        } else if(child == right) {
            if(parent != null) return parent.getIndex(this) + leftSize + 1;
            return leftSize + 1;
        }
        // if no child is found, we have a problem
        throw new IndexOutOfBoundsException(this + " cannot get the index of a subtree that does not exist on this node!");
    }


    /**
     * Inserts the specified object into the tree in sorted order.
     *
     * @return the IndexedTreeNode node where the object was inserted. This
     *      node can be used to call the deleteUp() method, which will
     *      delete the node from it's parent tree. It is also possible to
     *      use the getIndex() method on the node to discover what the sorted
     *      index of the value is.
     */
    IndexedTreeNode insert(IndexedTree host, Object inserted) {
        // if this is a newborn leaf, the value can be null as long as there are no children
        if(value == null) {
            // can't insert into non-leaf node with null value
            assert(leftSize == 0 && rightSize == 0);
            value = inserted;
            ensureAVL(host);
            return this;

        // if it sorts on the left side, insert there
        } else if(host.getComparator().compare(inserted, value) < 0) {
            if(left == null) left = new IndexedTreeNode(this);
            leftSize++;
            return left.insert(host, inserted);

        // if it doesn't sort on the left side, insert on the right
        } else {
            if(right == null) right = new IndexedTreeNode(this);
            rightSize++;
            return right.insert(host, inserted);
        }
    }
    /**
     * Inserts the specified object into the tree with the specified index.
     *
     * @return the IndexedTreeNode node where the object was inserted. This
     *      node can be used to call the deleteUp() method, which will
     *      delete the node from it's parent tree. It is also possible to call
     *      the getIndex() method on the node. As new nodes are inserted, the
     *      index will shift. The getIndex() method can be used to get the
     *      current index of the node at any time.
     */
    IndexedTreeNode insert(IndexedTree host, int index, Object inserted) {
        // if this node has no value, insert as a leaf
        if(index == 0 && value == null) {
            // can't insert into non-leaf node with null value
            assert(leftSize == 0 && rightSize == 0);
            value = inserted;
            ensureAVL(host);
            return this;

        // if the index is on the left side, insert there
        } else if(index <= leftSize) {
            if(left == null) left = new IndexedTreeNode(this);
            leftSize++;
            return left.insert(host, index, inserted);

        // if the index is not on the left side, insert on the right
        } else {
            if(right == null) right = new IndexedTreeNode(this);
            rightSize++;
            return right.insert(host, index - leftSize - 1, inserted);
        }
    }


    /**
     * Unlinks this node from the sorted tree. This may cause the tree to
     * rotate nodes using AVL rotations.
     */
    public void removeFromTree(IndexedTree host) {
        // if this node has no value, we have a problem!
        assert(value != null);
        // if this is a leaf, we can delete it outright
        if(leftSize == 0 && rightSize == 0) {
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, null);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(null);
            }
            // clear the parent
            parent = null;
        // if this node has only a left child, we can replace this with that child
        } else if(leftSize > 0 && rightSize == 0) {
            // update the left child
            left.parent = parent;
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, left);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(left);
            }
        // if this node has only a right child, we can replace this with that child
        } else if(leftSize == 0 && rightSize > 0) {
            // update the right child
            right.parent = parent;
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, right);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(right);
            }
        // if this node has two children, replace this node with the best of the biggest
        } else {
            IndexedTreeNode middle = null;
            // if the left side is larger, use a left side node
            if(leftSize > rightSize) {
                middle = left.getLargestChildNode();
            // otherwise use a right side node
            } else {
                middle = right.getSmallestChildNode();
            }
            middle.removeFromTree(host);
            // cannot have new middle with leaves
            assert(middle.leftSize == 0 && middle.rightSize == 0);
            // update the left child
            middle.left = left;
            middle.leftSize = leftSize;
            if(left != null) left.parent = middle;
            // update the right child
            middle.right = right;
            middle.rightSize = rightSize;
            if(right != null) right.parent = middle;
            // update the height
            middle.height = height;
            // update the parent
            middle.parent = parent;
            if(parent != null) {
                parent.replaceChildNode(this, middle);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(middle);
            }
        }
        // clear the parent
        parent = null;
        // clear the left child
        left = null;
        leftSize = 0;
        // clear the right child
        right = null;
        rightSize = 0;
    }
    /**
     * Notifies that a node has been removed from the specified subtree.
     * This simply decrements the count on that subtree.
     */
    private void notifyChildNodeRemoved(IndexedTreeNode subtree) {
        if(subtree == left) leftSize--;
        else if(subtree == right) rightSize--;
        else throw new IllegalArgumentException(this + " cannot remove a subtree that does not exist on this node!");
        if(parent != null) parent.notifyChildNodeRemoved(this);
    }
    /**
     * Replaces the specified child with a new child.
     */
    private void replaceChildNode(IndexedTreeNode original, IndexedTreeNode replacement) {
        if(original == left) left = replacement;
        else if(original == right) right = replacement;
        else throw new IllegalArgumentException(this + " cannot replace a non-existant child");
    }

    /**
     * A primitive way to validate that nodes are stored in sorted
     * order and that their sizes are consistent. This throws a
     * IllegalStateException if any infraction is found.
     */
    void validate(IndexedTree host) {
        // first validate the children
        if(left != null) left.validate(host);
        if(right != null) right.validate(host);

        // validate sort order
        if(host.getComparator() != null) {
            if(leftSize > 0 && host.getComparator().compare(left.value, value) > 0) {
                throw new IllegalStateException("" + this + "left larger than middle");
            }
            if(rightSize > 0) if(host.getComparator().compare(value, right.value) > 0) {
                throw new IllegalStateException("" + this + " middle larger than right");
            }
        }
        // validate left size
        if((left == null && leftSize != 0) || (left != null && leftSize != left.size())) {
            throw new IllegalStateException("Cached leftSize " + leftSize + " != reported left.size() " + left.size());
        }
        // validate right size
        if((right == null && rightSize != 0) || (right != null && rightSize != right.size())) {
            throw new IllegalStateException("Cached rightSize " + rightSize + " != reported right.size() " + right.size());
        }
    }

    /**
     * Ensures that the tree satisfies the AVL property.  It is sufficient to
     * recurse up the tree only as long as height recalculations are needed.
     * As such, this method is intended to be called only on a node whose height
     * may be out of sync due to an insertion or deletion.
     */
    private void ensureAVL(IndexedTree host) {
        int oldHeight = height;
        recalculateHeight();
        avlRotate(host);

        // If adjustments were made, recurse up the tree
        if(height != oldHeight && parent != null) parent.ensureAVL(host);
    }

    /**
     * Recalculates the cached height at this level.
     */
    private void recalculateHeight() {
        int leftHeight = left == null ? 0 : left.height;
        int rightHeight = right == null ? 0 : right.height;
        height = 1 + Math.max(leftHeight, rightHeight);
    }

    /**
     * Determines if AVL rotations are required and performs them if they are.
     */
    private void avlRotate(IndexedTree host) {
        // look up the left and right heights
        int leftHeight = (left != null ? left.height : 0);
        int rightHeight = (right != null ? right.height : 0);

        // rotations will be on the left
        if(leftHeight - rightHeight >= 2) {
            // determine if a double rotation is necessary
            int leftLeftHeight = (left.left != null ? left.left.height : 0);
            int leftRightHeight = (left.right != null ? left.right.height : 0);

            // Perform first half of double rotation if necessary
            if(leftRightHeight > leftLeftHeight) left.rotateRight(host);

            // Do the rotation for this node
            rotateLeft(host);

        // rotations will be on the right
        } else if(rightHeight - leftHeight >= 2) {
            // determine if a double rotation is necessary
            int rightLeftHeight = (right.left != null ? right.left.height : 0);
            int rightRightHeight = (right.right != null ? right.right.height : 0);

            // Perform first half of double rotation if necessary
            if(rightLeftHeight > rightRightHeight) right.rotateLeft(host);

            // Do the rotation for this node
            rotateRight(host);
        }
    }

    /**
     * AVL-Rotates this subtree with its left child.
     *
     * For every link (left, right, parent), there are up to three
     * updates to be made. We need to set the new value on the
     * replacement, the new value on this, and the new value on the
     * other node.
     */
    private void rotateLeft(IndexedTree host) {
        // The replacement node is on the left
        IndexedTreeNode replacement = left;

        // take the right child of the replacement as my left child
        left = replacement.right;
        leftSize = replacement.rightSize;
        if(replacement.right != null) replacement.right.parent = this;

        // set the right child of the replacement to this
        replacement.right = this;
        replacement.rightSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replaceChildNode(this, replacement);

        // set a new tree root
        else host.setRootNode(replacement);

        // fix parent links on this and the replacement
        replacement.parent = parent;
        parent = replacement;

        // recalculate height at this node
        recalculateHeight();

        // require height to be recalculated on the replacement node
        replacement.height = 0;
    }

    /**
     * AVL-Rotates this subtree with its right child.
     *
     * For every link (left, right, parent), there are up to three
     * updates to be made. We need to set the new value on the
     * replacement, the new value on this, and the new value on the
     * other node.
     */
    private void rotateRight(IndexedTree host) {
        // The replacement node is on the right
        IndexedTreeNode replacement = right;

        // take the right child of the replacement as my left child
        right = replacement.left;
        rightSize = replacement.leftSize;
        if(replacement.left != null) replacement.left.parent = this;

        // set the right child of the replacement to this
        replacement.left = this;
        replacement.leftSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replaceChildNode(this, replacement);

        // set a new tree root
        else host.setRootNode(replacement);

        //fix parent links on this and the replacement
        replacement.parent = parent;
        parent = replacement;

        // recalculate height at this node
        recalculateHeight();

        // require height to be recalculated on the replacement node
        replacement.height = 0;
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * <p>This method has package-level protection as if it is
     * called on a node directly it will have non-deterministic
     * results.
     */
    boolean contains(Comparator comparator, Object object) {
        int sortSide = comparator.compare(object, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return false;
            return left.contains(comparator, object);

        // if it equals this node, return this
        } else if(sortSide == 0) {
            return true;

        // if it sorts on the right side, search there
        } else {
            if(right == null) return false;
            return right.contains(comparator, object);
        }
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>This method has package-level protection as if it is
     * called on a node directly it will have non-deterministic
     * results.
     */
    int indexOf(Comparator comparator, Object object, boolean simulate) {
        int sortSide = comparator.compare(object, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) {
                if(simulate) return getIndex();
                else return -1;
            } else {
                return left.indexOf(comparator, object, simulate);
            }

        // if it equals this node, search to the left for equal values
        } else if(sortSide == 0) {
            return findLastNode(comparator, this, object, true);

        // if it sorts on the right side, search there
        } else {
            if(right == null) {
                if(simulate) return getIndex() + 1;
                else return -1;
            } else {
                return right.indexOf(comparator, object, simulate);
            }
        }
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>This method has package-level protection as if it is
     * called on a node directly it will have non-deterministic
     * results.
     */
    int lastIndexOf(Comparator comparator, Object object) {
        int sortSide = comparator.compare(object, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return -1;
            return left.lastIndexOf(comparator, object);

        // if it equals this node, search to the right for equal values
        } else if(sortSide == 0) {
            return findLastNode(comparator, this, object, false);

        // if it sorts on the right side, search there
        } else {
            if(right == null) return -1;
            return right.lastIndexOf(comparator, object);
        }
    }

    /**
     * Helper method to the indexOf and lastIndexOf methods
     */
    private int findLastNode(Comparator comparator, IndexedTreeNode parentNode, Object object, boolean goLeft) {
        IndexedTreeNode child = null;
        if(goLeft) child = parentNode.left;
        else child = parentNode.right;

        // Child doesn't exist
        if(child == null) {
            return parentNode.getIndex();

        // Child is different than the parent node
        } else if(comparator.compare(object, child.value) != 0) {
            int result = secondaryLastNodeSearch(comparator, child, object, !goLeft);
            if(result != -1) {
                return result;
            }
            return parentNode.getIndex();

        // Child is the same so recurse on the child
        } else {
            return findLastNode(comparator, child, object, goLeft);
        }
    }

    /**
     * Helper method to the indexOf and lastIndexOf methods
     */
    private int secondaryLastNodeSearch(Comparator comparator, IndexedTreeNode parentNode, Object object, boolean goLeft) {
        IndexedTreeNode child = null;
        if(goLeft) child = parentNode.left;
        else child = parentNode.right;

        // Child doesn't exist
        if(child == null) {
            return -1;

        // Child is the same as the searched for node
        } else if(comparator.compare(object, child.value) == 0) {
            return findLastNode(comparator, child, object, !goLeft);

        // Child is different so recurse on the child
        } else {
            return secondaryLastNodeSearch(comparator, child, object, goLeft);
        }
    }

    /**
     * Prints the tree by its contents.
     */
    public String toString() {
        //String valueString = "" + height();
        String valueString = value.toString();
        if(left != null && right != null) {
            return "(" + left.toString() + " " + valueString + " " + right.toString() + ")";
        } else if(left != null) {
            return "(" + left.toString() + " " + valueString + " .)";
        } else if(right != null) {
            return "(. " + valueString + " " + right.toString() + ")";
        } else if(value == null) {
            return ".";
        } else {
            return valueString;
        }
    }

    /**
     * A simple read-only iterator of the indexed tree.
     *
     * @todo implement write capability, and backward-forward
     *      motion to complete the ListIterator interface.
     */
    static class IndexedTreeIterator implements Iterator {

        /** the last tree node returned by this iterator */
        private IndexedTreeNode next;

        /**
         * Creates an iterator that iterates the tree starting at the specified
         * node.
         */
        public IndexedTreeIterator(IndexedTreeNode first) {
            this.next = first;
        }

        /**
         * Returns true if the iteration has more elements.
         */
        public boolean hasNext() {
            return (next != null);
         }

        /**
         * Returns the next element in the iteration.
         */
        public Object next() {
            // there are no more nodes right of this one
            if (next == null) throw new NoSuchElementException();
            // before returning the result, calculate the result to follow
            IndexedTreeNode result = next;
            // if there's a right child, return that child's leftmost child
            if(next.rightSize != 0) {
                next = next.right.getSmallestChildNode();
            // if there's no right child, return the first right parent
            } else {
                IndexedTreeNode currentParent = next;
                next = null;
                // set the next value if this node has a parent on the right somewhere
                while(currentParent.parent != null) {
                    if(currentParent.parent.left == currentParent) {
                        next = currentParent.parent;
                        break;
                    } else {
                        currentParent = currentParent.parent;
                    }
                }
            }
            return result;
        }

        /**
         * This operation is <strong>not implemented</strong>. Removes from
         * the underlying collection the last element returned by the iterator.
         */
        public void remove() {
            throw new UnsupportedOperationException("The method is not implemented.");
        }
    }
}

