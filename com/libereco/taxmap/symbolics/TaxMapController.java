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

import com.libereco.taxmap.symbolics.classifier.IGestaltClassifier;
import com.libereco.taxmap.symbolics.config.Configurable;
import com.libereco.taxmap.symbolics.config.ConfigurableException;
import com.libereco.taxmap.symbolics.data.ling.IAtomicConceptOfLabel;
import com.libereco.taxmap.symbolics.data.map.IGestaltMap;
import com.libereco.taxmap.symbolics.data.map.IMapFactory;
import com.libereco.taxmap.symbolics.data.tree.Gestalt;
import com.libereco.taxmap.symbolics.data.tree.IGestalt;
import com.libereco.taxmap.symbolics.data.tree.INode;
import com.libereco.taxmap.symbolics.filter.IMapFilter;
import com.libereco.taxmap.symbolics.reader.gestalt.IGestaltReader;
import com.libereco.taxmap.symbolics.reader.map.IMapReader;
import com.libereco.taxmap.symbolics.comparator.element.IComparatorArxiv;
import com.libereco.taxmap.symbolics.comparator.structure.tree.ITreeComparator;
import com.libereco.taxmap.symbolics.semantics.ILinguisticSemantic;
import com.libereco.taxmap.symbolics.semantics.ISenseComparator;
import com.libereco.taxmap.symbolics.semantics.wordnet.InMemoryWordNetBinaryArray;
import com.libereco.taxmap.symbolics.normalizer.GestaltNormalizer;
import com.libereco.taxmap.symbolics.normalizer.IGestaltNormalizer;
import com.libereco.taxmap.symbolics.serializer.gestalt.IGestaltSerializer;
import com.libereco.taxmap.symbolics.serializer.map.IMapSerializer;
import com.libereco.taxmap.symbolics.util.TaxMapUtil;

