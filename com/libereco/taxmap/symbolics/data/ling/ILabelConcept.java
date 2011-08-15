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
	 * Returns the meaning at index index.
	 *
	 * @param index index
	 * @return meaning at index index
	 */
	IDenotation getDenotationAt(int index);

	/**
	 * @return the number of meanings
	 */
	int getDenotationCount();

	/**
	 * Returns the index of meaning in the receivers meanings. 
	 * If the receiver does not contain meaning, -1 will be returned.
	 *
	 * @param meaning a meaning to search for
	 * @return the index of meaning in the receivers meanings
	 */
	int getDenotationIndex(IDenotation meaning);

	/**
	 * @return the iterator over the meanings of the query label.
	 */
	Iterator<IDenotation> getDenotations();

	/**
	 * @return list of meanings of the query label.
	 */
	List<IDenotation> getDenotationList();

	/**
	 * Creates a meaning and adds it as the last meaning.
	 *
	 * @param pos pos
	 * @param id id
	 * @return a newly created meaning
	 */
	IDenotation createDenotation(char pos, long id);

	/**
	 * Adds a meaning to the existing list of meanings.
	 * @param meaning meaning to be added.
	 */
	void addDenotation(IDenotation meaning);

	/**
	 * Adds a meaning to the existing list of meanings at the index.
	 *
	 * @param index index where the meaning will be added.
	 * @param meaning meaning to be added.
	 */
	void addDenotation(int index, IDenotation meaning);

	/**
	 * Removes the meaning at the index from the existing list of meanings.
	 * @param index index of a meaning to be removed.
	 */
	void removeDenotation(int index);

	/**
	 * Removes the meaning from the existing list of meanings.
	 * @param meaning meaning to be removed.
	 */
	void removeDenotation(IDenotation meaning);
}

