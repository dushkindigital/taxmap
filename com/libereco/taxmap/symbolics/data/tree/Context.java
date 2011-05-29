package com.libereco.taxmap.symbolics.data.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * A Context that is built on top a tree data structure.
 */
public class Context implements IContext, IHierarchy 
{
	private INode _root;
	private ArrayList<INode> _nodeList;

	public Context() 
	{
		_root = null;
		_nodeList = null;
	}

	public void setRoot(INode node) 
	{
		this._root = node;
		_root.addHierarchy(this);
	}

	public INode getRoot() 
	{
		return _root;
	}

	public boolean hasRoot() 
	{
		return _root != null;
	}

	public INode createNode() 
	{
		return new Node();
	}

	public INode createNode(String name) 
	{
		return new Node(name);
	}

	public INode createRoot() 
	{
		_root = new Node();
		_root.addHierarchy(this);
		return _root;
	}

	public INode createRoot(String name) 
	{
		INode node = createRoot();
		node.getNodeData().setName(name);
		return node;
	}

	public Iterator<INode> getNodes() 
	{
		if (hasRoot()) 
		{
			return new Node.StartIterator(_root, _root.getDescendants());
		} 
		else 
		{
			return Collections.<INode>emptyList().iterator();
		}
	}

	public List<INode> getNodeList() 
	{
		if (_nodeList != null) 
		{
			return Collections.unmodifiableList(_nodeList);
		} 
		else 
		{
			if (hasRoot()) 
			{
				_nodeList = new ArrayList<INode>();
				_nodeList.add(root);
				_nodeList.addAll(root.getDescendantsList());
				_nodeList.trimToSize();
				return Collections.unmodifiableList(_nodeList);
			} 
			else 
			{
				return Collections.emptyList();
			}
		}
	}

	public void trim() 
	{
		if (_root instanceof Node) 
		{
			((Node) _root).trim();
		}
	}

	public void changeTree() 
	{
		_root = null;
		_nodeList = null;
	}
}

