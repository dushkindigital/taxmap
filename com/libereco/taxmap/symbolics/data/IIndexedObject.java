package com.libereco.taxmap.symbolics.data;

/**
 * Objects which could be referenced by index.
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
