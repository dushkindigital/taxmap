/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.tree;

import com.libereco.taxmap.symbolics.data.ling.ILabelConcept;

import java.util.Iterator;
import java.util.List;

/**
 * An interface to the information contained in the node.
 * @author Chiranjit Acharya
 */
public interface INodeCore 
{
	/**
	 * Returns the node name, 
	 * A name is a natural language text associated with a node.
	 */
	String getNodeName();

	/**
	 * Sets the node name.
	 */
	void setNodeName(String Name);

	/**
	 * Returns the concept of a label predicate. Concept of a label is a logical representation of the
	 * natural language label associated with a node.
	 *
	 * @return concept of a label predicate
	 */
	String getLabelConceptPredicate();

	/**
	 * Sets the concept of a label predicate.
	 * @param LabelConceptPredicate the concept of a label predicate
	 */
	void setLabelConceptPredicate(String LabelConceptPredicate);

	/**
	 * Returns the concept entailed by the predicate at a node. 
	 * Concept at a node predicate is the expression of the concept of a node situated in a tree.
	 * @return concept at node predicate
	 */
	String getNodeConceptPredicate();

	/**
	 * Sets the concept at node predicate.
	 * @param NodeConceptPredicate the concept at node predicate
	 */
	void setNodeConceptPredicate(String NodeConceptPredicate);

	/**
	 * Returns the unique id of the node.
	 */
	String getNodeId();

	/**
	 * Sets the id of the node.
	 * @param Id the new id of the node
	 */
	void setNodeId(String Id);

	/**
	 * Indicates whether this node belongs to the source context.
	 * @return true if the node belongs to the source context
	 */
	boolean getNodeSourceFlag();

	/**
	 * Sets the source flag to indicate this node belongs to the source context.
	 * @param source context belongingness flag
	 */
	void setNodeSourceFlag(boolean source);

	/**
	 * Returns the concept at given location index
	 * @param location index
	 * @return concept at location
	 */
	ILabelConcept getConceptAt(int location);

	/**
	 * Returns the number of concepts.
	 */
	int getConceptCount();

	/**
	 * Returns the location index of concept in the existing label concepts. 
	 * If the existing label does not contain concept, -1 will be returned.
	 *
	 * @param concept 
	 * @return location index
	 */
	int getConceptIndex(ILabelConcept concept);

	/**
	 * Returns fundamental concepts of labels associated with the given node.
	 * @return fundamental concepts of labels
	 */
	Iterator<ILabelConcept> getConceptIterator();

	/**
	 * Returns unmodifiable list of the concepts of the existing label.
	 * @return unmodifiable list of concepts of the existing label
	 */
	List<ILabelConcept> getConceptList();

	/**
	 * Creates an instance of an Concept.
	 */
	ILabelConcept createConcept();

	/**
	 * Adds fundamental concept of label to the node concepts.
	 * @param concept fundamental concept of label
	 */
	void addConcept(ILabelConcept concept);

	/**
	 * Adds concept to the existing label concepts at index.
	 *
	 * @param index location where the concept is to be added
	 * @param concept  
	 */
	void addConcept(int index, ILabelConcept concept);

	/**
	 * Removes the concept at index from the existing set of label concepts.
	 * @param index location of an concept to remove
	 */
	void removeConcept(int index);

	/**
	 * Removes concept from the existing set of label concepts.
	 * @param concept
	 */
	void removeConcept(ILabelConcept concept);

	/**
	 * Returns if a node has been rendered properly.
	 */
	boolean isNodeRendered();

	/**
	 * Sets if a node has been rendered properly.
	 */
	void setNodeRendered(boolean rendition);

	/**
	 * Returns if a subtree has been rendered properly.
	 */
	boolean isSubtreeRendered();

	/**
	 * Returns the derivation info of a node.
	 */
	String getDerivationInfo();

	/**
	 * Sets the derivation info of a node.
	 */
	void setDerivationInfo(String origin);

	/**
	 * Returns the instance of the current node object
	 */
	Object getInstance();

	/**
	 * Sets the instance of the current node object
	 */
	void setInstance(Object instance);
}

