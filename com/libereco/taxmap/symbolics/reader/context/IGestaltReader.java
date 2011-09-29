/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.reader.gestalt;

import com.libereco.taxmap.symbolics.config.IEnvironment;
import com.libereco.taxmap.symbolics.data.tree.IGestalt;
import com.libereco.taxmap.symbolics.reader.IReader;

/**
 * Interface for gestalt readers. 
 * Gestalt readers read gestalts or taxonomy views from various physical sources such as files, databases, etc.
 * @author Chiranjit Acharya
 */
public interface IGestaltReader extends IEnvironment 
{
	/**
	 * Reads the gestalt from a file or database.
	 *
	 * @param fileName file to be read
	 * @return interface to the gestalt data structure
	 * @throws GestaltReaderException 
	 */
	IGestalt readGestalt(String fileName) throws GestaltReaderException;

	/**
	 * Returns the format descriptor
	 */
	String getDescriptor();

	/**
	 * Returns the type of the reader.
	 */
	IReader.ReaderType getType();
}

