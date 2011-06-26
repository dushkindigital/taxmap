package com.libereco.taxmap.symbolics.data.tree;

import java.util.Iterator;
import java.util.List;

/**
 * An interface for the context data structure. 
 * A context is a hierarchy of nodes each of which is populated with semantically consistent n-grams. 
 */
public interface IContext 
{
	/**
	 * Sets a new root node for the context.
	 */
	void setRootNode(INode root);

	/**
	 * Returns the root node of the context.
	 */
	INode getRootNode();

	/**
	 * Returns true if the context has a root node.
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
	 * Returns iterator over all context nodes.
	 */
	Iterator<INode> getNodeIterator();

	/**
	 * Returns unmodifiable list of all context nodes.
	 */
	List<INode> getNodeList();
}

