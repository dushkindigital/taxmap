/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics;


/**
 * This class represents a multidimensional array 
 * whose dimensions are specified during construction.
 * The multidimensional array is modeled by an underlying one dimensional array.
 * @author Chiranjit Acharya
 *
 * @param <T> the class of the array elements
 */
public class MultiVector<T> 
{
	/** The underlying single dimension array. */
	private T[] _vector;
	
	/**
	 * The array of dimensional sizes of the multidimensional array.
	 */
	private int[] dimensionSizes;
	
	/**
	 * The number of coordinateOffset that is possible with specifying
	 * the last following attributes. These are used for computing
	 * the single array index from multidimensional indices.
	 */
	private int[] coordinateOffset;

	/**
	 * The constructor.
	 * 
	 * @param dimensionSizes int array which contains the size of each dimension 
	 */
	public MultiVector(int[] dimensionSizes) {
		this.dimensionSizes = dimensionSizes;
		int dimension = dimensionSizes.length;
		
		int vectorSize = 1;
		coordinateOffset = new int[dimension];
		for (int i = dimension - 1; i > 0; i--) {
			vectorSize *= dimensionSizes[i];
			coordinateOffset[i - 1] = vectorSize;
		}
		vectorSize *= dimensionSizes[0];
		coordinateOffset[dimension - 1] = 1;
		_vector = (T[]) new Object[vectorSize];
	}
	
	/**
	 * Returns the array element at the specified single dimension
	 * array position.
	 * 
	 * @param index the index
	 * @return an element
	 */
	public T get(int index) {
		return _vector[index];
	}
	
	/**
	 * Returns the array element at the position specified by the
	 * given indices.
	 * 
	 * @param indices the indices
	 * @return an element
	 */
	public T get(int[] indices) {
		return _vector[getIndex(indices)];
	}
	
	/**
	 * Sets the array element at the specified single dimension
	 * array position.
	 * 
	 * @param index the index
	 * @param e an element
	 */
	public void set(int index, T e) {
		_vector[index] = e;
	}
	
	/**
	 * Sets the array element at the position specified by the given
	 * indices
	 * 
	 * @param indices the indices
	 * @param e an element
	 */
	public void set(int[] indices, T e) {
		_vector[getIndex(indices)] = e;
	}

	/**
	 * Computes the single dimension array index from the given
	 * multidimensional indices.
	 *  
	 * @param indices the indices
	 * @return the corresponding index
	 */
	public int getIndex(int[] indices) {
		return sumProduct(indices, coordinateOffset);
	}

	/**
	 * Computs the multidimensional indices corresponding to a single
	 * dimension array index.
	 * 
	 * @param index the index
	 * @return the corresponding indices
	 */
	public int[] getIndices(int index) {
		int[] indices = new int[coordinateOffset.length];
		int r = index;
		for (int i = 0; i < indices.length; i++) {
			indices[i] = r / coordinateOffset[i];
			r = r % coordinateOffset[i];
		}
		return indices;
	}
	
	/**
	 * Returns the number of elements this _vector can hold.
	 * 
	 * @return size
	 */
	public int size() {
		return _vector.length;
	}
	
	/**
	 * Returns an int array holding the array of dimensional sizes of the
	 * multidimensional array.
	 * 
	 * @return dimensionSizes
	 */
	public int[] getDimensions() {
		return dimensionSizes;
	}
	
	/**
	 * Calculates the sum product of two int arrays. Used for the calculation
	 * of a single dimension array index from multidimensional indices.
	 */
	private int sumProduct(int[] firstIndices, int[] secondIndices) {
		int firstLength = firstIndices.length;
		int secondLength = secondIndices.length;
		int length = secondLength > firstLength ? firstLength : secondLength;
		int product = 0;
		for (int i = 0; i < length; i++) {
			product += firstIndices[i] * secondIndices[i];
		}
		return product;
	}
}

