package com.libereco.taxmap.symbolics.config;

/**
 * Exception class for Environment.
 */
public class EnvironmentException extends Exception 
{
	public EnvironmentException(String errmsg) 
	{
		super(errmsg);
	}

	public EnvironmentException(String errmsg, Throwable reason) 
	{
		super(errmsg, reason);
	}
}
