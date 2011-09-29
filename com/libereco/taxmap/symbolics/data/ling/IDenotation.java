/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.ling;

/**
 * Interface for a dictionary meaning of a word.
 * @author Chiranjit Acharya
 */
public interface IDenotation 
{
	char getPosition();

	void setPosition(char pos);

	long getId();

	void setId(long id);
}
