package com.libereco.taxmap.symbolics.data.ling;

/**
 * Interface for a dictionary meaning of a word.
 */
public interface IDenotation 
{
	char getPosition();

	void setPosition(char pos);

	long getId();

	void setId(long id);
}
