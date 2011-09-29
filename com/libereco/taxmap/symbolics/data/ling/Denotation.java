/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.ling;

/**
 * Default dictionary meaning implementation.
 * @author Chiranjit Acharya
 */
public class Denotation implements IDenotation 
{
	char _position;
	long _id;

	private Denotation() {}

	public Denotation(char pos, long id) 
	{
		this._position = pos;
		this._id = id;
	}

	public char getPosition() 
	{
		return _position;
	}

	public void setPosition(char pos) 
	{
		this._position = pos;
	}

	public long getId() 
	{
		return _id;
	}

	public void setId(long id) 
	{
		this._id = id;
	}

	public String toString() 
	{
		return _position + "#" + _id;
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
		if (_position != meaning._position) 
			return false;

		return true;
	}

	public int hash() 
	{
		int hashval = (int) _position;
		hashval = 29 * hashval + (int) (_id ^ (_id >>> 31));
		return hashval;
	}
}

