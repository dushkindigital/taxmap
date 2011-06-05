package com.libereco.taxmap.symbolics;

import com.libereco.taxmap.symbolics.config.EnvironmentException;

/**
 * Base TaxMap exception class
 */
public class TaxMapException extends EnvironmentException 
{
	/**
	 * Constructor.
	 * Creates a new Exception by using super(msg) method.
	 * @param errorString the description of the error
	 *
	 */
	public TaxMapException(String errorString) 
	{
		super(errorString);
	}

	/**
	 * Constructor.
	 * Creates a new Exception by using super(msg, cause) method.
	 * @param errorString the description of the error
	 * @param reason      the cause of exception
	 *
	 */
	public TaxMapException(String errorString, Throwable reason) 
	{
		super(errorString, reason);
	}
}

