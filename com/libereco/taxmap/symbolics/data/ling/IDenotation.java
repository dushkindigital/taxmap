package com.libereco.taxmap.symbolics.data.ling;

/**
 * Interface for a dictionary meaning of a word.
 */
public interface IDenotation 
{
	char getDenotationPos();

	void setDenotationPos(char pos);

	long getDenotationId();

	void setDenotationId(long id);
}
