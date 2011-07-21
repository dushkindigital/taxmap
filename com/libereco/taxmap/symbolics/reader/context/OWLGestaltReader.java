package com.libereco.taxmap.symbolics.reader.gestalt;

import java.util.Set;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.HermiT.Reasoner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.libereco.taxmap.symbolics.config.EnvironmentException;
import com.libereco.taxmap.symbolics.data.tree.INode;
import com.libereco.taxmap.symbolics.data.tree.Gestalt;
import com.libereco.taxmap.symbolics.data.tree.IGestalt;
import com.libereco.taxmap.symbolics.reader.IReader;

/**
 * Reads a gestalt using an OWL API
 */
public class OWLGestaltReader extends GestaltReader implements IGestaltReader 
{
	private static final Logger log = Logger.getLogger(OWLGestaltReader.class);

	// class to start from, if not specified, a Thing will be used
	private static final String ROOT_CATEGORY_KEY = "rootCategory";
	private String rootCategory = null;

	// whether to exclude Nothing class
	private static final String INCLUDE_ALL_KEY = "allInclusive";
	private boolean allInclusive = true;

	// whether to replace _
	private static final String NO_UNDERSCORE_KEY = "noUnderscore";
	private boolean noUnderscore = true;


	private static OWLClass VOID_CATEGORY = OWLManager.getOWLDataFactory().getOWLClass(OWLRDFVocabulary.OWL_VOID.getIRI());


	public IGestalt loadGestalt(String fileName) throws GestaltReaderException 
	{
		IGestalt gestalt = new Gestalt();
		try 
		{
			OWLOntologyManager ontologyDriver = OWLManager.createOWLOntologyManager();
			IRI iri = IRI.create(fileName);
			OWLOntology ontology = ontologyDriver.loadOntologyFromOntologyDocument(iri);

			OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
			OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
			reasoner.precomputeInferences();

			OWLClass gestaltTop = null;
			if (rootCategory != null) 
			{
				if (rootCategory.indexOf('#') == -1) 
				{
					gestaltTop = ontologyDriver.getOWLDataFactory().getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI() + "#" + rootCategory));
				} 
				else 
				{
					gestaltTop = ontologyDriver.getOWLDataFactory().getOWLClass(IRI.create(rootCategory));
				}
			}
			if (gestaltTop == null) 
			{
				IRI classIRI = OWLRDFVocabulary.OWL_THING.getIRI();
				gestaltTop = ontologyDriver.getOWLDataFactory().getOWLClass(classIRI);
			}

			buildHierarchy(reasoner, ontology, gestalt, gestalt.createRoot(labelFor(ontology, gestaltTop)), gestaltTop);

			for (OWLClass category : ontology.getClassesInSignature()) 
			{
				if (!reasoner.isSatisfiable(category)) 
				{
					gestalt.getRoot().addChild(gestalt.createNode(labelFor(ontology, category)));
				}
			}

			generateId(gestalt);
			log.info("Parsed nodes: " + nodesParsed);
		} 
		catch (OWLException exception) 
		{
			final String errMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			log.error(errMessage, exception);
			throw new GestaltReaderException(errMessage, exception);
		}

		return gestalt;
	}

}

