package com.libereco.taxmap.symbolics.data.map;

import java.util.List;
import java.util.Set;

/**
 * Interface for mappings.
 */
public interface IMap<T> extends Set<IMapElement<T>>
{
    /**
     * Returns the relation between the source and the target.
     *
     * @param source source
     * @param target target
     * @return relation between source and target
     */
    char getRelation(T source, T target);

    /**
     * Sets the relation between the source and the target.
     *
     * @param source   source
     * @param target   target
     * @param relation relation
     * @return true if the mapping was modified
     */
    boolean setRelation(T source, T target, char relation);

    /**
     * Sets the similarity between two trees.
     *
     * @param similarity the similarity between two trees
     */
    void setSimilarity(double similarity);

    /**
     * Returns the similarity between two trees.
     *
     * @return the similarity between two trees
     */
    double getSimilarity();

    /**
     * Returns mapping elements with given source element 
     *
     * @param source source element
     * @return mapping elements with given source element
     */
    List<IMapElement<T>> getSource(T source);

    /**
     * Returns mapping elements with given target element
     *
     * @param target target element
     * @return mapping elements with given target element
     */
    List<IMapElement<T>> getTarget(T target);
}
