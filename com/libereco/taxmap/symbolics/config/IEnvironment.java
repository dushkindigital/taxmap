/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.config;

import java.util.Properties;

/**
 * Interface class for collection of environment variables
 * @author Chiranjit Acharya
 */
public interface IEnvironment 
{
	/**
	 * Gets module environment variables.
	 * @return collective environment of a module
	 */
	Properties getVariables();

	/**
	 * Sets module environment variables. The module might check for variables change and
	 * reconfigure or reload submodules.
	 *
	 * @param newVariables collectively defines a new environment
	 * @return true if variables have been changed
	 * @throws EnvironmentException 
	 */
	boolean setVariables(Properties newVariables) throws EnvironmentException;

	/**
	 * Sets module environment by reading it from a file.
	 *
	 * @param fileName .variables file name
	 * @return true if variables have been changed
	 * @throws EnvironmentException 
	 */
	boolean setVariables(String fileName) throws EnvironmentException;
}

