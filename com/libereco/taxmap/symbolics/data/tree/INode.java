package com.libereco.taxmap.symbolics.data.tree;

import java.util.Iterator;
import java.util.List;

import com.libereco.taxmap.symbolics.data.IIndexedObject;

/**
 * An interface to a node.
 */
public interface INode extends IIndexedObject
{
	/**
	 * Returns the child node at given index.
	 */
	INode getChildNode(int Index);

	/**
	 * Returns the number of children INodes.
	 */
	int getChildNodeCount();

	/**
	 * Returns the index of node in the receivers children. 
	 *
	 * @param child a node to search for
	 * @return the index of node in the receivers children
	 */
	int getChildNodeIndex(INode node);

	/**
	 * Returns the iterator over the children of the receiver.
	 */
	Iterator<INode> getChildNodes();

	/**
	 * Returns unmodifiable list of receivers children.
	 */
	List<INode> getChildNodeList();

	/**
	 * Creates a child to the given node as the last child.
	 * @return a child node 
	 */
	INode createChildNode();

	/**
	 * Creates a child with a name to the given node as the last child.
	 * @param name a name for a new child
	 * @return a child node 
	 */
	INode createChildNode(String name);

	/**
	 * Adds a child to the given node as the last child.
	 * @param node
	 */
	void addChildNode(INode node);

	/**
	 * Adds child to the receiver at index.
	 * @param index index where the node will be added
	 * @param node 
	 */
	void addChildNode(int index, INode node);

	/**
	 * Removes the child at index from the existing list
	 * @param index index of a child to remove
	 */
	void removeChildNode(int index);

	/**
	 * Removes node from the receiver.
	 * @param node child to remove
	 */
	void removeChildNode(INode node);

	/**
	 * Returns the parent of the receiver.
	 */
	INode getParentNode();

	/**
	 * Sets the parent of the receiver to newParent.
	 * @param node new parent
	 */
	void setParentNode(INode node);

	/**
	 * Returns true if the receiver has a parent and false otherwise.
	 */
	boolean hasParentNode();

	/**
	 * Removes the subtree rooted at this node from the tree
	 */
	void removeFromParentNode();

	/**
	 * Returns true if the receiver is a leaf.
	 */
	boolean isLeafNode();

	/**
	 * Returns the count of ancestor nodes.
	 */
	int getAncestorCount();

	/**
	 * Returns ancestors of the receiver. 
	 */
	Iterator<INode> getAncestorNodes();

	/**
	 * Returns unmodifiable list of receivers ancestors.
	 */
	List<INode> getAncestorsList();

	/**
	 * Returns the level of this node
	 */
	int getNodeLevel();

	/**
	 * Returns the count of descendant nodes.
	 */
	int getDescendantNodeCount();

	/**
	 * Returns descendants of the receiver. 
	 */
	Iterator<INode> getDescendantNodes();

	/**
	 * Returns list of receivers descendants.
	 */
	List<INode> getDescendantNodeList();

	/**
	 * Returns interface to the node metadata.
	 */
	INodeCore getNodeCore();
}

