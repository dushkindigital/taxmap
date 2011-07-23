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
 * Reads a gestalt using W3C standardized OWL API
 */
public class OWLGestaltReader extends GestaltReader implements IGestaltReader 
{
	private static final Logger readLog = Logger.getLogger(OWLGestaltReader.class);

	// class to start from, if not specified, a Thing will be used
	private static final String ROOT_CATEGORY_KEY = "rootCategory";
	private String rootCategory = null;

	// whether to exclude Nothing class
	private static final String INCLUDE_ALL_KEY = "allInclusive";
	private boolean allInclusive = true;

	// whether to replace _
	private static final String MAKE_NATURAL_KEY = "makeNatural";
	private boolean makeNatural = true;


	private static OWLClass VOID_CATEGORY = OWLManager.getOWLDataFactory().getOWLClass(OWLRDFVocabulary.OWL_VOID.getIRI());

	private class TagIdentifier implements OWLAnnotationObjectVisitor 
	{
		String tag;

		public TagIdentifier() 
		{
			tag = null;
		}

		public void visit(OWLAnnotation annotation) 
		{
			if (annotation.getProperty().isLabel()) 
			{
				OWLLiteral literal = (OWLLiteral) annotation.getValue();
				tag = literal.getLiteral();
			}

		}

		public String getTag() 
		{
			return tag;
		}
	}


	public boolean setProperties(Properties features) throws EnvironmentException 
	{
		boolean result = super.setProperties(features);
		if (result) 
		{
			if (features.containsKey(ROOT_CATEGORY_KEY)) 
			{
				rootCategory = features.getProperty(ROOT_CATEGORY_KEY);
			}

			if (features.containsKey(INCLUDE_ALL_KEY)) 
			{
				allInclusive = Boolean.parseBoolean(features.getProperty(INCLUDE_ALL_KEY));
			}

			if (features.containsKey(MAKE_NATURAL_KEY)) 
			{
				makeNatural = Boolean.parseBoolean(features.getProperty(MAKE_NATURAL_KEY));
			}
		}
		return result;
	}

	public void induceHierarchy(OWLReasoner reasoner, OWLOntology ontology, IGestalt gestalt, INode root, OWLClass category) throws OWLException 
	{
		if (reasoner.isSatisfiable(category)) 
		{
			if (reasoner.getSuperClasses(category, true).getFlattened().size() > 1) 
			{
				if (readLog.isEnabledFor(Level.WARN)) 
				{
					readLog.warn("Multiple superclasses:\t" + category.toStringID());
				}
			}
			for (OWLClass childCategory : reasoner.getSubClasses(category, true).getFlattened()) 
			{
				if (!allInclusive || !VOID_CATEGORY.equals(childCategory)) 
				{
					if (!childCategory.equals(category)) 
					{
						INode childNode = gestalt.createNode(generateTag(ontology, childCategory));
						root.addChild(childNode);
						induceHierarchy(reasoner, ontology, gestalt, childNode, childCategory);
					} 
					else 
					{
						if (readLog.isEnabledFor(Level.WARN)) 
						{
							readLog.warn("Subclass equal to class:\t" + category.toStringID());
						}
					}
				}
			}
		}
	}

	public IGestalt readGestalt(String fileName) throws GestaltReaderException 
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

			OWLClass gestaltTopCategory = null;
			if (rootCategory != null) 
			{
				if (rootCategory.indexOf('#') == -1) 
				{
					gestaltTopCategory = ontologyDriver.getOWLDataFactory().getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI() + "#" + rootCategory));
				} 
				else 
				{
					gestaltTopCategory = ontologyDriver.getOWLDataFactory().getOWLClass(IRI.create(rootCategory));
				}
			}
			if (gestaltTopCategory == null) 
			{
				IRI categoryIRI = OWLRDFVocabulary.OWL_THING.getIRI();
				gestaltTopCategory = ontologyDriver.getOWLDataFactory().getOWLClass(categoryIRI);
			}

			induceHierarchy(reasoner, ontology, gestalt, gestalt.createRoot(generateTag(ontology, gestaltTopCategory)), gestaltTopCategory);

			for (OWLClass category : ontology.getClassesInSignature()) 
			{
				if (!reasoner.isSatisfiable(category)) 
				{
					gestalt.getRoot().addChild(gestalt.createNode(generateTag(ontology, category)));
				}
			}

			generateId(gestalt);
			readLog.info("Explored nodes: " + ExploredNodeNum);
		} 
		catch (OWLException exception) 
		{
			final String errorMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();
			readLog.error(errorMessage, exception);
			throw new GestaltReaderException(errorMessage, exception);
		}

		return gestalt;
	}

	public String getDescriptor() 
	{
		return IReader.OWL_FILES;
	}

	public IReader.ReaderType getType() 
	{
		return IReader.ReaderType.FILE;
	}

	private String generateTag(OWLOntology ontology, OWLClass category) 
	{
		String tag;
		TagIdentifier identifier = new TagIdentifier();
		Set<OWLAnnotation> annotationSet = category.getAnnotations(ontology);
		for (OWLAnnotation annotation : annotationSet) 
		{
			annotation.accept(identifier);
		}

		if (identifier.getTag() != null) 
		{
			tag = identifier.getTag();
		} 
		else 
		{
			if (category.getIRI().getFragment() != null && category.getIRI().getFragment() != "") 
			{
				tag = category.getIRI().getFragment();
			} 
			else 
			{
				tag = category.getIRI().toString();
			}
		}
		if (makeNatural) 
		{
			tag = tag.replaceAll("_", " ");
		}
		return tag;
	}
}

