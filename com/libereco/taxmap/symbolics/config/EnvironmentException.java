/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.config;

/**
 * Exception class for Environment.
 * @author Chiranjit Acharya
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
