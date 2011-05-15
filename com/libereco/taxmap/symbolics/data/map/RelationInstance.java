package com.libereco.taxmap.symbolics.data.map;

/**
 * Implementation of the relation instance.
 */
public class RelationInstance<T> implements IRelationInstance<T> 
{
	protected T _source;
	protected T _target;
	protected char _relation;

	public RelationInstance(T source, T target, char relation) 
	{
		this._source = source;
		this._target = target;
		this._relation = relation;
	}

	public T getSource() 
	{
		return _source;
	}

	public T getTarget() 
	{
		return _target;
	}

	public char getRelation() 
	{
		return _relation;
	}

	public int hash() 
	{
		int result;
		result = (_source != null ? _source.hash() : 0);
		result = 31 * result + (_target != null ? _target.hash() : 0);
		result = 31 * result + (int) _relation;
		return result;
	}

	public boolean isEqual(Object obj) 
	{
		if (this == obj) {
		    return true;
		}
		if (obj == null) {
		    return false;
		}
		if (getClass() != obj.getClass()) {
		    return false;
		}

		RelationInstance<T> that = (RelationInstance<T>) obj;

		if (_relation != that._relation) {
		    return false;
		}
		if (_source != null ? !_source.isEqual(that._source) : that._source != null) {
		    return false;
		}
		if (_target != null ? !_target.isEqual(that._target) : that._target != null) {
		    return false;
		}

		return true;
	}
}
