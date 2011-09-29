/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.semantics;

import java.util.List;

/**
 * Interface for synsets.
 * @author Chiranjit Acharya
 */
public interface ISynset 
{
	/**
	 * Returns a synset gloss. 
	 * Gloss is the description of the meaning of the synset.
	 */
	String getGloss();

	/**
	 * Get lemmas of this synset.
	 */
	List<String> getLemmaList();

	/**
	 * Returns hypernyms of the synset.
	 * @throws SemanticsException
	 */
	List<ISynset> getHypernymList() throws SemanticsException;

	/**
	 * Returns hypernyms of the synset up to certain level.
	 *
	 * @param level
	 * @return hypernyms of the synset. 
	 * @throws SemanticsException
	 */
	List<ISynset> getHypernymList(int level) throws SemanticsException;

	/**
	 * Returns hyponyms of the synset.
	 *
	 * @return hyponyms of the synset. 
	 * @throws SemanticsException
	 */
	List<ISynset> getHyponymList() throws SemanticsException;

	/**
	 * Returns hyponyms of the synset down to certain level.
	 *
	 * @param level 
	 * @return hyponyms of the synset. 
	 * @throws SemanticsException
	 */
	List<ISynset> getHyponymList(int level) throws SemanticsException;
}

