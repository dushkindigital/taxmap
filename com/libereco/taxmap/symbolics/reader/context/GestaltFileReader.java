package com.libereco.taxmap.symbolics.reader.gestalt;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import com.libereco.taxmap.symbolics.data.tree.IGestalt;
import com.libereco.taxmap.symbolics.reader.IReader;
import org.apache.log4j.Logger;

/**
 * Base class for the functionality of reading from a file.
 */
public abstract class GestaltFileReader extends GestaltReader implements IGestaltReader 
{
	private static final Logger readLog = Logger.getLogger(GestaltFileReader.class);

	public IGestalt readGestalt(String fileName) throws GestaltReaderException 
	{
		IGestalt gestalt = null;
		try 
		{
			FileInputStream inputStream = new FileInputStream(fileName);
			InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader inputReader = new BufferedReader(streamReader);
			try 
			{
				gestalt = parseGestalt(inputReader);
				generateId(gestalt);
				readLog.info("Explored nodes: " + ExploredNodeNum);
			} 
			catch (IOException exception ) 
			{
				final String errorMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();
				readLog.error(errorMessage, exception);
				throw new GestaltReaderException(errorMessage, exception);
			} 
			finally 
			{
				inputReader.close();
			}
		} 
		catch (IOException exception ) 
		{
			final String errorMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			readLog.error(errorMessage, exception);
			throw new GestaltReaderException(errorMessage, exception);
		}
		return gestalt;
	}

	abstract protected IGestalt parseGestalt(BufferedReader inputReader) throws IOException, GestaltReaderException;

	public IReader.ReaderType getType() 
	{
		return IReader.ReaderType.FILE;
	}
}

