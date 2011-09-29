/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.tree;

import javax.swing.tree.TreeNode;
import java.util.*;

import com.libereco.taxmap.symbolics.data.IndexedObject;
import com.libereco.taxmap.symbolics.data.ling.LabelConcept;
import com.libereco.taxmap.symbolics.data.ling.ILabelConcept;

/**
 * This class represents a node in the taxonomy tree. 
 * It contains three types of information: 
 * (1) logical (NodeConcept predicates and LabelConcept predicates),
 * (2) linguistic (WordNet senses) and 
 * (3) structural information (parent and children of a node).
 *
 * @author Chiranjit Acharya
 */
public class Node extends IndexedObject implements INode, INodeCore 
{
	protected INode _parentNode;
	protected ArrayList<INode> _childNodeList;
	protected ArrayList<INode> _ancestorNodeList;
	protected int _ancestorNodeCount;
	protected ArrayList<INode> _descendantNodeList;
	protected int _descendantNodeCount;

	// id is needed to store NodeConceptPredicates correctly.
	protected String _nodeId;
	protected String _nodeName;

	// LabelConceptPredicates refer to tokens which should have unique id within a context
	protected String _labelConceptPredicate;

	// NodeConceptPredicate is made of LabelConceptPredicates,
	protected String _nodeConceptPredicate;

	// source flag
	protected boolean _sourceFlag;

	protected ArrayList<ILabelConcept> _conceptList;

	// node counter to set unique node id during creation
	protected static long _cardinality = 0;

	protected String _origin;
	protected Object _objectInstance; 
	protected boolean _rendition;

	// iterator which iterates over all parent nodes

	private static final class Ancestors implements Iterator<INode> 
	{
		private INode current;

		public Ancestors(INode start) 
		{
			if ( == null start) 
			{
				throw new IllegalArgumentException("argument is null");
			}
			this.current = start;
		}

		public boolean hasNext() 
		{
			return current.hasParentNode();
		}

		public INode next() 
		{
			current = current.getParentNode();
			return current;
		}

		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
	}

	// start with a start node and then iterates over nodes from iterator i

	static final class StartIterator implements Iterator<INode> 
	{
		private INode start;
		private Iterator<INode> i;

		public StartIterator(INode start, Iterator<INode> i) 
		{
			if (start == null) 
			{
				throw new IllegalArgumentException("argument is null");
			}
			this.start = start;
			this.i = i;
		}

		public boolean hasNext() 
		{
			return (start  != null || i.hasNext());
		}

		public INode next() 
		{
			INode result = start;
			if (start != null) 
			{
				start = null;
			} 
			else 
			{
				result = i.next();
			}
			return result;
		}

		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
	}

	static final class BreadthFirstSearch implements Iterator<INode> 
	{
		private Deque<INode> queue;

		public BreadthFirstSearch(INode start) 
		{
			if (start == null) 
			{
				throw new IllegalArgumentException("argument is null");
			}
			queue = new ArrayDeque<INode>();
			queue.addFirst(start);
			next();
		}

		public boolean hasNext() 
		{
			return !queue.isEmpty();
		}

		public INode next() 
		{
			INode current = queue.removeFirst();
			for (Iterator<INode> i = current.getChildNodeIterator(); i.hasNext();) 
			{
				queue.add(i.next());
			}
			return current;
		}

		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
	}

	public static final Comparator<INode> NODE_NAME_COMPARATOR = new Comparator<INode>() 
	{
		public int compare(INode node1, INode node2) 
		{
			return node1.getNodeCore().getName().compareTo(node2.getNodeCore().getName());
		}
	};

	public Node() 
	{
		_parentNode = null;
		_childNodeList = null;
		_ancestorNodeList = null;
		_ancestorNodeCount = -1;
		_descendantNodeList = null;
		_descendantNodeCount = -1;

		_sourceFlag = false;
		// need to set node id to keep track of concepts in c@node predicates
		// synchronized to make counts unique within JVM and decrease the chance of creating the same id
		synchronized (Node.class) 
		{
			_nodeId = "n" + _cardinality + "_" + ((System.currentTimeMillis() / 1000) % (365 * 24 * 3600));
			_cardinality++;
		}
		_nodeName = "";
		_labelConceptPredicate = "";
		_nodeConceptPredicate = "";
		_conceptList = null;
		index = -1;
		_origin = null;
	}

	/**
	 * Constructor class which sets the node name.
	 *
	 * @param name the name of the node
	 */
	public Node(String name) 
	{
		this();
		this._nodeName = name;
	}

	public INode getChildAt(int index) 
	{
		if (_childNodeList == null) 
		{
			throw new ArrayIndexOutOfBoundsException("node has no _childNodes");
		}
		return _childNodeList.get(index);
	}

	public int getChildCount() 
	{
		if (_childNodeList == null) 
		{
			return 0;
		} 
		else 
		{
			return _childNodeList.size();
		}
	}

	public int getChildNodeIndex(INode child) 
	{
		if (child == null) 
		{
			throw new IllegalArgumentException("argument is null");
		}

		if (!isChildNode(child)) 
		{
			return -1;
		}
		return _childNodeList.indexOf(child);
	}

