package com.libereco.taxmap.symbolics.data;

/**
 * An object with an index.
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
