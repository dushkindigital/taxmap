package com.libereco.taxmap.symbolics.semantics;

import com.libereco.taxmap.symbolics.TaxMapException;

/**
 * Exception class for Senses
 */
public class SenseException extends TaxMapException 
{
	public SenseException(String errorMessage) 
	{
		super(errorMessage);
	}

	public SenseException(String errorMessage, Throwable reason) 
	{
		super(errorMessage, reason);
	}
}
