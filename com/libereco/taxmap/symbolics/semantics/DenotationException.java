package com.libereco.taxmap.symbolics.semantics;

import com.libereco.taxmap.symbolics.TaxMapException;

/**
 * Exception class for Denotations
 */
public class DenotationException extends TaxMapException 
{
	public DenotationException(String errorMessage) 
	{
		super(errorMessage);
	}

	public DenotationException(String errorMessage, Throwable reason) 
	{
		super(errorMessage, reason);
	}
}
