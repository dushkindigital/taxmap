/**
 *  Copyright (C) 2011 Dushkin Digital Media, LLC
 *  500 E 77th Street, Ste. 806
 *  New York, NY 10162
 *
 *  All rights reserved.
 **/

package com.libereco.taxmap.symbolics.data.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * A Gestalt class is built on top a tree data structure.
 * @author Chiranjit Acharya
 */
public class Gestalt implements IGestalt, IHierarchy 
{
	private INode m_TopNode;
	private ArrayList<INode> m_NodeList;

	public Gestalt() 
	{
		m_TopNode = null;
		m_NodeList = null;
	}

	public void SetRootNode(INode node) 
	{
		this.m_TopNode = node;
		m_TopNode.addHierarchy(this);
	}

	public INode GetRootNode() 
	{
		return m_TopNode;
	}

	public boolean HasRootNode() 
	{
		return m_TopNode != null;
	}

	public INode CreateNode() 
	{
		return new Node();
	}

	public INode CreateNode(String name) 
	{
		return new Node(name);
	}

	public INode CreateRootNode() 
	{
		m_TopNode = new Node();
		m_TopNode.addHierarchy(this);
		return m_TopNode;
	}

	public INode CreateRootNode(String name) 
	{
		INode node = createRootNode();
		node.getNodeData().setName(name);
		return node;
	}

	public Iterator<INode> GetNodeIterator() 
	{
		if (hasRootNode()) 
		{
			return new Node.StartIterator(m_TopNode, m_TopNode.getDescendants());
		} 
		else 
		{
			return Collections.<INode>emptyList().iterator();
		}
	}

	public List<INode> GetNodeList() 
	{
		if (m_NodeList != null) 
		{
			return Collections.unmodifiableList(m_NodeList);
		} 
		else 
		{
			if (HasRootNode()) 
			{
				m_NodeList = new ArrayList<INode>();
				m_NodeList.add(m_TopNode);
				m_NodeList.addAll(m_TopNode.getDescendantsList());
				m_NodeList.trimToSize();
				return Collections.unmodifiableList(m_NodeList);
			} 
			else 
			{
				return Collections.emptyList();
			}
		}
	}

	public void Trim() 
	{
		if (m_TopNode instanceof Node) 
		{
			((Node) m_TopNode).trim();
		}
	}

	public void ChangeTree() 
	{
		m_TopNode = null;
		m_NodeList = null;
	}
}

