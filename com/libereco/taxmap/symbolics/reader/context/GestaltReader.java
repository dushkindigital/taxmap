package com.libereco.taxmap.symbolics.reader.gestalt;

import com.libereco.taxmap.symbolics.config.Environment;
import com.libereco.taxmap.symbolics.data.tree.IGestalt;
import com.libereco.taxmap.symbolics.data.tree.INode;
import org.apache.log4j.Logger;

/**
 * Base class for taxonomy file readers.
 */
public abstract class GestaltReader extends Environment implements IGestaltReader 
{
	private static final Logger log = Logger.getLogger(GestaltReader.class);

	protected int ExploredNodeNum = 0;

	protected void generateId(IGestalt gestalt) 
	{
		log.debug("Generating ids for gestalt...");
		ExploredNodeNum = 0;
		for (INode node : gestalt.getNodeList()) 
		{
			node.getNodeCore().setId("n" + Integer.toString(ExploredNodeNum));
			ExploredNodeNum++;
		}
	}
}

