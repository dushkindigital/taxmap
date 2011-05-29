package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.data.matrix.IIndexedObject;
import com.libereco.taxmap.symbolics.data.matrix.ITaxMapGenerator;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;

/**
 * Mapping between context nodes based on a bilinear map.
 */
public class NodeMap extends BilinearMap<INode> 
{
	public NodeMap(ITaxMapGenerator generator, IContext source, IContext target) 
	{
		super(generator, source, target);
	}

	protected int getRowCount(IContext context) 
	{
		return getNodeCount(context);
	}

	protected int getColumnCount(IContext context) 
	{
		return getNodeCount(context);
	}

	private int getNodeCount(IContext context) 
	{
		int nodeNum = 0;
		for (INode node : context.getNodesList()) 
		{
			node.setIndex(nodeNum);
			nodeNum++;
		}
		return nodeNum;
	}

	protected void initColumn(IContext targetContext, IIndexedObject[] targetObjects) 
	{
		initNodes(targetContext, targetObjects);
	}

	protected void initRow(IContext sourceContext, IIndexedObject[] sourceObjects) 
	{
		initNodes(sourceContext, sourceObjects);
	}

	private void initNodes(IContext context, IIndexedObject[] obj) 
	{
		for (INode node : context.getNodesList()) 
		{
			obj[node.getIndex()] = node;
		}
	}
}

