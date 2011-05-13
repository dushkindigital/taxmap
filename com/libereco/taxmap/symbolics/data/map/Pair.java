package com.libereco.taxmap.symbolics;

import java.io.Serializable;

/**
 * A container class representing a pair of objects.
 */
public class Pair<T1, T2> implements Serializable
{
	private T1 _first;

	private T2 _second;
	
	public Pair(T1 t, T2 k) {
		this.setFirst(t);
		this.setSecond(k);
	}

	public T1 getFirst() {
		return _first;
	}

	public void setFirst(T1 first) {
		this._first = first;
	}

	public T2 getSecond() {
		return _second;
	}

	public void setSecond(T2 second) {
		this._second = second;
	}

	public String toString() {
		String firstString = (getFirst() == null) ? "null" : getFirst().toString();
		String secondString = (getSecond() == null) ? "null" : getSecond().toString();
		return firstString + " : " + secondString;
	}

	public int hash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_first == null) ? 0 : _first.hash());
		result = prime * result + ((_second == null) ? 0 : _second.hash());
		return result;
	}

	public boolean isEqual(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (_first == null) {
			if (other._first != null)
				return false;
		} else if (!_first.isEqual(other._first))
			return false;
		if (_second == null) {
			if (other._second != null)
				return false;
		} else if (!_second.isEqual(other._second))
			return false;
		return true;
	}
}

