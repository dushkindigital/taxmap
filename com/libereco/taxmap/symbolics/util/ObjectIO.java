/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.util;

import java.io.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.libereco.taxmap.symbolics.TaxMapException;

/**
 * File I/O class for Java runtime objects.
 * @author Chiranjit Acharya
 */
public class ObjectIO 
{
	private static final Logger log = Logger.getLogger(ObjectIO.class);

	/**
	 * Reads Java object from a file.
	 *
	 * @param fileName the file where the object is stored
	 * @return object instance
	 * @throws TaxMapException 
	 */
	public static Object readObject(String fileName) throws TaxMapException 
	{
		Object instance;
		try 
		{
			FileInputStream ifstream = new FileInputStream(fileName);
			BufferedInputStream bufstream = new BufferedInputStream(ifstream);
			ObjectInputStream objstream = new ObjectInputStream(bufstream);
			try 
			{
				instance = objstream.readObject();
			} 
			catch (IOException exception) 
			{
				String errorString = exception.getClass().getSimpleName() + ": " + exception.getMessage();
				log.error(errorString, exception);
				throw new TaxMapException(errorString, exception);
			} 
			catch (ClassNotFoundException exception) 
			{
				String errorString = exception.getClass().getSimpleName() + ": " + exception.getMessage();
				log.error(errorString, exception);
				throw new TaxMapException(errorString, exception);
			}
			objstream.close();
			bufstream.close();
			ifstream.close();
		} 
		catch (IOException exception) 
		{
			String errorString = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			log.error(errorString, exception);
			throw new TaxMapException(errorString, exception);
		}
		return instance;
	}
}

