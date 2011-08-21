package com.libereco.taxmap.symbolics.semantics.wordnet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import com.libereco.taxmap.symbolics.config.Environment;
import com.libereco.taxmap.symbolics.config.EnvironmentException;
import com.libereco.taxmap.symbolics.data.ling.IDenotation;
import com.libereco.taxmap.symbolics.data.ling.Sense;
import com.libereco.taxmap.symbolics.data.map.IRelationInstance;
import com.libereco.taxmap.symbolics.semantics.*;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.log4j.Logger;

/**
 * Implements a Semantics and Denotation Comparator using WordNet.
 */
public class WordNetReader extends Environment implements ISemantics, IDenotationComparator 
{

	private static final Logger log = Logger.getLogger(WordNetReader.class);

	private static final String JWNL_CONFIG_PATH = "JWNLPropertiesPath";
	private Dictionary _dictionary = null;

	private Map<String, Character> _meaningMap;

	public WordNetReader() 
	{
		_meaningMap = new HashMap<String, Character>();
	}

	@Override
	public boolean setProperties(Properties attributes) throws EnvironmentException 
	{
		boolean attribFlag = super.setProperties(attributes);
		if (attribFlag) 
		{
			if (attributes.containsKey(JWNL_CONFIG_PATH)) 
			{
				// initialize JWNL (this must be done before JWNL library can be used)
				try 
				{
					final String jwnlConfig = attributes.getProperty(JWNL_CONFIG_PATH);
					log.info("Initializing JWNL from " + jwnlConfig);
					JWNL.initialize(new FileInputStream(jwnlConfig));
					_dictionary = Dictionary.getInstance();
				} 
				catch (JWNLException e) 
				{
					final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
					log.error(errorString, e);
					throw new EnvironmentException(errorString, e);
				} 
				catch (FileNotFoundException e) 
				{
					final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
					log.error(errorString, e);
					throw new EnvironmentException(errorString, e);
				}
			} 
			else 
			{
				final String errorString = "JWNL configuration path not found " + JWNL_CONFIG_PATH;
				log.error(errorString);
				throw new EnvironmentException(errorString);
			}
		}
		return attribFlag;
	}

	public List<IDenotation> getDenotationList(String expression) throws SemanticsException 
	{
		List<IDenotation> meaningList = new ArrayList<IDenotation>();
		try 
		{
			IndexWordSet wordSet = _dictionary.lookupAllIndexWords(expression);
			if (null != wordSet && 0 < wordSet.size()) 
			{
				//Looping on all words in indexWordSet
				for (int i = 0; i < wordSet.getIndexWordArray().length; i++) 
				{
					IndexWord word = wordSet.getIndexWordArray()[i];
					for (int j = 0; j < word.getSenseCount(); j++) 
					{
						Synset synset = word.getSenses()[j];
						meaningList.add(new Sense(synset.getPOS().getKey().charAt(0), synset.getOffset()));
					}
				}
			}
		} 
		catch (JWNLException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new SemanticsException(errorString, e);
		}
		return meaningList;
	}

	public String getLemmatization(String derivation) throws SemanticsException 
	{
		try 
		{
			String lemmaForm = derivation;
			IndexWordSet wordSet = _dictionary.lookupAllIndexWords(derivation);
			if (null != wordSet) 
			{
				for (IndexWord indexWord : wordSet.getIndexWordArray()) 
				{
					String word = indexWord.getLemma();
					if (null != word) 
					{
						lemmaForm = word;
						break;
					}
				}
			}
			return lemmaForm;
		} 
		catch (JWNLException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new SemanticsException(errorString, e);
		}
	}

	public boolean isEqual(String expression1, String expression2) throws SemanticsException 
	{
		try 
		{
			IndexWordSet wordSet1 = _dictionary.lookupAllIndexWords(expression1);
			IndexWordSet wordSet2 = _dictionary.lookupAllIndexWords(expression2);
			if ((wordSet1 == null) || (wordSet2 == null) || (wordSet1.size() < 1) || (wordSet2.size() < 1)) 
			{
				return false;
			} 
			else 
			{
				IndexWord[] wordArray1 = wordSet1.getIndexWordArray();
				IndexWord[] wordArray2 = wordSet2.getIndexWordArray();
				for (IndexWord word1 : wordArray1) 
				{
					for (IndexWord word2 : wordArray2) 
					{
						if (word1.equals(word2))
							return true;
					}
				}
			}
		} 
		catch (JWNLException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new SemanticsException(errorString, e);
		}
		return false;
	}

	public ISynset getISynset(IDenotation source) throws SemanticsException 
	{
		return new WordNetSynset(getSynset(source));
	}

