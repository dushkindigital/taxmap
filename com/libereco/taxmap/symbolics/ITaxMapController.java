package com.libereco.taxmap.symbolics;

import com.libereco.taxmap.symbolics.config.*;
import com.libereco.taxmap.symbolics.data.*;
import com.libereco.taxmap.symbolics.reader.*;
import com.libereco.taxmap.symbolics.normalizer.*;
import com.libereco.taxmap.symbolics.writer.*;

/**
 * Top level interface for taxonomy mapping functionalities.
 * The following code can be used in order to obtain an instance of ITaxMapController interface.
 * ITaxMapController tmc = com.libereco.taxmap.symbolics.TaxMapController.getInstance();
 *
 */
public interface ITaxMapController extends IConfigurable {

    /**
     * Creates a context instance.
     * @return a context instance
     */
    public IContext createContext();

    /**
     * Returns mapping factory.
     * @return mapping factory 
     */
    public IMapFactory getMapFactory();

    /**
     * Uses current reader to read the context from external source into internal data structure.
     *
     * @param fileName a string passed to the current reader
     * @return interface to internal context representation
     * @throws TaxMapException 
     */
    IContext readContext(String fileName) throws TaxMapException;

    /**
     * Returns currently configured context reader.
     * @return currently configured context reader
     */
    IContextReader getContextReader();

    /**
     * Writes the context using a current writer.
     * @param ctxSource context to be written
     * @param fileName  a write destination passed to the context writer
     * @throws TaxMapException 
     */
    void writeContext(IContext ctxSource, String fileName) throws TaxMapException;

    /**
     * Returns currently configured context writer.
     * @return currently configured context writer
     */
    IContextWriter getContextWriter();

    /**
     * Reads the mapping between source and target contexts using the current mapping reader.
     * @param ctxSource source context
     * @param ctxTarget target context
     * @param inputFile a mapping location passed to the mapping reader
     * @return a mapping
     * @throws TaxMapException 
     */
    IContextMap<INode> readMap(IContext ctxSource, IContext ctxTarget, String inputFile) throws TaxMapException;

    /**
     * Returns currently configured mapping reader.
     * @return currently configured mapping reader
     */
    IMapReader getMapReader();

    /**
     * Writes the mapping using a current mapping writer.
     * @param mapping    a mapping
     * @param outputFile a write destination passed to the mapping writer
     * @throws TaxMapException 
     */
    void writeMap(IContextMap<INode> mapping, String outputFile) throws TaxMapException;

    /**
     * Returns currently configured mapping writer.
     * @return currently configured mapping writer
     */
    IMapWriter getMapWriter();

    /**
     * Filters a mapping. For example, filtering could be a minimization.
     * @param mapping a mapping to filter
     * @return a filtered mapping
     * @throws TaxMapException 
     */
    IContextMap<INode> filterMap(IContextMap<INode> mapping) throws TaxMapException;

    /**
     * Performs the first step of the taxonomy mapping algorithm.
     * @param context interface to a context to be normalized
     * @throws TaxMapException 
     */
    void normalize(IContext context) throws TaxMapException;

    /**
     * Returns currently configured context normalizer.
     * @return currently configured context normalizer
     */
    IContextNormalizer getContextNormalizer();

    /**
     * Performs the second step of the taxonomy mapping algorithm.
     * @param context interface to the normalized context
     * @throws TaxMapException 
     */
    void classify(IContext context) throws TaxMapException;

    /**
     * Performs the third step of taxonomy mapping algorithm.
     * @param sourceContext interface of source context with concept at node formula
     * @param targetContext interface of target context with concept at node formula
     * @return interface to a matrix of semantic relations between atomic concepts of labels in the contexts
     * @throws TaxMapException 
     */
    IContextMap<IAtomicConceptOfLabel> elementLevelMap(IContext sourceContext, IContext targetContext) throws TaxMapException;

    /**
     * Performs the fourth step of taxonomy mapping algorithm.
     * @param sourceContext interface of source context with concept at node formula
     * @param targetContext interface of target context with concept at node formula
     * @param conceptMap   mapping between atomic concepts of labels in the contexts
     * @return mapping between the concepts at nodes in the contexts
     * @throws TaxMapException 
     */
    IContextMap<INode> structureLevelMap(IContext sourceContext, IContext targetContext,
                                                  IContextMap<IAtomicConceptOfLabel> conceptMap) throws TaxMapException;

    /**
     * Performs the first two steps of the taxonomy mapping algorithm.
     * @param context interface to context to be normalized
     * @throws TaxMapException 
     */
    void offline(IContext context) throws TaxMapException;

    /**
     * Performs the last two steps of the taxonomy mapping algorithm.
     * @param sourceContext interface to normalized source context to be matched
     * @param targetContext interface to normalized target context to be matched
     * @return interface to finally derived mapping
     * @throws TaxMapException 
     */
    IContextMap<INode> online(IContext sourceContext, IContext targetContext) throws TaxMapException;

    /**
     * Performs the whole matching process.
     * @param sourceContext interface to source context to be matched
     * @param targetContext interface to target context to be matched
     * @return interface to finally derived mapping
     * @throws TaxMapException 
     */
    IContextMap<INode> map_taxonomy(IContext sourceContext, IContext targetContext) throws TaxMapException;
}
