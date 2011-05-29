package com.libereco.taxmap.symbolics.data.map;

import com.libereco.taxmap.symbolics.config.Configurable;
import com.libereco.taxmap.symbolics.config.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import com.libereco.taxmap.symbolics.data.matrix.IIndexedObject;
import com.libereco.taxmap.symbolics.data.matrix.ITaxMapMatrix;
import com.libereco.taxmap.symbolics.data.matrix.ITaxMapGenerator;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;

import java.util.*;

/**
 * represents a mapping using a bilinear data structure. 
 */
public class BilinearMap<T extends IIndexedObject> extends Map<T> implements IContextRelation<T>, IRelationGenerator 
{
	private static String TAXMAP_GENERATOR_KEY = "TaxMapGenerator";
	protected ITaxMapGenerator generator;

	protected Properties attributes;
	protected ITaxMapMatrix bimap;

	private int predicateNum;

	private T[] sourceObjects;
	private T[] targetObjects;

	private volatile transient int InstanceUpdateNum;

	private class BilinearMapIterator implements Iterator<IRelationInstance<T>> 
	{
		private int MeanUpdateNum;
		private int thisRow;
		private int thisCol;
		private IRelationInstance<T> next;
		private IRelationInstance<T> current;

		private BilinearMapIterator() 
		{
			this.MeanUpdateNum = InstanceUpdateNum;
			if (0 == size()) 
			{
				next = null;
			} 
			else 
			{
				thisRow = -1;
				thisCol = bimap.getColNum() - 1;
				next = findNext();
			}
		}

		public boolean hasNext() 
		{
			return null != next;
		}

		public IRelationInstance<T> next() 
		{
			if (InstanceUpdateNum != MeanUpdateNum) 
			{
				throw new ConcurrentModificationException();
			}
			if (null == next) 
			{
				throw new NoSuchElementException();
			}

			current = next;
			next = findNext();
			return current;
		}

		public void remove() 
		{
			if (null == current) 
			{
				throw new IllegalStateException();
			}
			if (InstanceUpdateNum != MeanUpdateNum) 
			{
				throw new ConcurrentModificationException();
			}
			setRelation(current.getSource(), current.getTarget(), IRelationInstance.UNKNOWN);
			MeanUpdateNum = InstanceUpdateNum;
			current = null;
		}

		private IRelationInstance<T> findNext() 
		{
			IRelationInstance<T> result = null;
			char relation = IRelationInstance.UNKNOWN;
			do 
			{
				thisCol++;
				if (bimap.getColNum() == thisCol) 
				{
					thisRow++;
					thisCol = 0;
				}
			}
			while (thisRow < bimap.getRowNum() && thisCol < bimap.getColNum() && IRelationInstance.UNKNOWN == (relation = bimap.get(thisRow, thisCol)));

			if (IRelationInstance.UNKNOWN != relation) 
			{
				result = new RelationInstance<T>(sourceObjects[thisRow], targetObjects[thisCol], relation);
			}
			return result;
		}
	}

	public BilinearMap() 
	{
		attributes = new Properties();
	}

	public BilinearMap(Properties attributes) 
	{
		this.attributes = attributes;
	}

	public BilinearMap(ITaxMapGenerator generator) 
	{
		this.generator = generator;
	}

	public BilinearMap(ITaxMapGenerator generator, IContext sourceContext, IContext targetContext) 
	{
		this.sourceContext = sourceContext;
		this.targetContext = targetContext;
		this.generator = generator;
		bimap = generator.getInstance();

		int rowNum = getRowCount(sourceContext);
		int colNum = getColCount(targetContext);
		bimap.init(rowNum, colNum);

		sourceObjects = (T[]) new IIndexedObject[rowNum];
		targetObjects = (T[]) new IIndexedObject[colNum];

		initRow(sourceContext, sourceObjects);
		initColumn(targetContext, targetObjects);

		predicateNum = 0;
		InstanceUpdateNum = 0;
	}

	protected void initColumn(IContext targetContext, IIndexedObject[] targetObjects) 
	{ }

	protected void initRow(IContext sourceContext, IIndexedObject[] sourceObjects) 
	{ }

	public Properties getAttributes() 
	{
		return attributes;
	}

	public boolean setAttributes(Properties newAttributes) throws ConfigurableException 
	{
		boolean similarAttribute = !newAttributes.equals(attributes);
		if (similarAttribute) 
		{
			if (newAttributes.containsKey(TAXMAP_GENERATOR_KEY)) 
			{
				generator = (ITaxMapGenerator) Configurable.configureComponent(generator, attributes, newAttributes, "bilinear map generator", TAXMAP_GENERATOR_KEY, ITaxMapGenerator.class);
			} 
			else 
			{
				String errorString = "TaxMap configuration key not found" + TAXMAP_GENERATOR_KEY;
				throw new ConfigurableException(errorString);
			}

			attributes.clear();
			attributes.putAll(newAttributes);
		}
		return similarAttribute;
	}

