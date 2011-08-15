package com.libereco.taxmap.symbolics.data.ling;

/**
 * Default dictionary meaning implementation.
 *
 */
public class Denotation implements IDenotation 
{
	char _pos;
	long _id;

	private Denotation() {}

	public Denotation(char pos, long id) 
	{
		this._pos = pos;
		this._id = id;
	}

	public char getDenotationPos() 
	{
		return _pos;
	}

	public void setDenotationPos(char pos) 
	{
		this._pos = pos;
	}

	public long getDenotationId() 
	{
		return _id;
	}

	public void setDenotationId(long id) 
	{
		this._id = id;
	}

	public String toString() 
	{
		return _pos + "#" + _id;
	}

	public boolean equals(Object that) 
	{
		if (this == that) 
			return true;
		if (!(that instanceof Denotation)) 
			return false;

		Denotation meaning = (Denotation)that;

		if (_id != meaning._id) 
			return false;
		if (_pos != meaning._pos) 
			return false;

		return true;
	}

	public int hash() 
	{
		int hashval = (int) _pos;
		hashval = 29 * hashval + (int) (_id ^ (_id >>> 31));
		return hashval;
	}
}

