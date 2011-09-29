/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.semantics;

import java.util.List;

import com.libereco.taxmap.symbolics.config.IEnvironment;
import com.libereco.taxmap.symbolics.data.ling.ISense;

/**
 * High-level interface to WordNet.
 * @author Chiranjit Acharya
 */
public interface ISemantics extends IEnvironment 
{
	/**
	 * Returns a synset given a sense
	 * @param sense 
	 * @return synset
	 * @throws SemanticsException 
	 */
	public ISynset getISynset(ISense sense) throws SemanticsException;

	/**
	 * Determines if lemmas of two words are equal
	 *
	 * @param word1 source string
	 * @param word2 target string
	 * @return true if lemmas are equal
	 * @throws SemanticsException 
	 */
	public boolean isEqual(String word1, String word2) throws SemanticsException;

	/**
	 * Returns all senses of a word.
	 *
	 * @param word 
	 * @return word senses
	 * @throws SemanticsException 
	 */
	public List<ISense> getSenseList(String word) throws SemanticsException;

	/**
	 * Returns base form (lemma) of a word.
	 *
	 * @param word 
	 * @return base form
	 * @throws SemanticsException 
	 */
	public String getBaseForm(String word) throws SemanticsException;
}

