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

	public Node() 
	{
		_parentNode = null;
		_childNodes = null;
		_ancestorNodes = null;
		_ancestorNodeCount = -1;
		_descendantNodes = null;
		_descendantNodeCount = -1;

		_sourceFlag = false;
		// need to set node id to keep track of concepts in predicates
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
	}

	/**
	 * Constructor class which sets the node name.
	 */
	public Node(String name) 
	{
		this();
		this._nodeName = name;
	}

	// iterator which iterates over all parent nodes

	private static final class Ancestors implements Iterator<INode> 
	{
		private INode current;

		public Ancestors(INode start) 
		{
			if (null == start) 
			{
				throw new IllegalArgumentException("argument is null");
			}
			this.current = start;
		}

		public boolean hasNext() 
		{
			return current.hasParent();
		}

		public INode next() 
		{
			current = current.getParent();
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
			if (null == start) 
			{
				throw new IllegalArgumentException("argument is null");
			}
			this.start = start;
			this.i = i;
		}

		public boolean hasNext() 
		{
			return (null != start || i.hasNext());
		}

		public INode next() 
		{
			INode result = start;
			if (null != start) 
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
			if (null == start) 
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

	public INode getChildAt(int index) 
	{
		if (_childNodes == null) 
		{
			throw new ArrayIndexOutOfBoundsException("node has no childNodes");
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

}
