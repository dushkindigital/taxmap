/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.reader.gestalt;

import com.libereco.taxmap.symbolics.config.Environment;
import com.libereco.taxmap.symbolics.data.tree.IGestalt;
import com.libereco.taxmap.symbolics.data.tree.INode;
import org.apache.log4j.Logger;

/**
 * Base class for taxonomy file readers.
 * @author Chiranjit Acharya
 */
public abstract class GestaltReader extends Environment implements IGestaltReader 
{
	private static final Logger readLog = Logger.getLogger(GestaltReader.class);

	protected int ExploredNodeNum = 0;

	protected void generateId(IGestalt gestalt) 
	{
		readLog.debug("Generating ids for gestalt nodes...");
		ExploredNodeNum = 0;
		for (INode node : gestalt.getNodeList()) 
		{
			node.getNodeCore().setId("n" + Integer.toString(ExploredNodeNum));
			ExploredNodeNum++;
		}
	}
}