	public char getRelation(List<IDenotation> sourceMeaningList, List<IDenotation> targetMeaningList) throws DenotationException 
	{
		for (IDenotation sourceMeaning : sourceMeaningList) 
		{
			for (IDenotation targetMeaning : targetMeaningList) 
			{
				if (getSemanticRelation(sourceMeaning, targetMeaning, IRelationInstance.EQUIVALENCE)) 
				{
					return IRelationInstance.EQUIVALENCE;
				}
			}
		}

		for (IDenotation sourceMeaning : sourceMeaningList) 
		{
			for (IDenotation targetMeaning : targetMeaningList) 
			{
				if (getSemanticRelation(sourceMeaning, targetMeaning, IRelationInstance.LESS_GENERAL)) 
				{
					return IRelationInstance.LESS_GENERAL;
				}
			}
		}

		for (IDenotation sourceMeaning : sourceMeaningList) 
		{
			for (IDenotation targetMeaning : targetMeaningList) 
			{
				if (getSemanticRelation(sourceMeaning, targetMeaning, IRelationInstance.MORE_GENERAL)) 
				{
					return IRelationInstance.MORE_GENERAL;
				}
			}
		}

		for (IDenotation sourceMeaning : sourceMeaningList) 
		{
			for (IDenotation targetMeaning : targetMeaningList) 
			{
				if (getSemanticRelation(sourceMeaning, targetMeaning, IRelationInstance.DISJOINT)) 
				{
					return IRelationInstance.DISJOINT;
				}
			}
		}
		return IRelationInstance.IDK;
	}

	/**
	 * Method which returns whether particular type of relation between
	 * two senses holds(according to semantics).
	 * It uses cache to store already obtained relations in order to improve performance.
	 *
	 */
	private boolean getSemanticRelation(IDenotation sourceMeaning, IDenotation targetMeaning, char symbol) throws DenotationException 
	{
		final String meaningDuplet = sourceMeaning.toString() + targetMeaning.toString();
		Character relationSymbol = _meaningMap.get(meaningDuplet);

		if (null == relationSymbol) 
		{
			if (isSourceSynonymToTarget(sourceMeaning, targetMeaning)) 
			{
				_meaningMap.put(meaningDuplet, IRelationInstance.EQUIVALENCE);
				return symbol == IRelationInstance.EQUIVALENCE;
			} 
			else 
			{
				if (isSourceAntonymToTarget(sourceMeaning, targetMeaning)) 
				{
					_meaningMap.put(meaningDuplet, IRelationInstance.DISJOINT);
					return symbol == IRelationInstance.DISJOINT;
				} 
				else 
				{
					if (isSourceLessGeneralThanTarget(sourceMeaning, targetMeaning)) 
					{
						_meaningMap.put(meaningDuplet, IRelationInstance.LESS_GENERAL);
						return symbol == IRelationInstance.LESS_GENERAL;
					} 
					else 
					{
						if (isSourceMoreGeneralThanTarget(sourceMeaning, targetMeaning)) 
						{
							_meaningMap.put(meaningDuplet, IRelationInstance.MORE_GENERAL);
							return symbol == IRelationInstance.MORE_GENERAL;
						} 
						else 
						{
							_meaningMap.put(meaningDuplet, IRelationInstance.IDK);
							return false;
						}
					}
				}
			}
		} 
		else 
		{
			return symbol == relationSymbol;
		}
	}

	public boolean isSourceSynonymToTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException 
	{
		if (sourceMeaning.equals(targetMeaning)) 
		{
			return true;
		}
		try 
		{
			Synset sourceSynset = getSynset(sourceMeaning);
			Synset targetSynset = getSynset(targetMeaning);

			RelationshipList relationList = RelationshipFinder.getInstance().findRelationships(sourceSynset, targetSynset, PointerType.SIMILAR_TO);
			if (list.size() > 0) 
			{
				if (('a' == sourceMeaning.getPosition()) || ('a' == targetMeaning.getPosition())) 
				{
					return (((Relationship) relationList.get(0)).getDepth() == 0);
				} 
				else 
				{
					return true;
				}
			}
		} 
		catch (JWNLException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new DenotationException(errorString, e);
		} 
		catch (SemanticsException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new DenotationException(errorString, e);
		} 
		return false;
	}

