/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.libereco.taxmap.symbolics.classifier.IContextClassifier;
import com.libereco.taxmap.symbolics.config.Configurable;
import com.libereco.taxmap.symbolics.config.ConfigurableException;
import com.libereco.taxmap.symbolics.data.ling.IAtomicConceptOfLabel;
import com.libereco.taxmap.symbolics.data.map.IContextMap;
import com.libereco.taxmap.symbolics.data.map.IMapFactory;
import com.libereco.taxmap.symbolics.data.tree.Context;
import com.libereco.taxmap.symbolics.data.tree.IContext;
import com.libereco.taxmap.symbolics.data.tree.INode;
import com.libereco.taxmap.symbolics.filter.IMapFilter;
import com.libereco.taxmap.symbolics.reader.context.IContextReader;
import com.libereco.taxmap.symbolics.reader.map.IMapReader;
import com.libereco.taxmap.symbolics.comparator.element.IComparatorArxiv;
import com.libereco.taxmap.symbolics.comparator.structure.tree.ITreeComparator;
import com.libereco.taxmap.symbolics.semantics.ILinguisticSemantic;
import com.libereco.taxmap.symbolics.semantics.ISenseComparator;
import com.libereco.taxmap.symbolics.semantics.wordnet.InMemoryWordNetBinaryArray;
import com.libereco.taxmap.symbolics.normalizer.ContextNormalizer;
import com.libereco.taxmap.symbolics.normalizer.IContextNormalizer;
import com.libereco.taxmap.symbolics.writer.context.IContextWriter;
import com.libereco.taxmap.symbolics.writer.map.IMapWriter;
import com.libereco.taxmap.symbolics.util.TaxMapUtil;

/**
 * TaxMapController controls the process of comparison, reads contexts and performs other auxiliary work.
 * @author Chiranjit Acharya
 */
public class TaxMapController extends Configurable implements ITaxMapController {

    static {
        TaxMapUtil.configureLog4J();
    }

    private static final Logger log = Logger.getLogger(TaxMapController.class);

    /**
     * Default configuration file name.
     */
    public static final String DEFAULT_CONFIG_FILE_NAME = ".." + File.separator + "conf" + File.separator + "TaxMap.properties";


    // usage string
    private static final String USAGE = "Usage: TaxMapController <command> <arguments> [options]\n" ; // +

    // component configuration keys and component instance variables
    private static final String CONTEXT_READER_KEY = "ContextReader";
    private IContextReader contextReader = null;

    private static final String CONTEXT_WRITER_KEY = "ContextWriter";
    private IContextWriter contextWriter = null;

    private static final String MAPPING_READER_KEY = "MapReader";
    private IMapReader mapReader = null;

    private static final String MAPPING_WRITER_KEY = "MapWriter";
    private IMapWriter mapWriter = null;

    private static final String MAPPING_FILTER_KEY = "MapFilter";
    private IMapFilter mapFilter = null;

    private static final String CONTEXT_PREPROCESSOR_KEY = "ContextNormalizer";
    private IContextNormalizer contextNormalizer = null;

    private static final String CONTEXT_CLASSIFIER_KEY = "ContextClassifier";
    private IContextClassifier contextClassifier = null;

    private static final String COMPARATOR_LIBRARY_KEY = "ComparatorArxiv";
    private IComparatorArxiv comparatorArxiv = null;

    private static final String TREE_COMPARATOR_KEY = "TreeComparator";
    private ITreeComparator treeComparator = null;

    private static final String SENSE_COMPARATOR_KEY = "SenseComparator";
    private ISenseComparator senseComparator = null;

    private static final String LINGUISTIC_SEMANTIC_KEY = "LinguisticSemantic";
    private ILinguisticSemantic linguisticSemantic = null;

    private static final String MAPPING_FACTORY_KEY = "MapFactory";
    private IMapFactory mapFactory = null;

    public static ITaxMapController getInstance() throws TaxMapException {
        return new TaxMapController();
    }

    public TaxMapController() throws TaxMapException {
        super();
    }

