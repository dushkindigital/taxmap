package com.libereco.taxmap.symbolics.semantics;

import java.util.List;

import com.libereco.taxmap.symbolics.config.IEnvironment;
import com.libereco.taxmap.symbolics.data.ling.ISense;

/**
 * An interface to sense comparators.
 */
public interface ISenseComparator extends IEnvironment 
{
	/**
	 * Returns semantic relation between two sets of senses.
	 *
	 * @param sourceSenseList source list of senses
	 * @param targetSenseList target list of senses
	 * @return relation between two sense lists
	 * @throws SenseException 
	 */
	public char getRelation(List<ISense> sourceSenseList, List<ISense> targetSenseList) throws SenseException;

	/**
	 * Checks whether the source sense is more general than the target sense.
	 *
	 * @param source sense
	 * @param target sense
	 * @return if generality relation holds
	 * @throws SenseException
	 */
	public boolean isSourceGreaterThanTarget(ISense source, ISense target) throws SenseException;

	/**
	 * Checks whether the source sense is less general than the target sense.
	 *
	 * @param source sense
	 * @param target sense
	 * @return if speciality relation holds
	 * @throws SenseException
	 */
	public boolean isSourceLesserThanTarget(ISense source, ISense target) throws SenseException;

	/**
	 * Checks whether the source sense is equivalent to the target sense.
	 *
	 * @param source sense
	 * @param target sense
	 * @return if equivalence relation holds
	 * @throws SenseException
	 */
	public boolean isSourceEquivalentToTarget(ISense source, ISense target) throws SenseException;

	/**
	 * Checks whether the source sense is unrelated (orthogonal) to the target sense.
	 *
	 * @param source sense
	 * @param target sense
	 * @return if disjointedness or orthogonality relation holds.
	 * @throws SenseException
	 */
	public boolean isSourceOrthogonalToTarget(ISense source, ISense target) throws SenseException;
}