	public boolean isSourceAntonymToTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException 
	{
		if (sourceMeaning.equals(targetMeaning)) 
		{
			return false;
		}
		try 
		{
			Synset sourceSynset = getSynset(sourceMeaning);
			Synset targetSynset = getSynset(targetMeaning);

			if ('n' != sourceMeaning.getPosition() || 'n' != targetMeaning.getPosition()) 
			{
				RelationshipList relationList = RelationshipFinder.getInstance().findRelationships(sourceSynset, targetSynset, PointerType.ANTONYM);
				if (relationList.size() > 0) 
				{
					return true;
				}
			}
		} 
		catch (JWNLException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new DenotationException(errorString, e);
		} 
		catch (SemanticsException e) 
		{
			final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new DenotationException(errorString, e);
		} 
		return false;
	}

	/**
	 * Checks whether source sense less general than target.
	 */
	public boolean isSourceLessGeneralThanTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException 
	{
		return isSourceMoreGeneralThanTarget(targetMeaning, sourceMeaning);
	}

	public boolean isSourceMoreGeneralThanTarget(IDenotation sourceMeaning, IDenotation targetMeaning) throws DenotationException 
	{
		if (('n' == sourceMeaning.getPosition() && 'n' == targetMeaning.getPosition()) || ('v' == sourceMeaning.getPosition() && 'v' == targetMeaning.getPosition())) 
		{
			if (sourceMeaning.equals(targetMeaning)) 
			{
				return false;
			}
			try 
			{
				Synset sourceSynset = getSynset(sourceMeaning);
				Synset targetSynset = getSynset(targetMeaning);

				RelationshipList list = RelationshipFinder.getInstance().findRelationships(sourceSynset, targetSynset, PointerType.HYPERNYM);
				if (!isUnidirectionalList(list)) 
				{
					PointerTargetTree ptt = PointerUtils.getInstance().getInheritedMemberHolonyms(targetSynset);
					PointerTargetNodeList ptnl = PointerUtils.getInstance().getMemberHolonyms(targetSynset);
					if (!traverseTree(ptt, ptnl, sourceSynset)) 
					{
						ptt = PointerUtils.getInstance().getInheritedPartHolonyms(targetSynset);
						ptnl = PointerUtils.getInstance().getPartHolonyms(targetSynset);
						if (!traverseTree(ptt, ptnl, sourceSynset)) 
						{
							ptt = PointerUtils.getInstance().getInheritedSubstanceHolonyms(targetSynset);
							ptnl = PointerUtils.getInstance().getSubstanceHolonyms(targetSynset);
							if (traverseTree(ptt, ptnl, sourceSynset)) 
							{
								return true;
							}
						} 
						else 
						{
							return true;
						}
					} 
					else 
					{
						return true;
					}
				} 
				else 
				{
					return true;
				}
			} 
			catch (JWNLException e) 
			{
				final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
				log.error(errorString, e);
				throw new DenotationException(errorString, e);
			} 
			catch (SemanticsException e) 
			{
				final String errorString = e.getClass().getSimpleName() + ": " + e.getMessage();
				log.error(errorString, e);
				throw new DenotationException(errorString, e);
			} 
		}
		return false;
	}

	/**
	 * traverses PointerTargetTree.
	 */
	private static boolean traverseTree(PointerTargetTree syn, PointerTargetNodeList ptnl, Synset sourceMeaning) 
	{
		java.util.List MGListsList = syn.toList();
		for (Object aMGListsList : MGListsList) 
		{
			PointerTargetNodeList MGList = (PointerTargetNodeList) aMGListsList;
			for (Object aMGList : MGList) 
			{
				Synset toAdd = ((PointerTargetNode) aMGList).getSynset();
				if (toAdd.equals(sourceMeaning)) 
				{
					return true;
				}
			}
		}
		for (Object aPtnl : ptnl) 
		{
			Synset toAdd = ((PointerTargetNode) aPtnl).getSynset();
			if (toAdd.equals(sourceMeaning)) 
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks unidirectionality of semantic relations in the list.
	 *
	 * @param list a list with relations
	 * @return true if relations in the list are unidirectional
	 */
	private boolean isUnidirectionalList(RelationshipList relationList) 
	{
		if (relationList.size() > 0) 
		{
			try 
			{
				if (((AsymmetricRelationship) relationList.get(0)).getCommonParentIndex() == 0) 
				{
					return true;
				}
			} 
			catch (java.lang.IndexOutOfBoundsException ex) 
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a synset for a meaning.
	 */
	private Synset getSynset(IDenotation sourceMeaning) throws SemanticsException 
	{
		try 
		{
			POS sourcePosition = POS.getPOSForKey(Character.toString(sourceMeaning.getPosition()));
			return _dictionary.getSynsetAt(sourcePosition, sourceMeaning.getId());
		} 
		catch (JWNLException e) 
		{
			final String errorString = "Incorrect synset id: " + sourceMeaning + ". Error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
			log.error(errorString, e);
			throw new SemanticsException(errorString, e);
		}
	}
}

