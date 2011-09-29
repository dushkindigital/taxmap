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
import com.libereco.taxmap.symbolics.data.ling.IDenotation;

/**
 * An interface to sense comparators.
 * @author Chiranjit Acharya
 */
public interface IDenotationComparator extends IEnvironment 
{
	/**
	 * Returns semantic relation between two sets of senses.
	 *
	 * @param sourceMeaningList source list of senses
	 * @param targetMeaningList target list of senses
	 * @return relation between two sense lists
	 * @throws DenotationException 
	 */
	public char getRelation(List<IDenotation> sourceMeaningList, List<IDenotation> targetMeaningList) throws DenotationException;

	/**
	 * Checks whether the source sense is more general than the target sense.
	 *
	 * @param sourceMeaning
	 * @param targetMeaning
	 * @return if generality relation holds
	 * @throws DenotationException
	 */
	public boolean isSourceGreaterThanTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException;

	/**
	 * Checks whether the source sense is less general than the target sense.
	 *
	 * @param sourceMeaning
	 * @param targetMeaning
	 * @return if speciality relation holds
	 * @throws DenotationException
	 */
	public boolean isSourceLesserThanTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException;

	/**
	 * Checks whether the source sense is equivalent to the target sense.
	 *
	 * @param sourceMeaning
	 * @param targetMeaning
	 * @return if equivalence relation holds
	 * @throws DenotationException
	 */
	public boolean isSourceEquivalentToTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException;

	/**
	 * Checks whether the source sense is unrelated (orthogonal) to the target sense.
	 *
	 * @param sourceMeaning
	 * @param targetMeaning
	 * @return if disjointedness or orthogonality relation holds.
	 * @throws DenotationException
	 */
	public boolean isSourceOrthogonalToTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException;
}

