package com.libereco.taxmap.symbolics.data.ling;

import java.util.Iterator;
import java.util.List;

import com.libereco.taxmap.symbolics.data.IIndexedObject;

/**
 * An interface for the label concept. 
 * The concept of a label corresponds to the natural language token.
 * In case of unigram tokens, the correspondence is one-to-one; 
 * In case of multigram tokens, the entire n-gram can be represented as a singlular concept.
 *
 */
public interface ILabelConcept extends IIndexedObject
{
	/**
	 * @return the token(s) corresponding to the atomic concept
	 */
	String getToken();

	/**
	 * Sets the token(s) corresponding to the atomic concept.
	 * @param token token(s) corresponding to the atomic concept.
	 */
	void setToken(String token);

	/**
	 * @return lemmatized version of the token(s)
	 */
	String getLemma();

	/**
	 * Sets lemmatized version of the token(s).
	 * @param lemma lemmatized version of the token(s)
	 */
	void setLemma(String lemma);

	/**
	 * @return token index as token identifier in the label
	 */
	int getId();

	/**
	 * Sets token identifier in the label. In most cases equals to token index.
	 * @param id token identifier in the label
	 */
	void setId(int id);


	/**
	 * Returns the sense at index index.
	 *
	 * @param index index
	 * @return sense at index index
	 */
	ISense getSenseAt(int index);

	/**
	 * @return the number of senses
	 */
	int getSenseCount();

	/**
	 * Returns the index of sense in the receivers senses. 
	 * If the receiver does not contain sense, -1 will be returned.
	 *
	 * @param sense a sense to search for
	 * @return the index of sense in the receivers senses
	 */
	int getSenseIndex(ISense sense);

	/**
	 * @return the iterator over the senses of the query label.
	 */
	Iterator<ISense> getSenses();

	/**
	 * @return list of senses of the query label.
	 */
	List<ISense> getSenseList();

	/**
	 * Creates a sense and adds it as the last sense.
	 *
	 * @param pos pos
	 * @param id id
	 * @return a newly created sense
	 */
	ISense createSense(char pos, long id);

	/**
	 * Adds a sense to the existing list of senses.
	 * @param sense sense to be added.
	 */
	void addSense(ISense sense);

	/**
	 * Adds a sense to the existing list of senses at the index.
	 *
	 * @param index index where the sense will be added.
	 * @param sense sense to be added.
	 */
	void addSense(int index, ISense sense);

	/**
	 * Removes the sense at the index from the existing list of senses.
	 * @param index index of a sense to be removed.
	 */
	void removeSense(int index);

	/**
	 * Removes the sense from the existing list of senses.
	 * @param sense sense to be removed.
	 */
	void removeSense(ISense sense);
}

