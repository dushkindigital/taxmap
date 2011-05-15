package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.config.IConfiguration;
import com.libereco.taxmap.symbolics.data.ling.ILabelConcept;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;

/**
 * Generates relations between two contexts or concepts.
 */
public interface IRelationGenerator extends IConfiguration 
{
	IContextRelation<INode> getContextRelationInstance(IContext source, IContext target);

	IContextRelation<ILabelConcept> getConceptRelationInstance(IContext source, IContext target);
}
