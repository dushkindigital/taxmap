/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data;

/**
 * Objects which could be referenced by index.
 * @author Chiranjit Acharya
 */
public interface IIndexedObject {
    /**
     * Gets the index of a node.
     * @return the index of a node.
     */
    int getIndex();

    /**
     * Sets the index of a node.
     * @param index index
     */
    void setIndex(int index);
}
