/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.tree;

import java.util.Iterator;
import java.util.List;

/**
 * An interface for the gestalt data structure. 
 * A gestalt is a hierarchy of nodes each of which is populated with semantically consistent n-grams. 
 * @author Chiranjit Acharya
 */
public interface IGestalt 
{
	/**
	 * Sets a new root node for the gestalt.
	 */
	void SetRootNode(INode node);

	/**
	 * Returns the root node of the gestalt.
	 */
	INode GetRootNode();

	/**
	 * Returns true if the gestalt has a root node.
	 */
	boolean HasRootNode();

	/**
	 * Creates a node.
	 */
	INode CreateNode();

	/**
	 * Creates a new node with the given name.
	 */
	INode CreateNode(String name);

	/**
	 * Creates a root node.
	 */
	INode CreateRootNode();

	/**
	 * Creates a root node with a name.
	 */
	INode CreateRootNode(String name);

	/**
	 * Returns iterator over all gestalt nodes.
	 */
	Iterator<INode> GetNodeIterator();

	/**
	 * Returns unmodifiable list of all gestalt nodes.
	 */
	List<INode> GetNodeList();
}

