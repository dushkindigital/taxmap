package com.libereco.taxmap.symbolics.data.ling;

import com.libereco.taxmap.symbolics.data.IndexedObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents atomic concept of label as a
 * concept label and list of associated meaningList in WordNet.
 *
 */
public class LabelConcept extends IndexedObject implements ILabelConcept 
{
	private int _id;
	private String _token;
	private String _lemma;

	private ArrayList<IDenotation> _meaningList;
	private static final Iterator<IDenotation> VOID_SENSE_ITERATOR = Collections.<IDenotation>emptyList().iterator();

	public LabelConcept() {}

	/**
	 * Constructor class which sets the id, token and lemma.
	 *
	 * @param id    id of token
	 * @param token token
	 * @param lemma lemma
	 */
	public LabelConcept(int id, String token, String lemma) 
	{
		this._id = id;
		this._token = token;
		this._lemma = lemma;
	}

	public String getToken() 
	{
		return _token;
	}

	public void setToken(String token) 
	{
		this._token = token;
	}

	public String getLemma() 
	{
		return _lemma;
	}

	public void setLemma(String lemma) 
	{
		this._lemma = lemma;
	}

	public int getId() 
	{
		return _id;
	}

	public void setId(int id) 
	{
		this._id = id;
	}

	public IDenotation getDenotationAt(int index) 
	{
		if (_meaningList == null) 
		{
			throw new ArrayIndexOutOfBoundsException("Concept has no meaning");
		}
		return _meaningList.get(index);
	}

	public int getDenotationCount() 
	{
		if (_meaningList == null) 
		{
			return 0;
		} 
		else 
		{
			return _meaningList.size();
		}
	}

	public int getDenotationIndex(IDenotation meaning) 
	{
		if (meaning == null) 
		{
			throw new IllegalArgumentException("Argument is null");
		}

		if (_meaningList == null) 
		{
			return -1;
		}

		return _meaningList.indexOf(meaning);
	}

	public Iterator<IDenotation> getDenotations() 
	{
		if (_meaningList == null) 
		{
			return VOID_SENSE_ITERATOR;
		} 
		else 
		{
			return _meaningList.iterator();
		}
	}

	public List<IDenotation> getDenotationList() 
	{
		if (_meaningList == null) 
		{
			return Collections.unmodifiableList(_meaningList);
		} 
		else 
		{
			return Collections.emptyList();
		}
	}

	public IDenotation createDenotation(char pos, long id) 
	{
		IDenotation meaning = new Denotation(pos, id);
		addDenotation(meaning);
		return meaning;
	}

	public void addDenotation(IDenotation meaning) 
	{
		addDenotation(getDenotationCount(), meaning);
	}

	public void addDenotation(int index, IDenotation meaning) 
	{
		if (meaning == null) 
		{
			throw new IllegalArgumentException("New meaning is null");
		}

		if (_meaningList == null) 
		{
			_meaningList = new ArrayList<IDenotation>();
		}

		if (_meaningList.indexOf(meaning) == -1) 
		{
			_meaningList.add(index, meaning);
		}
	}

	public void removeDenotation(int index) 
	{
		// checks children and throws exception in case
		getDenotationAt(index);
		_meaningList.remove(index);
	}

	public void removeDenotation(IDenotation meaning) 
	{
		if (meaning == null) 
		{
			throw new IllegalArgumentException("Argument is null");
		}

		removeDenotation(getDenotationIndex(meaning));
	}

	public void trim() 
	{
		if (_meaningList == null) 
		{
			_meaningList.trimToSize();
		}
	}

	public String toString() 
	{
		return _token;
	}
}

