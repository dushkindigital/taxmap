/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.map;

import java.util.AbstractSet;
import com.libereco.taxmap.symbolics.data.tree.IContext;


/**
 * Abstract relation class definition.
 * @author Chiranjit Acharya
 */
public abstract class Map<T> extends AbstractSet<IRelationInstance<T> > implements IContextRelation<T> 
{
	protected double _similarity;
	protected IContext _sourceContext;
	protected IContext _targetContext;

	public double getSimilarity() {
		return _similarity;
	}

	public void setSimilarity(double similarity) {
		this._similarity = similarity;
	}

	public IContext getSourceContext() {
		return _sourceContext;
	}

	public IContext getTargetContext() {
		return _targetContext;
	}
}