/**
 * TaxMapController controls the process of comparison, reads gestalts and performs other auxiliary tasks.
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
    private static final String CONTEXT_READER_KEY = "GestaltReader";
    private IGestaltReader gestaltReader = null;

    private static final String CONTEXT_WRITER_KEY = "GestaltSerializer";
    private IGestaltSerializer gestaltSerializer = null;

    private static final String MAPPING_READER_KEY = "MapReader";
    private IMapReader mapReader = null;

    private static final String MAPPING_WRITER_KEY = "MapSerializer";
    private IMapSerializer mapSerializer = null;

    private static final String MAPPING_FILTER_KEY = "MapFilter";
    private IMapFilter mapFilter = null;

    private static final String CONTEXT_PREPROCESSOR_KEY = "GestaltNormalizer";
    private IGestaltNormalizer gestaltNormalizer = null;

    private static final String CONTEXT_CLASSIFIER_KEY = "GestaltClassifier";
    private IGestaltClassifier gestaltClassifier = null;

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

    public IGestalt createGestalt() {
        return new Gestalt();
    }

    public IMapFactory getMapFactory() {
        return mapFactory;
    }

    public IGestalt readGestalt(String fileName) throws TaxMapException {
        if (null == gestaltReader) {
            throw new TaxMapException("Gestalt reader is not configured.");
        }

        log.info("Reading gestalt from: " + fileName);
        final IGestalt result = gestaltReader.readGestalt(fileName);
        if (result instanceof Gestalt) {
            ((Gestalt) result).trim();
        }
        log.info("Reading gestalt finished");
        return result;
    }

    public IGestaltReader getGestaltReader() {
        return gestaltReader;
    }

    public void serializeGestalt(IGestalt Gestalt1, String fileName) throws TaxMapException {
        if (null == gestaltSerializer) {
            throw new TaxMapException("Gestalt serializer is not configured.");
        }
        log.info("Writing gestalt to: " + fileName);
        gestaltSerializer.serialize(Gestalt1, fileName);
        log.info("Writing gestalt finished");
    }

    public IGestaltSerializer getGestaltSerializer() {
        return gestaltSerializer;
    }

    public IGestaltMap<INode> readMap(IGestalt Gestalt1, IGestalt Gestalt2, String inputFile) throws TaxMapException {
        if (null == mapReader) {
            throw new TaxMapException("Map reader is not configured.");
        }
        log.info("Reading map from: " + inputFile);
        final IGestaltMap<INode> result = mapReader.readMap(Gestalt1, Gestalt2, inputFile);
        log.info("Map reading finished");
        return result;
    }

    public IMapReader getMapReader() {
        return mapReader;
    }

    public void serializeMap(IGestaltMap<INode> map, String outputFile) throws TaxMapException {
        if (null == mapSerializer) {
            throw new TaxMapException("Map serializer is not configured.");
        }
        log.info("Writing map to: " + outputFile);
        mapSerializer.serialize(map, outputFile);
        log.info("Map writing finished");
    }

    public IMapSerializer getMapSerializer() {
        return mapSerializer;
    }

    public IGestaltMap<INode> filterMap(IGestaltMap<INode> map) throws TaxMapException {
        if (null == mapFilter) {
            throw new TaxMapException("Map filter is not configured.");
        }
        log.info("Filtering...");
        final IGestaltMap<INode> result = mapFilter.filter(map);
        log.info("Filtering finished");
        return result;
    }

    public IGestaltMap<IAtomicConceptOfLabel> elementLevelMap(IGestalt Gestalt1, IGestalt Gestalt2) throws TaxMapException {
        if (null == comparatorArxiv) {
            throw new TaxMapException("Comparator functionality is not configured.");
        }

        if (!Gestalt1.getRoot().getNodeData().isSubtreePreprocessed()) {
            throw new TaxMapException("Source gestalt is not normalized.");
        }

        if (!Gestalt2.getRoot().getNodeData().isSubtreePreprocessed()) {
            throw new TaxMapException("Target gestalt is not normalized.");
        }

        log.info("Element level comparison...");
        final IGestaltMap<IAtomicConceptOfLabel> conceptMap = comparatorArxiv.elementLevelMap(Gestalt1, Gestalt2);
        log.info("Element level comparison finished");
        return conceptMap;
    }

    public IGestaltMap<INode> structureLevelMap(IGestalt Gestalt1,
                                                         IGestalt Gestalt2, IGestaltMap<IAtomicConceptOfLabel> conceptMap) throws TaxMapException {
        if (null == treeComparator) {
            throw new TaxMapException("Tree comparator is not configured.");
        }
        log.info("Structure level comparison...");
        IGestaltMap<INode> map = treeComparator.treeCompare(Gestalt1, Gestalt2, conceptMap);
        log.info("Structure level comparison finished");
        log.info("Returning links: " + map.size());
        return map;
    }

    public void offline(IGestalt gestalt) throws TaxMapException {
        log.info("Computing concept at label formulas...");
        normalize(gestalt);
        log.info("Computing concept at label formulas finished");

        log.info("Computing concept at node formulas...");
        classify(gestalt);
        log.info("Computing concept at node formulas finished");
    }

    public IGestaltMap<INode> online(IGestalt Gestalt1, IGestalt Gestalt2) throws TaxMapException {
        // Performs element level comparison which computes the relation between labels.
        IGestaltMap<IAtomicConceptOfLabel> conceptMap = elementLevelMap(Gestalt1, Gestalt2);
        // Performs structure level comparison which computes the relation between nodes.
        return structureLevelMap(Gestalt1, Gestalt2, conceptMap);
    }

    public IGestaltMap<INode> map_taxonomy(IGestalt Gestalt1, IGestalt Gestalt2) throws TaxMapException {
        log.info("Taxonomy map started...");
        offline(Gestalt1);
        offline(Gestalt2);
        IGestaltMap<INode> result = online(Gestalt1, Gestalt2);
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

            gestaltReader = (IGestaltReader) configureComponent(gestaltReader, oldProperties, newProperties, "gestalt reader", CONTEXT_READER_KEY, IGestaltReader.class);
            gestaltSerializer = (IGestaltSerializer) configureComponent(gestaltSerializer, oldProperties, newProperties, "gestalt serializer", CONTEXT_WRITER_KEY, IGestaltSerializer.class);
            mapReader = (IMapReader) configureComponent(mapReader, oldProperties, newProperties, "map reader", MAPPING_READER_KEY, IMapReader.class);
            mapSerializer = (IMapSerializer) configureComponent(mapSerializer, oldProperties, newProperties, "map serializer", MAPPING_WRITER_KEY, IMapSerializer.class);
            mapFilter = (IMapFilter) configureComponent(mapFilter, oldProperties, newProperties, "map filter", MAPPING_FILTER_KEY, IMapFilter.class);
            gestaltNormalizer = (IGestaltNormalizer) configureComponent(gestaltNormalizer, oldProperties, newProperties, "gestalt normalizer", CONTEXT_PREPROCESSOR_KEY, IGestaltNormalizer.class);
            gestaltClassifier = (IGestaltClassifier) configureComponent(gestaltClassifier, oldProperties, newProperties, "gestalt classifier", CONTEXT_CLASSIFIER_KEY, IGestaltClassifier.class);
            comparatorArxiv = (IComparatorArxiv) configureComponent(comparatorArxiv, oldProperties, newProperties, "comparison library", COMPARATOR_LIBRARY_KEY, IComparatorArxiv.class);
            treeComparator = (ITreeComparator) configureComponent(treeComparator, oldProperties, newProperties, "tree comparator", TREE_COMPARATOR_KEY, ITreeComparator.class);
        }
        return result;
    }

    public Properties getProperties() {
        return properties;
    }

    public void normalize(IGestalt gestalt) throws TaxMapException {
        if (null == gestaltNormalizer) {
            throw new TaxMapException("Gestalt normalizer is not configured.");
        }

        log.info("Computing concepts at label...");
        gestaltNormalizer.normalize(gestalt);
        log.info("Computing concepts at label finished");
    }

    public IGestaltNormalizer getGestaltNormalizer() {
        return gestaltNormalizer;
    }

    public void classify(IGestalt gestalt) throws TaxMapException {
        if (null == gestaltClassifier) {
            throw new TaxMapException("Gestalt classifier is not configured.");
        }
        log.info("Computing concepts at node...");
        gestaltClassifier.buildCNodeFormulas(gestalt);
        log.info("Computing concepts at node finished");
    }

    public static void main(String[] args) throws IOException, ConfigurableException {}
}
