package com.libereco.taxmap.symbolics.data.tree;

import java.util.Iterator;
import java.util.List;

/**
 * An interface for the gestalt data structure. 
 * A gestalt is a hierarchy of nodes each of which is populated with semantically consistent n-grams. 
 */
public interface IGestalt 
{
	/**
	 * Sets a new root node for the gestalt.
	 */
	void setRootNode(INode root);

	/**
	 * Returns the root node of the gestalt.
	 */
	INode getRootNode();

	/**
	 * Returns true if the gestalt has a root node.
	 */
	boolean hasRootNode();

	/**
	 * Creates a node.
	 */
	INode createNode();

	/**
	 * Creates a new node with the given name.
	 */
	INode createNode(String name);

	/**
	 * Creates a root node.
	 */
	INode createRootNode();

	/**
	 * Creates a root node with a name.
	 */
	INode createRootNode(String name);

	/**
	 * Returns iterator over all gestalt nodes.
	 */
	Iterator<INode> getNodeIterator();

	/**
	 * Returns unmodifiable list of all gestalt nodes.
	 */
	List<INode> getNodeList();
}

