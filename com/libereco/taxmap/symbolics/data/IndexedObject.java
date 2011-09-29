/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data;

/**
 * An object with an index.
 * @author Chiranjit Acharya
 */
public class IndexedObject implements IIndexedObject
{
    protected int _index;

    public IndexedObject() {
        this._index = -1;
    }

    public int getIndex() {
        return _index;
    }

    public void setIndex(int index) {
        _index = index;
    }
}
