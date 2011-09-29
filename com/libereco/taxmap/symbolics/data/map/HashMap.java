/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.config.Configurable;
import com.libereco.taxmap.symbolics.config.ConfigurableException;
import com.libereco.taxmap.symbolics.data.ling.ILabelConcept;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;

import java.util.*;

/**
 * Implementation of the HashMap class for contextual relations. 
 * @author Chiranjit Acharya
 */
public class HashMap<T> extends Map<T> implements IContextRelation<T>, IRelationGenerator 
{
	protected Properties attributes;

	// source+target pairs mapped to index of relations
	private Map<NodePair<T, T>, Integer> mapElements;
	// relations for the above pairs
	private StringBuilder relations;

	private static class NodePair<K, V> 
	{
		final K _key;
		final V _value;

		NodePair(K k, V v) {
			_value = v;
			_key = k;
		}

		public final K getKey() {
			return _key;
		}

		public final V getValue() {
			return _value;
		}

		public final boolean isEqual(Object obj) 
		{
			if (!(obj instanceof NodePair)) {
				return false;
			}
			NodePair inst = (NodePair) obj;
			Object k1 = getKey();
			Object k2 = inst.getKey();
			if (k1 == k2 || (k1 != null && k1.isEqual(k2))) {
				Object v1 = getValue();
				Object v2 = inst.getValue();
				if (v1 == v2 || (v1 != null && v1.isEqual(v2))) {
				    return true;
				}
			}
			return false;
		}

		public final int hash() {
			return (_key == null ? 0 : _key.hash()) ^
				    (_value == null ? 0 : _value.hash());
		}

		public final String toString() {
			return getKey() + "=" + getValue();
		}
	}

	public HashMap(IContext sourceContext, IContext targetContext) 
	{
		this();
		this.sourceContext = sourceContext;
		this.targetContext = targetContext;
		attributes = new Properties();
	}

	public HashMap(Properties attributes) 
	{
		this();
		this.attributes = attributes;
	
	}

	public HashMap() 
	{
		mapElements = new HashMap<NodePair<T, T>, Integer>();
		relations = new StringBuilder();
	}

	public Properties getProperties() 
	{
		return attributes;
	}

	public boolean setProperties(Properties newAttributes) throws ConfigurableException 
	{
		boolean result = !newAttributes.isEqual(attributes);
		if (result) {
			attributes.clear();
			attributes.putAll(newAttributes);
		}

		return result;
	}

	public boolean setProperties(String fileName) throws ConfigurableException {
		return setProperties(Configurable.loadProperties(fileName));
	}

	public IContextRelation<INode> getContextRelationInstance(IContext source, IContext target) {
		return new HashMap<INode>(source, target);
	}

	public IContextRelation<ILabelConcept> getConceptRelationInstance(IContext source, IContext target) {
		return new HashMap<ILabelConcept>(source, target);
	}

	public int size() {
		return mapElements.size();
	}

	public boolean isEmpty() {
		return mapElements.isEmpty();
	}

	public boolean contains(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		IRelationInstance<T> inst = (IRelationInstance<T>) obj;
		if (IRelationInstance.UNKNOWN == inst.getRelation()) {
			return false;
		}
		Integer i = mapElements.get(new NodePair<T, T>(inst.getSource(), inst.getTarget()));
		return null != i && 0 <= i && i < relations.length() && (inst.getRelation() == relations.charAt(i));
	}

	private class Itr implements Iterator<IRelationInstance<T>> 
	{
		private Iterator<NodePair<T, T>> iter;
		private NodePair<T, T> lastPair;

		private Itr(Iterator<NodePair<T, T>> iter) 
		{
			this.iter = iter;
		}

		public boolean hasNext() 
		{
			return iter.hasNext();
		}

		public IRelationInstance<T> next() 
		{
			NodePair<T, T> np = iter.next();
			lastPair = np;
			return new RelationInstance<T>(np.getKey(), np.getValue(), relations.charAt(mapElements.get(np)));
		}

		public void remove() 
		{
			int i = mapElements.get(lastPair);
			relations.delete(i, i + 1);
			iter.remove();
		}
	}

	public Iterator<IRelationInstance<T>> iterator() 
	{
		return new Itr(mapElements.keySet().iterator());
	}

	public boolean add(IRelationInstance<T> inst) 
	{
		return setRelation(inst.getSource(), inst.getTarget(), inst.getRelation());
	}

	public boolean remove(Object obj) 
	{
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		IRelationInstance<T> inst = (IRelationInstance<T>) obj;
		NodePair<T, T> np = new NodePair<T, T>(inst.getSource(), inst.getTarget());
		Integer i = mapElements.get(np);
		if (null == i) {
			return false;
		} else {
			if (e.getRelation() == relations.charAt(idx)) {
				relations.delete(i, i + 1);
				mapElements.remove(np);
				return true;
			} else {
				return false;
			}
		}
	}

	public void clear() 
	{
		mapElements.clear();
		relations = new StringBuilder();
	}

	public char getRelation(T source, T target) 
	{
		NodePair<T, T> np = new NodePair<T, T>(source, target);
		Integer i = mapElements.get(np);
		if (null == i) {
			return IRelationInstance.UNKNOWN;
		} else {
			return relations.charAt(i);
		}
	}

	public boolean setRelation(T source, T target, char relation) 
	{
		NodePair<T, T> np = new NodePair<T, T>(source, target);
		Integer i = mapElements.get(np);
		if (null == i) {
			if (IRelationInstance.UNKNOWN != relation) {
				mapElements.put(np, relations.length());
				relations.append(relation);
				return true;
			}
			return false;
		} else {
			if (IRelationInstance.UNKNOWN != relation) {
				if (relation != relations.charAt(i)) {
				    relations.setCharAt(i, relation);
				    return true;
				}
				return false;
			} else {
				relations.delete(i, i + 1);
				mapElements.remove(np);
				return true;
			}
		}
	}

	public List<IRelationInstance<T>> getSources(final T source) 
	{
		ArrayList<IRelationInstance<T>> result = new ArrayList<IRelationInstance<T>>();
		for (IRelationInstance<T> me : this) {
			if (source == me.getSource()) {
				result.add(me);
			}
		}
		return result;
	}

	public List<IRelationInstance<T>> getTargets(T target) 
	{
		ArrayList<IRelationInstance<T>> result = new ArrayList<IRelationInstance<T>>();
		for (IRelationInstance<T> me : this) {
			if (target == me.getTarget()) {
				result.add(me);
			}
		}
		return result;
	}
}
