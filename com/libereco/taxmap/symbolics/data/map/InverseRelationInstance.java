package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.data.tree.INode;

/**
 * Creates an inverse relation instance corresponding to a forward relation instance
 */
public class InverseRelationInstance extends RelationInstance<INode> 
{
	public InverseRelationInstance(INode sourceNode, INode targetNode, char relation) 
	{
		super(sourceNode, targetNode, relation);
		if (null != sourceNode && sourceNode.getNodeData().getSource()) 
		{
			this.relation = relation;
			this.source = sourceNode;
			this.target = targetNode;
		} 
		else 
		{
			this.source = targetNode;
			this.target = sourceNode;
			if (LESS_GENERAL == relation) 
			{
				this.relation = MORE_GENERAL;
			} 
			else 
			{
				if (MORE_GENERAL == relation) 
				{
					this.relation = LESS_GENERAL;
				} 
				else 
				{
					this.relation = relation;
				}
			}
		}
	}
}

