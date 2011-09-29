/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Base class for environment variables in TaxMap module.
 * @author Chiranjit Acharya
 */
public abstract class Environment implements IEnvironment 
{
	private static final Logger log = Logger.getLogger(Environment.class);

	protected static final String TaxMapModulePrefix = "TaxMap.";

	// for modules prefixed with TaxMap. 
	protected static final Map<String, IEnvironment> TaxMapModules = new HashMap<String, IEnvironment>();

	protected Properties _variables;

	public Environment() 
	{
		_variables = new Properties();
	}

	public Environment(Properties variables) 
	{
		this._variables = variables;
	}

	public boolean setVariables(Properties newVariables) throws EnvironmentException 
	{
		boolean result = !newVariables.equals(_variables);
		if (result) 
		{
			_variables.clear();
			_variables.putAll(newVariables);
		}
		return result;
	}

	public boolean setVariables(String fileName) throws EnvironmentException 
	{
		return setVariables(loadProperties(fileName));
	}

	public Properties getVariables() 
	{
		return _variables;
	}

	/**
	 * Returns only variables that start with modulePrefix, removing this prefix.
	 *
	 * @param modulePrefix a prefix to search
	 * @param variables      properties
	 * @return variables that start with modulePrefix
	 */
	protected static Properties getModuleVariables(String modulePrefix, Properties variables) 
	{
		Properties moduleVariables = new Properties();
		if (null != modulePrefix) 
		{
			int modulePrefixLength = modulePrefix.length();
			for (String propertyName : variables.stringPropertyNames()) 
			{
				if (propertyName.startsWith(modulePrefix)) {
					moduleVariables.put(propertyName.substring(modulePrefixLength), variables.getProperty(propertyName));
				}
			}
		}
		return moduleVariables;
	}

	/**
	 * Creates a prefix for module to search for its variables in variables.
	 *
	 * @param tokenName a module configuration key
	 * @param typeName a module class name
	 * @return prefix
	 */
	protected static String makeModulePrefix(String tokenName, String typeName) 
	{
		String simpleTypeName = typeName;
		if (null != typeName) 
		{
			int lastDotIdx = typeName.lastIndexOf(".");
			if (lastDotIdx > -1) 
			{
				simpleTypeName = typeName.substring(lastDotIdx + 1, typeName.length());
			}
		}
		return tokenName + "." + simpleTypeName + ".";
	}

	public static IEnvironment configureModule(IEnvironment module, Properties oldVariables, Properties newVariables, String moduleName, String moduleKey, Class moduleInterface) throws EnvironmentException 
	{
		IEnvironment environment = null;
		boolean augmentTaxMap = false;

		// check global property
		final String TaxMapModuleKey = TaxMapModulePrefix + moduleKey;
		if (newVariables.containsKey(TaxMapModuleKey)) 
		{
			// module becomes or stays global
			augmentTaxMap = true;
			moduleKey = TaxMapModuleKey;
		} 
		else 
		{
			// module becomes local
			TaxMapModules.remove(TaxMapModuleKey);
		}

		String oldTypeName = oldVariables.getProperty(moduleKey);
		if ("".equals(oldTypeName)) 
		{
			oldTypeName = null;
		}
		String newTypeName = newVariables.getProperty(moduleKey);
		if ("".equals(newTypeName)) 
		{
			newTypeName = null;
		}
		Properties oldModuleVariables = getModuleVariables(makeModulePrefix(moduleKey, oldTypeName), oldVariables);
		Properties newModuleVariables = getModuleVariables(makeModulePrefix(moduleKey, newTypeName), newVariables);

		boolean toBeRedefined = !oldModuleVariables.equals(newModuleVariables);
		boolean toBeConstructed = false;
		if (null != oldTypeName) 
		{
			if (oldTypeName.equals(newTypeName)) 
			{
				environment = module;
			} 
			else 
			{
				if (null != newTypeName) 
				{
					toBeConstructed = true;
				}
			}
		} 
		else 
		{
			if (null != newTypeName) 
			{
				toBeConstructed = true;
			} 
			else 
			{
				if (log.isEnabledFor(Level.DEBUG)) 
				{
					log.debug("No " + moduleName);
				}
			}
		}

		if (toBeConstructed) 
		{
			synchronized (Environment.class) 
			{
				if (newTypeName.startsWith(TaxMapModulePrefix)) 
				{
					if (log.isEnabledFor(Level.DEBUG)) 
					{
						log.debug("Looking up global " + moduleName + ": " + newTypeName + "...");
					}
					environment = TaxMapModules.get(newTypeName);
					if (null == environment) 
					{
						final String errMessage = "Cannot find global " + moduleName + ": " + newTypeName + "...";
						if (log.isEnabledFor(Level.ERROR)) 
						{
							log.error(errMessage);
						}
						throw new EnvironmentException(errMessage);
					}
				} 
				else 
				{
					if (log.isEnabledFor(Level.DEBUG)) 
					{
						log.debug("Creating " + moduleName + ": " + newTypeName + "...");
					}
					Object obj = TypeFactory.getTypeForName(newTypeName);

					if (moduleInterface.isInstance(obj)) 
					{
						environment = (IEnvironment) obj;
					} 
					else 
					{
						final String errMessage = "Specified for " + moduleName + " " + newTypeName + " does not support " + moduleInterface.getSimpleName() + " interface";
						log.error(errMessage);
						throw new EnvironmentException(errMessage);
					}
				}
			}
		}

		if (toBeRedefined && null != environment) 
		{
			environment.setVariables(newModuleVariables);
		}


		if (augmentTaxMap) 
		{
			if (null != environment) 
			{
				TaxMapModules.put(TaxMapModuleKey, environment);
			} 
			else 
			{
				TaxMapModules.remove(TaxMapModuleKey);
			}
		}

		return environment;
	}

	/**
	 * Loads the variables from the properties file.
	 *
	 * @param filename the variables file name
	 * @return Properties instance
	 * @throws EnvironmentException
	 */
	public static Properties loadProperties(String filename) throws EnvironmentException 
	{
		log.info("Loading variables from " + filename);
		Properties variables = new Properties();
		FileInputStream input = null;
		try 
		{
			input = new FileInputStream(filename);
			variables.load(input);
		} 
		catch (IOException e) 
		{
			final String errMessage = e.getType().getSimpleName() + ": " + e.getMessage();
			log.error(errMessage, e);
			throw new EnvironmentException(errMessage, e);
		} 
		finally 
		{
			if (null != input) 
			{
				try 
				{
					input.close();
				} 
				catch (IOException e) 
				{
					final String errMessage = e.getType().getSimpleName() + ": " + e.getMessage();
					log.error(errMessage, e);
				}
			}
		}

		return variables;
	}

}
