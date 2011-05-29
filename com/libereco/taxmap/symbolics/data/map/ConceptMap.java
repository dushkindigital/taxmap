package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.data.ling.ILabelConcept;
import com.libereco.taxmap.symbolics.data.matrix.IIndexedObject;
import com.libereco.taxmap.symbolics.data.matrix.ITaxMapGenerator;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;

/**
 * Mapping between concept labels based on a bilinear map.
 */
public class ConceptMap extends BilinearMap<ILabelConcept> 
{
	public ConceptMap(ITaxMapGenerator generator, IContext source, IContext target) 
	{
		super(generator, source, target);
	}

	protected int getRowCount(IContext context) 
	{
		return getConceptCount(context);
	}

	protected int getColCount(IContext context) 
	{
		return getConceptCount(context);
	}

	private int getConceptCount(IContext context) 
	{
		int conceptNum = 0;
		for (INode node : context.getNodesList()) 
		{
			for (ILabelConcept concept : node.getNodeData().getConceptList()) 
			{
				concept.setIndex(conceptNum);
				conceptNum++;
			}
		}
		return conceptNum;
	}

	protected void initColumn(IContext targetContext, IIndexedObject[] targetObjects) 
	{
		initNodes(targetContext, targetObjects);
	}

	protected void initRow(IContext sourceContext, IIndexedObject[] sourceObjects) 
	{
		initNodes(sourceContext, sourceObjects);
	}

	private void initNodes(IContext context, IIndexedObject[] object) 
	{
		for (INode node : context.getNodesList()) 
		{
			for (ILabelConcept concept : node.getNodeData().getConceptList()) 
			{
				object[concept.getIndex()] = concept;
			}
		}
	}
}

