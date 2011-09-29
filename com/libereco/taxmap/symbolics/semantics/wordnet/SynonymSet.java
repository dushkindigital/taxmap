/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.semantics.wordnet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.libereco.taxmap.symbolics.semantics.ISynset;
import com.libereco.taxmap.symbolics.semantics.SemanticsException;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import org.apache.log4j.Logger;

/**
 * Implementation of WordNet characterized synset class.
 * @author Chiranjit Acharya
 */
public class SynonymSet implements ISynset 
{
	private static final Logger log = Logger.getLogger(SynonymSet.class);

	private Synset _synset;

	/**
	 * Constructor class with synset input.
	 * @param synset
	 */
	public SynonymSet(Synset synset) 
	{
		_synset = synset;
	}


	public String getGloss() 
	{
		return _synset.getGloss();
	}

	public List<String> getLemmaList() 
	{
		List<String> strList = new ArrayList<String>();
		String lemma;

		for (int i = 0; i < _synset.getWordsSize(); i++) 
		{
			lemma = _synset.getWord(i).getLemma();
			strList.add(lemma);
		}

		return strList;
	}

	public List<ISynset> getHypernymList() throws SemanticsException 
	{
		List<ISynset> senseList = new ArrayList<ISynset>();
		try 
		{
			PointerUtils senseHandle = PointerUtils.getInstance();
			PointerTargetTree hypernymTree = senseHandle.getHypernymTree(_synset, 1);
			for (Iterator listIterator = hypernymTree.toList().iterator(); listIterator.hasNext();) 
			{
				if (listIterator.hasNext()) 
				{
					for (Object object : ((PointerTargetNodeList) listIterator.next())) 
					{
						Synset synset = ((PointerTargetNode) object).getSynset();
						if (!isEqual(_synset, synset)) 
						{
							senseList.add(new SynonymSet(synset));
						}
					}
				}
			}
		} 
		catch (JWNLException exception) 
		{
			final String errstr = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			log.error(errstr, exception);
			throw new SemanticsException(errstr, exception);
		}
		return senseList;
	}

	public List<ISynset> getHypernymList(int level) throws SemanticsException 
	{
		List<ISynset> senseList = new ArrayList<ISynset>();
		try 
		{
			PointerUtils senseHandle = PointerUtils.getInstance();
			PointerTargetTree hypernymTree = senseHandle.getHypernymTree(_synset, level);
			for (Iterator listIterator = hypernymTree.toList().iterator(); listIterator.hasNext();) 
			{
				if (listIterator.hasNext()) 
				{
					for (Object object : ((PointerTargetNodeList) listIterator.next())) 
					{
						Synset synset = ((PointerTargetNode) object).getSynset();
						if (!isEqual(_synset, synset)) 
						{
							senseList.add(new SynonymSet(synset));
						}
					}
				}
			}
		} 
		catch (JWNLException exception) 
		{
			final String errstr = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			log.error(errstr, exception);
			throw new SemanticsException(errstr, exception);
		}
		return senseList;
	}

	public List<ISynset> getHyponymList() throws SemanticsException 
	{
		List<ISynset> senseList = new ArrayList<ISynset>();
		try 
		{
			PointerUtils senseHandle = PointerUtils.getInstance();
			PointerTargetTree hyponymTree = senseHandle.getHyponymTree(_synset, 1);
			for (Iterator listIterator = hyponymTree.toList().iterator(); listIterator.hasNext();) 
			{
				if (listIterator.hasNext()) 
				{
					for (Object object : ((PointerTargetNodeList) listIterator.next())) 
					{
						Synset synset = ((PointerTargetNode) object).getSynset();
						if (!isEqual(_synset, synset)) 
						{
							senseList.add(new SynonymSet(synset));
						}
					}
				}
			}
		} 
		catch (JWNLException exception) 
		{
			final String errstr = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			log.error(errstr, exception);
			throw new SemanticsException(errstr, exception);
		}
		return senseList;
	}

	public List<ISynset> getHyponymList(int level) throws SemanticsException 
	{
		List<ISynset> senseList = new ArrayList<ISynset>();
		try 
		{
			PointerUtils senseHandle = PointerUtils.getInstance();
			PointerTargetTree hyponymTree = senseHandle.getHyponymTree(_synset, level);
			for (Iterator listIterator = hyponymTree.toList().iterator(); listIterator.hasNext();) 
			{
				if (listIterator.hasNext()) 
				{
					for (Object object : ((PointerTargetNodeList) listIterator.next())) 
					{
						Synset synset = ((PointerTargetNode) object).getSynset();
						if (!isEqual(_synset, synset)) 
						{
							senseList.add(new SynonymSet(synset));
						}
					}
				}
			}
		} 
		catch (JWNLException exception) 
		{
			final String errstr = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			log.error(errstr, exception);
			throw new SemanticsException(errstr, exception);
		}
		return senseList;
	}

	public boolean isEqual(Synset sourceSense, Synset targetSense) 
	{
		long sourceOffset = sourceSense.getOffset();
		long targetOffset = targetSense.getOffset();
		String sourcePOS = sourceSense.getPOS().toString();
		String targetPOS = targetSense.getPOS().toString();
		return ((sourcePOS.equals(targetPOS)) && (sourceOffset == targetOffset));
	}

	public boolean equals(Object object) 
	{
		if (this == object) return true;
		if (!(object instanceof SynonymSet)) return false;

		final SynonymSet sense = (SynonymSet) object;

		if (_synset != null ? !_synset.equals(sense._synset) : sense._synset != null) return false;

		return true;
	}

	public int hashCode() 
	{
		return (_synset != null ? _synset.hashCode() : 0);
	}
}

