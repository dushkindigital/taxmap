/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.semantics;

import com.libereco.taxmap.symbolics.TaxMapException;

/**
 * Exception class for Semantics
 * @author Chiranjit Acharya
 */
public class SemanticsException extends TaxMapException 
{
	public SemanticsException(String errorMessage) 
	{
		super(errorMessage);
	}

	public SemanticsException(String errorMessage, Throwable reason) 
	{
		super(errorMessage, reason);
	}
}
