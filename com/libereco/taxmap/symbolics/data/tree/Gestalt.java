package com.libereco.taxmap.symbolics.data.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * A Gestalt that is built on top a tree data structure.
 */
public class Gestalt implements IGestalt, IHierarchy 
{
	private INode _root;
	private ArrayList<INode> _nodeList;

	public Gestalt() 
	{
		_root = null;
		_nodeList = null;
	}

	public void setRootNode(INode node) 
	{
		this._root = node;
		_root.addHierarchy(this);
	}

	public INode getRootNode() 
	{
		return _root;
	}

	public boolean hasRootNode() 
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

	public INode createRootNode() 
	{
		_root = new Node();
		_root.addHierarchy(this);
		return _root;
	}

	public INode createRootNode(String name) 
	{
		INode node = createRootNode();
		node.getNodeData().setName(name);
		return node;
	}

	public Iterator<INode> getNodeIterator() 
	{
		if (hasRootNode()) 
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
			if (hasRootNode()) 
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

