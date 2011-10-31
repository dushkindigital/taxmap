/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics;

import com.libereco.taxmap.symbolics.config.*;
import com.libereco.taxmap.symbolics.data.*;
import com.libereco.taxmap.symbolics.reader.*;
import com.libereco.taxmap.symbolics.normalizer.*;
import com.libereco.taxmap.symbolics.serializer.*;

/**
 * Top level interface for taxonomy mapping functionalities.
 * The following code can be used in order to obtain an instance of ITaxMapController interface.
 * ITaxMapController tmc = com.libereco.taxmap.symbolics.TaxMapController.getInstance();
 * @author Chiranjit Acharya
 */
public interface ITaxMapController extends IConfigurable {

    /**
     * Creates a gestalt instance.
     * @return a gestalt instance
     */
    public IGestalt createGestalt();

    /**
     * Returns mapping factory.
     * @return mapping factory 
     */
    public IMapFactory getMapFactory();

    /**
     * Uses current reader to read the gestalt from external source into internal data structure.
     *
     * @param fileName a string passed to the current reader
     * @return interface to internal gestalt representation
     * @throws TaxMapException 
     */
    IGestalt readGestalt(String fileName) throws TaxMapException;

    /**
     * Returns currently configured gestalt reader.
     * @return currently configured gestalt reader
     */
    IGestaltReader getGestaltReader();

    /**
     * Serializes the gestalt using a current serializer.
     * @param Gestalt1 gestalt to be written
     * @param fileName  a serialize destination passed to the gestalt serializer
     * @throws TaxMapException 
     */
    void serializeGestalt(IGestalt Gestalt1, String fileName) throws TaxMapException;

    /**
     * Returns currently configured gestalt serializer.
     * @return currently configured gestalt serializer
     */
    IGestaltSerializer getGestaltSerializer();

    /**
     * Reads the mapping between source and target gestalts using the current mapping reader.
     * @param Gestalt1 source gestalt
     * @param Gestalt2 target gestalt
     * @param inputFile a mapping location passed to the mapping reader
     * @return a mapping
     * @throws TaxMapException 
     */
    IGestaltMap<INode> readMap(IGestalt Gestalt1, IGestalt Gestalt2, String inputFile) throws TaxMapException;

    /**
     * Returns currently configured mapping reader.
     * @return currently configured mapping reader
     */
    IMapReader getMapReader();

    /**
     * Serializes the mapping using a current mapping serializer.
     * @param mapping    a mapping
     * @param outputFile a serialize destination passed to the mapping serializer
     * @throws TaxMapException 
     */
    void serializeMap(IGestaltMap<INode> mapping, String outputFile) throws TaxMapException;

    /**
     * Returns currently configured mapping serializer.
     * @return currently configured mapping serializer
     */
    IMapSerializer getMapSerializer();

    /**
     * Filters a mapping. For example, filtering could be a minimization.
     * @param mapping a mapping to filter
     * @return a filtered mapping
     * @throws TaxMapException 
     */
    IGestaltMap<INode> filterMap(IGestaltMap<INode> mapping) throws TaxMapException;

    /**
     * Performs the first step of the taxonomy mapping algorithm.
     * @param gestalt interface to a gestalt to be normalized
     * @throws TaxMapException 
     */
    void normalize(IGestalt gestalt) throws TaxMapException;

    /**
     * Returns currently configured gestalt normalizer.
     * @return currently configured gestalt normalizer
     */
    IGestaltNormalizer getGestaltNormalizer();

    /**
     * Performs the second step of the taxonomy mapping algorithm.
     * @param gestalt interface to the normalized gestalt
     * @throws TaxMapException 
     */
    void classify(IGestalt gestalt) throws TaxMapException;

    /**
     * Performs the third step of taxonomy mapping algorithm.
     * @param Gestalt1 interface of source gestalt with concept at node formula
     * @param Gestalt2 interface of target gestalt with concept at node formula
     * @return interface to a matrix of semantic relations between atomic concepts of labels in the gestalts
     * @throws TaxMapException 
     */
    IGestaltMap<IAtomicConceptOfLabel> elementLevelMap(IGestalt Gestalt1, IGestalt Gestalt2) throws TaxMapException;

    /**
     * Performs the fourth step of taxonomy mapping algorithm.
     * @param Gestalt1 interface of source gestalt with concept at node formula
     * @param Gestalt2 interface of target gestalt with concept at node formula
     * @param conceptMap   mapping between atomic concepts of labels in the gestalts
     * @return mapping between the concepts at nodes in the gestalts
     * @throws TaxMapException 
     */
    IGestaltMap<INode> structureLevelMap(IGestalt Gestalt1, IGestalt Gestalt2,
                                                  IGestaltMap<IAtomicConceptOfLabel> conceptMap) throws TaxMapException;

    /**
     * Performs the first two steps of the taxonomy mapping algorithm.
     * @param gestalt interface to gestalt to be normalized
     * @throws TaxMapException 
     */
    void offline(IGestalt gestalt) throws TaxMapException;

    /**
     * Performs the last two steps of the taxonomy mapping algorithm.
     * @param Gestalt1 interface to normalized source gestalt to be matched
     * @param Gestalt2 interface to normalized target gestalt to be matched
     * @return interface to finally derived mapping
     * @throws TaxMapException 
     */
    IGestaltMap<INode> online(IGestalt Gestalt1, IGestalt Gestalt2) throws TaxMapException;

    /**
     * Performs the whole matching process.
     * @param Gestalt1 interface to source gestalt to be matched
     * @param Gestalt2 interface to target gestalt to be matched
     * @return interface to finally derived mapping
     * @throws TaxMapException 
     */
    IGestaltMap<INode> map_taxonomy(IGestalt Gestalt1, IGestalt Gestalt2) throws TaxMapException;
}
