/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.map;

/**
 * Interface for a relation instance.
 * @author Chiranjit Acharya
 */
public interface IRelationInstance<T> 
{
	// basic relational constants
	char EQUIVALENCE = '=';
	char LESS_GENERAL = '<';
	char MORE_GENERAL = '>';
	char DISJOINT = '!';

	// relational constants for minimal links
	char LESS_GENERAL_IMPLIED = 'L';
	char MORE_GENERAL_IMPLIED = 'M';
	char DISJOINT_IMPLIED = 'X';

	char UNKNOWN = '?';

	T getSource();

	T getTarget();

	char getRelation();
}
