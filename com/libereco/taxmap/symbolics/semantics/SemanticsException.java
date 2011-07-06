package com.libereco.taxmap.symbolics.semantics;

import com.libereco.taxmap.symbolics.TaxMapException;

/**
 * Exception class for Semantics
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
