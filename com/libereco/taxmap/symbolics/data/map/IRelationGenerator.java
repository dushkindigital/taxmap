/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.config.IConfiguration;
import com.libereco.taxmap.symbolics.data.ling.ILabelConcept;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;

/**
 * Generates relations between two contexts or concepts.
 * @author Chiranjit Acharya
 */
public interface IRelationGenerator extends IConfiguration 
{
	IContextRelation<INode> getContextRelationInstance(IContext source, IContext target);

	IContextRelation<ILabelConcept> getConceptRelationInstance(IContext source, IContext target);
}