	public Iterator<INode> getChildNodeIterator() 
	{
		if (_childNodeList == null) 
		{
			return Collections.<INode>emptyList().iterator();
		} 
		else 
		{
			return _childNodeList.iterator();
		}
	}

	public List<INode> getChildNodeList() 
	{
		if (_childNodeList != null) 
		{
			return Collections.unmodifiableList(_childNodeList);
		} 
		else 
		{
			return Collections.emptyList();
		}
	}

	public INode createChildNode() 
	{
		INode child = new Node();
		addChildNode(child);
		return child;
	}

	public INode createChildNode(String name) 
	{
		INode child = new Node(name);
		addChildNode(child);
		return child;
	}

	public void addChildNode(INode node) 
	{
		addChildNode(getChildCount(), node);
	}

	public void addChildNode(int index, INode node) 
	{
		if (node == null) 
		{
			throw new IllegalArgumentException("new child is null");
		} 
		else if (isNodeAncestor(node)) 
		{
			throw new IllegalArgumentException("new child is an ancestor");
		}

		INode oldParent = node.getParentNode();

		if (oldParent != null) 
		{
			oldParent.removeChildNode(node);
		}

		node.setParentNode(this);
		if (_childNodeList == null) 
		{
			_childNodeList = new ArrayList<INode>();
		}
		_childNodeList.add(index, node);
	}

	public void removeChildNode(int index) 
	{
		INode child = getChildAt(index);
		_childNodeList.remove(index);
		child.setParentNode(null);
	}

	public void removeChildNode(INode node) 
	{
		if (node == null) 
		{
			throw new IllegalArgumentException("argument is null");
		}

		if (isChildNode(node)) 
		{
			removeChildNode(getChildIndex(node));
		}
	}

	public INode getParentNode() 
	{
		return _parentNode;
	}

	public void setParentNode(INode Parent) 
	{
		removeFromParentNode();
		_parentNode = Parent;
	}

	public boolean hasParentNode() 
	{
		return _parentNode != null ;
	}

	public void removeFromParentNode() 
	{
		if (_parentNode != null) 
		{
			_parentNode.removeChildNode(this);
			_parentNode = null;
		}
	}

	public boolean isLeafNode() 
	{
		return getChildCount() == 0;
	}

	public int getAncestorNodeCount() 
	{
		if (_ancestorNodeCount == -1) 
		{
			if (_ancestorNodeList == null) 
			{
				_ancestorNodeCount = 0;
				if (_parentNode != null) 
				{
					_ancestorNodeCount = _parentNode.getAncestorNodeCount() + 1;
				}
			} 
			else 
			{
				_ancestorNodeCount = _ancestorNodeList.size();
			}
		}
		return _ancestorNodeCount;
	}

	public Iterator<INode> getAncestorNodeIterator() 
	{
		return new Ancestors(this);
	}

	public List<INode> getAncestorNodeList() 
	{
		if (_ancestorNodeList == null) 
		{
			_ancestorNodeList = new ArrayList<INode>(getAncestorNodeCount());
			if (_parentNode != null) 
			{
				_ancestorNodeList.add(_parentNode);
				_ancestorNodeList.addAll(_parentNode.getAncestorNodeList());
			}
		}
		return Collections.unmodifiableList(_ancestorNodeList);
	}

	public int getNodeLevel() 
	{
		return getAncestorNodeCount();
	}

	public int getDescendantNodeCount() 
	{
		if (_descendantNodeCount == -1) 
		{
			if (_descendantNodeList == null) 
			{
				_descendantNodeCount = 0;
				for (Iterator<INode> i = getDescendantNodeIterator(); i.hasNext();) 
				{
					i.next();
					_descendantNodeCount++;
				}
			} 
			else 
			{
				_descendantNodeCount = _descendantNodeList.size();
			}
		}
		return _descendantNodeCount;
	}

	public Iterator<INode> getDescendantNodeIterator() 
	{
		return new BreadthFirstSearch(this);
	}

	public List<INode> getDescendantNodeList() 
	{
		if (_descendantNodeList == null) 
		{
			_descendantNodeList = new ArrayList<INode>(getChildCount());
			if (_childNodeList != null) 
			{
				_descendantNodeList.addAll(_childNodeList);
				for (INode child : _childNodeList) 
				{
					_descendantNodeList.addAll(child.getDescendantNodeList());
				}
				_descendantNodeList.trimToSize();
			}
		}
		return Collections.unmodifiableList(_descendantNodeList);
	}

	public Iterator<INode> getSubtree() 
	{
		return new StartIterator(this, getDescendantNodeIterator());
	}

	public INodeCore getNodeCore() 
	{
		return this;
	}

	private boolean isNodeAncestor(INode node) 
	{
		if (node == null) 
		{
			return false;
		}

		INode ancestor = this;

		do 
		{
			if (ancestor == node) 
			{
				return true;
			}
		} 
		while ((ancestor = ancestor.getParentNode()) != null);

		return false;
	}

