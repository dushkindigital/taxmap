/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.ling;

/**
 * Interface for a dictionary sense.
 * @author Chiranjit Acharya
 */
public interface ISense 
{
	char getSensePos();

	void setSensePos(char pos);

	long getSenseId();

	void setSenseId(long id);
}
