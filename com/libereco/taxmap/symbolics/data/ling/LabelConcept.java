package com.libereco.taxmap.symbolics.data.ling;

import com.libereco.taxmap.symbolics.data.IndexedObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents atomic concept of label as a
 * concept label and list of associated senses in WordNet.
 *
 */
public class LabelConcept extends IndexedObject implements ILabelConcept 
{
	private int _id;
	private String _token;
	private String _lemma;

	private ArrayList<ISense> _senses;
	private static final Iterator<ISense> VOID_SENSE_ITERATOR = Collections.<ISense>emptyList().iterator();

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

	public ISense getSenseAt(int index) 
	{
		if (_senses == null) 
		{
			throw new ArrayIndexOutOfBoundsException("Concept has no sense");
		}
		return _senses.get(index);
	}

	public int getSenseCount() 
	{
		if (_senses == null) 
		{
			return 0;
		} 
		else 
		{
			return _senses.size();
		}
	}

	public int getSenseIndex(ISense sense) 
	{
		if (sense == null) 
		{
			throw new IllegalArgumentException("Argument is null");
		}

		if (_senses == null) 
		{
			return -1;
		}

		return _senses.indexOf(sense);
	}

	public Iterator<ISense> getSenses() 
	{
		if (_senses == null) 
		{
			return VOID_SENSE_ITERATOR;
		} 
		else 
		{
			return _senses.iterator();
		}
	}

	public List<ISense> getSenseList() 
	{
		if (_senses == null) 
		{
			return Collections.unmodifiableList(_senses);
		} 
		else 
		{
			return Collections.emptyList();
		}
	}

	public ISense createSense(char pos, long id) 
	{
		ISense sense = new Sense(pos, id);
		addSense(sense);
		return sense;
	}

	public void addSense(ISense sense) 
	{
		addSense(getSenseCount(), sense);
	}

	public void addSense(int index, ISense sense) 
	{
		if (sense == null) 
		{
			throw new IllegalArgumentException("New sense is null");
		}

		if (_senses == null) 
		{
			_senses = new ArrayList<ISense>();
		}

		if (_senses.indexOf(sense) == -1) 
		{
			_senses.add(index, sense);
		}
	}

	public void removeSense(int index) 
	{
		// checks children and throws exception in case
		getSenseAt(index);
		_senses.remove(index);
	}

	public void removeSense(ISense sense) 
	{
		if (sense == null) 
		{
			throw new IllegalArgumentException("Argument is null");
		}

		removeSense(getSenseIndex(sense));
	}

	public void trim() 
	{
		if (_senses == null) 
		{
			_senses.trimToSize();
		}
	}

	public String toString() 
	{
		return _token;
	}
}

