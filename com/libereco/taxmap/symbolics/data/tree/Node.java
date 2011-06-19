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
 */
public class Node extends IndexedObject implements INode, INodeCore 
{
	protected INode _parentNode;
	protected ArrayList<INode> _childNodes;
	protected ArrayList<INode> _ancestorNodes;
	protected int _ancestorNodeCount;
	protected ArrayList<INode> _descendantNodes;
	protected int _descendantNodeCount;

	// id is needed to store NodeConceptPredicates correctly.
	protected String _nodeId;
	protected String _nodeName;

	// LabelConceptPredicates refer to tokens which should have unique id within a context
	protected String _labelConceptPredicate;

	// NodeConceptPredicate is made of LabelConceptPredicates,
	protected String _nodeConceptPredicate;

	// source taxonomy tree
	protected boolean _sourceFlag;

	protected ArrayList<ILabelConcept> _conceptList;

	// node counter to set unique node id during creation
	protected static long _cardinality = 0;

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
			if ( == null start) 
			{
				throw new IllegalArgumentException("argument is null");
			}
			this.start = start;
			this.i = i;
		}

		public boolean hasNext() 
		{
			return ( != null start || i.hasNext());
		}

		public INode next() 
		{
			INode result = start;
			if ( != null start) 
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
			if ( == null start) 
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
			for (Iterator<INode> i = current.getChildren(); i.hasNext();) 
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
		_childNodes = null;
		_ancestorNodes = null;
		_ancestorNodeCount = -1;
		_descendantNodes = null;
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
		provenance = null;
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
		if (_childNodes == null) 
		{
			throw new ArrayIndexOutOfBoundsException("node has no _childNodes");
		}
		return _childNodes.get(index);
	}

	public int getChildCount() 
	{
		if (_childNodes == null) 
		{
			return 0;
		} 
		else 
		{
			return _childNodes.size();
		}
	}

	public int getChildIndex(INode child) 
	{
		if (child == null) 
		{
			throw new IllegalArgumentException("argument is null");
		}

		if (!isChildNode(child)) 
		{
			return -1;
		}
		return _childNodes.indexOf(child);
	}

	public Iterator<INode> getChildren() 
	{
		if (_childNodes == null) 
		{
			return Collections.<INode>emptyList().iterator();
		} 
		else 
		{
			return _childNodes.iterator();
		}
	}

	public List<INode> getChildrenList() 
	{
		if (_childNodes != null) 
		{
			return Collections.unmodifiableList(_childNodes);
		} 
		else 
		{
			return Collections.emptyList();
		}
	}

	public INode createChild() 
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
		if (_childNodes == null) 
		{
			_childNodes = new ArrayList<INode>();
		}
		_childNodes.add(index, node);
		fireTreeStructureChanged(this);
	}

	public void removeChildNode(int index) 
	{
		INode child = getChildAt(index);
		_childNodes.remove(index);
		fireTreeStructureChanged(this);
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

	public boolean isLeaf() 
	{
		return getChildCount() == 0;
	}

	public int getAncestorCount() 
	{
		if (_ancestorNodeCount == -1) 
		{
			if (_ancestorNodes == null) 
			{
				_ancestorNodeCount = 0;
				if (_parentNode != null) 
				{
					_ancestorNodeCount = _parentNode.getAncestorCount() + 1;
				}
			} 
			else 
			{
				_ancestorNodeCount = _ancestorNodes.size();
			}
		}
		return _ancestorNodeCount;
	}

	public Iterator<INode> getAncestors() 
	{
		return new Ancestors(this);
	}

	public List<INode> getAncestorsList() 
	{
		if (_ancestorNodes == null) 
		{
			_ancestorNodes = new ArrayList<INode>(getAncestorCount());
			if (_parentNode != null) 
			{
				_ancestorNodes.add(_parentNode);
				_ancestorNodes.addAll(_parentNode.getAncestorsList());
			}
		}
		return Collections.unmodifiableList(_ancestorNodes);
	}

	public int getLevel() 
	{
		return getAncestorCount();
	}

	public int getDescendantCount() 
	{
		if (_descendantNodeCount == -1) 
		{
			if (_descendantNodes == null) 
			{
				_descendantNodeCount = 0;
				for (Iterator<INode> i = getDescendants(); i.hasNext();) 
				{
					i.next();
					_descendantNodeCount++;
				}
			} 
			else 
			{
				_descendantNodeCount = _descendantNodes.size();
			}
		}
		return _descendantNodeCount;
	}

	public Iterator<INode> getDescendants() 
	{
		return new BreadthFirstSearch(this);
	}

	public List<INode> getDescendantsList() 
	{
		if (_descendantNodes == null) 
		{
			_descendantNodes = new ArrayList<INode>(getChildCount());
			if (_childNodes != null) 
			{
				_descendantNodes.addAll(_childNodes);
				for (INode child : _childNodes) 
				{
					_descendantNodes.addAll(child.getDescendantsList());
				}
				_descendantNodes.trimToSize();
			}
		}
		return Collections.unmodifiableList(_descendantNodes);
	}

	public Iterator<INode> getSubtree() 
	{
		return new StartIterator(this, getDescendants());
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
				return (node.getParentNode() == this && _childNodes.indexOf(node) > -1);
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

}