	private boolean isChildNode(INode node) 
	{
		if (node == null) 
		{
			return false;
		} 
		else 
		{
			if (getChildCount() == 0) 
			{
				return false;
			} 
			else 
			{
				return (node.getParentNode() == this && _childNodeList.indexOf(node) > -1);
			}
		}
	}

	public String getName() 
	{
		return _nodeName;
	}

	public void setName(String Name) 
	{
		_nodeName = Name;
	}

	public String getId() 
	{
		return _nodeId;
	}

	public void setId(String Id) 
	{
		_nodeId = Id;
	}

	public String getLabelConceptPredicate() 
	{
		return _labelConceptPredicate;
	}

	public void setLabelConceptPredicate(String LabelConceptPredicate) 
	{
		this._labelConceptPredicate = LabelConceptPredicate;
	}

	public String getNodeConceptPredicate() 
	{
		return _nodeConceptPredicate;
	}

	public void setNodeConceptPredicate(String NodeConceptPredicate) 
	{
		this._nodeConceptPredicate = NodeConceptPredicate;
	}

	public boolean getSource() 
	{
		return _sourceFlag;
	}

	public void setSource(boolean source) 
	{
		this._sourceFlag = source;
	}

	public ILabelConcept getConceptAt(int index) 
	{
		if (_conceptList == null) 
		{
			throw new ArrayIndexOutOfBoundsException("node has no Concepts");
		}
		return _conceptList.get(index);
	}

	public int getConceptCount() 
	{
		if (_conceptList == null) 
		{
			return 0;
		} 
		else 
		{
			return _conceptList.size();
		}
	}

	public int getConceptIndex(ILabelConcept concept) 
	{
		if (concept == null) 
		{
			throw new IllegalArgumentException("argument is null");
		}

		return _conceptList.indexOf(concept);
	}

	public Iterator<ILabelConcept> getConceptIterator() 
	{
		if (_conceptList == null) 
		{
			return Collections.<ILabelConcept>emptyList().iterator();
		} 
		else 
		{
			return _conceptList.iterator();
		}
	}

	public List<ILabelConcept> getConceptList() 
	{
		if (_conceptList == null) 
		{
			return Collections.emptyList();
		} 
		else 
		{
			return Collections.unmodifiableList(_conceptList);
		}
	}

	public ILabelConcept createConcept() 
	{
		return new LabelConcept();
	}

	public void addConcept(ILabelConcept concept) 
	{
		addConcept(getConceptCount(), concept);
	}

	public void addConcept(int index, ILabelConcept concept) 
	{
		if (concept == null) 
		{
			throw new IllegalArgumentException("new concept is null");
		}

		if (_conceptList == null) 
		{
			_conceptList = new ArrayList<ILabelConcept>();
		}
		_conceptList.add(index, concept);
	}

	public void removeConcept(int index) 
	{
		_conceptList.remove(index);
	}

	public void removeConcept(ILabelConcept concept) 
	{
		_conceptList.remove(concept);
	}

	public boolean isNodeRendered() 
	{
		return _rendition;
	}

	public void setNodeRendered(boolean rendition) 
	{
		this._rendition = rendition;
	}

	public boolean isSubtreeRendered() 
	{
		boolean treeRendition = _rendition;
		if (treeRendition) 
		{
			if (_childNodeList != null) 
			{
				for (INode child : _childNodeList) 
				{
					treeRendition = treeRendition && child.getNodeCore().isSubtreeRendered();
					if (!treeRendition) 
					{
						break;
					}
				}
			}
		}
		return treeRendition;
	}

	public String getDerivationInfo() 
	{
		return _origin;
	}

	public void setDerivationInfo(String origin) 
	{
		this._origin = origin;
	}

	public Object getInstance() 
	{
		return _objectInstance;
	}

	public void setInstance(Object instance) 
	{
		_objectInstance = instance;
	}


	public String toString() 
	{
		return _nodeName;
	}

	public int getIndex(TreeNode node) 
	{
		if (node instanceof INode) 
		{
			return getChildIndex((INode) node);
		} 
		else 
		{
			return -1;
		}
	}

	public Enumeration childNodeList() 
	{
		return Collections.enumeration(_childNodeList);
	}

	public void insert(MutableTreeNode child, int index) 
	{
		if (child instanceof INode) 
		{
			addChildNode(index, (INode) child);
		}
	}

	public void remove(int index) 
	{
		removeChildNode(index);
	}

	public void remove(MutableTreeNode node) 
	{
		if (node instanceof INode) 
		{
			removeChildNode((INode) node);
		}
	}

	public void setParentNode(MutableTreeNode parent) 
	{
		if (parent instanceof INode) 
		{
			setParentNode((Node) parent);
		}
	}

	public void trim() 
	{
		if (_conceptList != null) 
		{
			_conceptList.trimToSize();
			for (ILabelConcept concept : _conceptList) 
			{
				if (concept instanceof LabelConcept) 
				{
					((LabelConcept) concept).trim();
				}
			}
		}
		if (_childNodeList != null) 
		{
			_childNodeList.trimToSize();
			for (INode child : _childNodeList) 
			{
				((Node) child).trim();
			}
		}
	}

}
