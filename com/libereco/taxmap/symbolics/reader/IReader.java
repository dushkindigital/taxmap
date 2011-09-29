/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.reader;

/**
 * File type related constants for readers.
 * @author Chiranjit Acharya
 */
public interface IReader 
{
	enum ReaderType {FILE, DATABASE, STRING}

	static String TXT_FILES = "Text Files (*.txt)";
	static String XML_FILES = "XML Files (*.xml)";
	static String RDF_FILES = "RDF Files (*.rdf)";
	static String OWL_FILES = "OWL Files (*.owl)";
}