    /**
     * Constructor class with initialization.
     *
     * @param propFileName the name of the properties file
     * @throws TaxMapException
     */
    public TaxMapController(String propFileName) throws TaxMapException {
        this();

        // update properties
        try {
            setProperties(propFileName);
        } catch (ConfigurableException e) {
            final String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errorMessage, e);
            throw new TaxMapException(errorMessage, e);
        }
    }

    /**
     * Constructor class with initialization.
     *
     * @param properties the properties 
     * @throws TaxMapException
     */
    public TaxMapController(Properties properties) throws TaxMapException {
        this();

        // update properties
        try {
            setProperties(properties);
        } catch (ConfigurableException e) {
            final String errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errorMessage, e);
            throw new TaxMapException(errorMessage, e);
        }
    }

    public IContext createContext() {
        return new Context();
    }

    public IMapFactory getMapFactory() {
        return mapFactory;
    }

    public IContext readContext(String fileName) throws TaxMapException {
        if (null == contextReader) {
            throw new TaxMapException("Context reader is not configured.");
        }

        log.info("Reading context from: " + fileName);
        final IContext result = contextReader.readContext(fileName);
        if (result instanceof Context) {
            ((Context) result).trim();
        }
        log.info("Reading context finished");
        return result;
    }

    public IContextReader getContextReader() {
        return contextReader;
    }

    public void writeContext(IContext ctxSource, String fileName) throws TaxMapException {
        if (null == contextWriter) {
            throw new TaxMapException("Context writer is not configured.");
        }
        log.info("Writing context to: " + fileName);
        contextWriter.write(ctxSource, fileName);
        log.info("Writing context finished");
    }

    public IContextWriter getContextWriter() {
        return contextWriter;
    }

    public IContextMap<INode> readMap(IContext ctxSource, IContext ctxTarget, String inputFile) throws TaxMapException {
        if (null == mapReader) {
            throw new TaxMapException("Map reader is not configured.");
        }
        log.info("Reading map from: " + inputFile);
        final IContextMap<INode> result = mapReader.readMap(ctxSource, ctxTarget, inputFile);
        log.info("Map reading finished");
        return result;
    }

    public IMapReader getMapReader() {
        return mapReader;
    }

    public void writeMap(IContextMap<INode> map, String outputFile) throws TaxMapException {
        if (null == mapWriter) {
            throw new TaxMapException("Map writer is not configured.");
        }
        log.info("Writing map to: " + outputFile);
        mapWriter.write(map, outputFile);
        log.info("Map writing finished");
    }

    public IMapWriter getMapWriter() {
        return mapWriter;
    }

    public IContextMap<INode> filterMap(IContextMap<INode> map) throws TaxMapException {
        if (null == mapFilter) {
            throw new TaxMapException("Map filter is not configured.");
        }
        log.info("Filtering...");
        final IContextMap<INode> result = mapFilter.filter(map);
        log.info("Filtering finished");
        return result;
    }

    public IContextMap<IAtomicConceptOfLabel> elementLevelMap(IContext sourceContext, IContext targetContext) throws TaxMapException {
        if (null == comparatorArxiv) {
            throw new TaxMapException("Comparator functionality is not configured.");
        }

        if (!sourceContext.getRoot().getNodeData().isSubtreePreprocessed()) {
            throw new TaxMapException("Source context is not normalized.");
        }

        if (!targetContext.getRoot().getNodeData().isSubtreePreprocessed()) {
            throw new TaxMapException("Target context is not normalized.");
        }

        log.info("Element level comparison...");
        final IContextMap<IAtomicConceptOfLabel> conceptMap = comparatorArxiv.elementLevelMap(sourceContext, targetContext);
        log.info("Element level comparison finished");
        return conceptMap;
    }

    public IContextMap<INode> structureLevelMap(IContext sourceContext,
                                                         IContext targetContext, IContextMap<IAtomicConceptOfLabel> conceptMap) throws TaxMapException {
        if (null == treeComparator) {
            throw new TaxMapException("Tree comparator is not configured.");
        }
        log.info("Structure level comparison...");
        IContextMap<INode> map = treeComparator.treeCompare(sourceContext, targetContext, conceptMap);
        log.info("Structure level comparison finished");
        log.info("Returning links: " + map.size());
        return map;
    }

    public void offline(IContext context) throws TaxMapException {
        log.info("Computing concept at label formulas...");
        normalize(context);
        log.info("Computing concept at label formulas finished");

        log.info("Computing concept at node formulas...");
        classify(context);
        log.info("Computing concept at node formulas finished");
    }

    public IContextMap<INode> online(IContext sourceContext, IContext targetContext) throws TaxMapException {
        // Performs element level comparison which computes the relation between labels.
        IContextMap<IAtomicConceptOfLabel> conceptMap = elementLevelMap(sourceContext, targetContext);
        // Performs structure level comparison which computes the relation between nodes.
        return structureLevelMap(sourceContext, targetContext, conceptMap);
    }

    public IContextMap<INode> map_taxonomy(IContext sourceContext, IContext targetContext) throws TaxMapException {
        log.info("Taxonomy map started...");
        offline(sourceContext);
        offline(targetContext);
        IContextMap<INode> result = online(sourceContext, targetContext);
        log.info("Taxonomy map finished");
        return result;
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Reading configuration...");
        }
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);
        boolean result = super.setProperties(newProperties);
        if (result) {
            // global ones
            linguisticSemantic = (ILinguisticSemantic) configureComponent(linguisticSemantic, oldProperties, newProperties, "linguistic oracle", LINGUISTIC_SEMANTIC_KEY, ILinguisticSemantic.class);
            senseComparator = (ISenseComparator) configureComponent(senseComparator, oldProperties, newProperties, "sense comparator", SENSE_COMPARATOR_KEY, ISenseComparator.class);
            mapFactory = (IMapFactory) configureComponent(mapFactory, oldProperties, newProperties, "map factory", MAPPING_FACTORY_KEY, IMapFactory.class);

            contextReader = (IContextReader) configureComponent(contextReader, oldProperties, newProperties, "context reader", CONTEXT_READER_KEY, IContextReader.class);
            contextWriter = (IContextWriter) configureComponent(contextWriter, oldProperties, newProperties, "context writer", CONTEXT_WRITER_KEY, IContextWriter.class);
            mapReader = (IMapReader) configureComponent(mapReader, oldProperties, newProperties, "map reader", MAPPING_READER_KEY, IMapReader.class);
            mapWriter = (IMapWriter) configureComponent(mapWriter, oldProperties, newProperties, "map writer", MAPPING_WRITER_KEY, IMapWriter.class);
            mapFilter = (IMapFilter) configureComponent(mapFilter, oldProperties, newProperties, "map filter", MAPPING_FILTER_KEY, IMapFilter.class);
            contextNormalizer = (IContextNormalizer) configureComponent(contextNormalizer, oldProperties, newProperties, "context normalizer", CONTEXT_PREPROCESSOR_KEY, IContextNormalizer.class);
            contextClassifier = (IContextClassifier) configureComponent(contextClassifier, oldProperties, newProperties, "context classifier", CONTEXT_CLASSIFIER_KEY, IContextClassifier.class);
            comparatorArxiv = (IComparatorArxiv) configureComponent(comparatorArxiv, oldProperties, newProperties, "comparison library", COMPARATOR_LIBRARY_KEY, IComparatorArxiv.class);
            treeComparator = (ITreeComparator) configureComponent(treeComparator, oldProperties, newProperties, "tree comparator", TREE_COMPARATOR_KEY, ITreeComparator.class);
        }
        return result;
    }

    public Properties getProperties() {
        return properties;
    }

    public void normalize(IContext context) throws TaxMapException {
        if (null == contextNormalizer) {
            throw new TaxMapException("Context normalizer is not configured.");
        }

        log.info("Computing concepts at label...");
        contextNormalizer.normalize(context);
        log.info("Computing concepts at label finished");
    }

    public IContextNormalizer getContextNormalizer() {
        return contextNormalizer;
    }

    public void classify(IContext context) throws TaxMapException {
        if (null == contextClassifier) {
            throw new TaxMapException("Context classifier is not configured.");
        }
        log.info("Computing concepts at node...");
        contextClassifier.buildCNodeFormulas(context);
        log.info("Computing concepts at node finished");
    }

    public static void main(String[] args) throws IOException, ConfigurableException {}
}
