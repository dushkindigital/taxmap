/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.data.ling;

/**
 * Default dictionary sense implementation.
 * @author Chiranjit Acharya
 */
public class Sense implements ISense 
{
    char _pos;
    long _id;

    private Sense() {
    }

    public Sense(char pos, long id) {
        this._pos = pos;
        this._id = id;
    }

    public char getSensePos() {
        return _pos;
    }

    public void setSensePos(char pos) {
        this._pos = pos;
    }

    public long getSenseId() {
        return _id;
    }

    public void setSenseId(long id) {
        this._id = id;
    }

    public String toString() {
        return _pos + "#" + _id;
    }

    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Sense)) return false;

        Sense sense = (Sense) that;

        if (_id != sense._id) return false;
        if (_pos != sense._pos) return false;

        return true;
    }

    public int hash() {
        int hashval = (int) _pos;
        hashval = 29 * hashval + (int) (_id ^ (_id >>> 31));
        return hashval;
    }
}