	public boolean setAttributes(String fileName) throws ConfigurableException 
	{
		return setAttributes(Configurable.loadProperties(fileName));
	}

	public char getRelation(T source, T target) 
	{
		return bimap.get(source.getIndex(), target.getIndex());
	}

	public boolean setRelation(T source, T target, char relation) 
	{
		boolean infomatch =
				source == sourceObjects[source.getIndex()] &&
				target == targetObjects[target.getIndex()] &&
				relation == bimap.get(source.getIndex(), target.getIndex());

		if (!infomatch) 
		{
			if (source == sourceObjects[source.getIndex()] && target == targetObjects[target.getIndex()]) 
			{
				InstanceUpdateNum++;
				bimap.set(source.getIndex(), target.getIndex(), relation);
				if (IRelationInstance.UNKNOWN == relation) 
				{
					predicateNum--;
				} 
				else 
				{
					predicateNum++;
				}
			}
		}

		return !infomatch;
	}

	public List<IRelationInstance<T>> getSources(T source) 
	{
		int i = source.getIndex();
		if (i >= 0 && i < sourceObjects.length && (source == sourceObjects[i])) 
		{
			ArrayList<IRelationInstance<T>> relationSet = new ArrayList<IRelationInstance<T>>();
			for (int j = 0; j < targetObjects.length; j++) 
			{
				char relation = bimap.get(i, j);
				if (IRelationInstance.UNKNOWN != relation) 
				{
					relationSet.add(new RelationInstance<T>(sourceObjects[i], targetObjects[j], relation));
				}
			}
			return relationSet;
		} 
		else 
		{
			return Collections.emptyList();
		}
	}

	public List<IRelationInstance<T>> getTargets(T target) 
	{
		int j = target.getIndex();
		if (j >= 0  && j < targetObjects.length && (target == targetObjects[j])) 
		{
			ArrayList<IRelationInstance<T>> relationSet = new ArrayList<IRelationInstance<T>>();
			for (int i = 0; i < sourceObjects.length; i++) 
			{
				char relation = bimap.get(i, j);
				if (IRelationInstance.UNKNOWN != relation) 
				{
					relationSet.add(new RelationInstance<T>(sourceObjects[i], targetObjects[j], relation));
				}
			}
			return relationSet;
		} 
		else 
		{
			return Collections.emptyList();
		}
	}

	public int size() 
	{
		return predicateNum;
	}

	public boolean isEmpty() 
	{
		return predicateNum == 0;
	}

	public boolean contains(Object obj) 
	{
		boolean relationFound = false;
		if (obj instanceof IRelationInstance) 
		{
			IRelationInstance inst = (IRelationInstance) obj;
			if (inst.getSource() instanceof IIndexedObject) 
			{
				T s = (T) inst.getSource();
				if (inst.getTarget() instanceof IIndexedObject) 
				{
					T t = (T) inst.getTarget();
					relationFound = IRelationInstance.UNKNOWN != getRelation(s, t) && s == sourceObjects[s.getIndex()] && t == targetObjects[t.getIndex()];
				}
			}
		}
		return relationFound;
	}

	public Iterator<IRelationInstance<T>> iterator() 
	{
		return new BilinearMapIterator();
	}

	public boolean add(IRelationInstance<T> inst) 
	{
		return setRelation(inst.getSource(), inst.getTarget(), inst.getRelation());
	}

	public boolean remove(Object obj) 
	{
		boolean relationFound = false;
		if (obj instanceof IRelationInstance) 
		{
			IRelationInstance inst = (IRelationInstance) obj;
			if (inst.getSource() instanceof IIndexedObject) 
			{
				T s = (T) inst.getSource();
				if (inst.getTarget() instanceof IIndexedObject) 
				{
					T t = (T) inst.getTarget();
					relationFound = setRelation(s, t, IRelationInstance.UNKNOWN);
				}
			}
		}

		return relationFound;
	}

	public void clear() 
	{
		int rowNum = bimap.getRowNum();
		int colNum = bimap.getColNum();
		bimap.init(rowNum, colNum);

		predicateNum = 0;
	}

	public IContextRelation<INode> getContextRelationInstance(IContext source, IContext target) 
	{
		return new NodeMap(generator, source, target);
	}

	public IContextRelation<IAtomicConceptOfLabel> getConceptRelationInstance(IContext source, IContext target) 
	{
		return new ConceptMap(generator, source, target);
	}

	protected int getColumnCount(IContext context) 
	{
		return -1;
	}

	protected int getRowCount(IContext context) 
	{
		return -1;
	}
}
