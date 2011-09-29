/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.reader.context;

import com.libereco.taxmap.symbolics.TaxMapException;

/**
 * Exception class for Gestalt Readers.
 * @author Chiranjit Acharya
 */
public class GestaltReaderException extends TaxMapException 
{
	public GestaltReaderException(String errorMessage) 
	{
		super(errorMessage);
	}

	public GestaltReaderException(String errorMessage, Throwable reason) 
	{
		super(errorMessage, reason);
	}
}

