/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.semantics.wordnet;

import java.util.Iterator;
import java.util.List;

import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.data.relationship.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.*;
import net.didion.jwnl.JWNLException;

/**
 *
 * @author Chiranjit Acharya
 */
public class DivergenceMetric
{
	private static final POS[] m_GramClass = { POS.NOUN };
	
	private Dictionary m_KnowledgeBase;
	private static final int STRING_SIZE_MAJOR = 1024;
	private String[][] m_GramList = new String[2][];
	private float[][] m_Divergence = new float[STRING_SIZE_MAJOR + 1][STRING_SIZE_MAJOR + 1];

	public DivergenceMetric(Dictionary dictionary) 
	{
		for (int i = 0; i <= STRING_SIZE_MAJOR; i++) 
		{
			m_Divergence[i][0] = i;
		}
		for (int j = 0; j <= STRING_SIZE_MAJOR; j++) 
		{
			m_Divergence[0][j] = j;
		}	
		this.m_KnowledgeBase = dictionary;
	}

	public float ComputeGramDivergence(String MultiGram1, String MultiGram2) 
	{
		float PayOff = 0.0f;

		m_GramList[0] = MultiGram1.split(" ");
		m_GramList[1] = MultiGram2.split(" ");

		for (int i = 1; i <= m_GramList[0].length; i++) 
		{
			for (int j = 1; j <= m_GramList[1].length; j++) 
			{
				try 
				{
					PayOff = ComputeLemmaDivergence(m_GramList[0][i - 1], m_GramList[1][j - 1]);
				} 
				catch (Exception e) 
				{
					PayOff = 1.0f;
				}

				m_Divergence[i][j] = Math.min(Math.min(m_Divergence[i-1][j] + 1.0f, m_Divergence[i][j-1] + 1.0f), 
								m_Divergence[i-1][j-1] + PayOff);
			}
		}

		return m_Divergence[m_GramList[0].length][m_GramList[1].length];
	}

	public float ComputeLemmaDivergence(String lemma1, String lemma2) 
	{
		IndexWordSet XWordList1, XWordList2;
		IndexWord XWord1, XWord2;

		float Divergence;
		float MinimumDivergence = 1.0f;
		POS p;

		if (lemma1.equals(lemma2)) 
		{
			MinimumDivergence = 0.0f;
		} 
		else 
		{
			try 
			{
				// Compute complete definition for each word (all POS, all senses)
				XWordList1 = this.m_KnowledgeBase.lookupAllIndexWords(lemma1);
				XWordList2 = this.m_KnowledgeBase.lookupAllIndexWords(lemma2);

				// for each POS listed in word classes...
				for (int i = 0; i < m_GramClass.length; i++) 
				{
					p = m_GramClass[i];

					if (XWordList1.isValidPOS(p) && XWordList2.isValidPOS(p)) 
					{
						XWord1 = XWordList1.getIndexWord(p);
						XWord2 = XWordList2.getIndexWord(p);

						// Compute Divergence between words based on this POS
						Divergence = ComputeWordDivergence(XWord1, XWord2);
						if (Divergence < MinimumDivergence) 
						{
							MinimumDivergence = Divergence;
						}
					}
				}
			} 
			catch (JWNLException e) 
			{
				System.err.println("Exception in WordNet module: " + e);
				return 1.0f;
			}
		}

		return MinimumDivergence;
	}

	// Compute Divergence between words that have the same POS
	private float ComputeWordDivergence(IndexWord XWord1, IndexWord XWord2) throws JWNLException
	{
		RelationshipList RelationList;
		AsymmetricRelationship Relation;
		int CommonIndex, RelationDepth, HypernymTreeDepth, CommonRootDepth, CommonLeafDepth;
		float Divergence, MinimumDivergence;
		PointerTargetNode CommonParent;
		Synset SynsetInstance;
		List HypernymTree;

		Divergence = 1.0f;
		
		// for each pairing of word senses...
		for (int i = 1; i <= XWord1.getSenseCount(); i++) 
		{
			for (int j = 1; j <= XWord2.getSenseCount(); j++) 
			{
				// get list of relationships between words
				try 
				{
					RelationList = RelationshipFinder.getInstance().findRelationships
					  (XWord1.getSense(i), XWord2.getSense(j), PointerType.HYPERNYM);
				} 
				catch (Exception e) 
				{
					continue;
				}
				
				int k = 0;
				
				// calculate Divergence for each one
				for (Iterator RelationListItr = RelationList.iterator(); RelationListItr.hasNext(); k++) 
				{
					Relation = (AsymmetricRelationship) RelationListItr.next();
					
					CommonIndex = Relation.getCommonParentIndex();
					
					RelationDepth = Relation.getDepth();
					
					// Divergence between items through common prent (HypernymTreeDepth of furthest word from common prent)
					CommonLeafDepth = Math.max(RelationDepth - CommonIndex, CommonIndex); 

					// get the index node
					CommonParent = (PointerTargetNode) Relation.getNodeList().get(CommonIndex);

					// get the synset of the index node
					SynsetInstance = CommonParent.getSynset();
          
					// get all the hypernyms of the common prent synset
					// returns a list of hypernym chains. probably always one chain, but  better safe...
					HypernymTree = (PointerUtils.getInstance().getHypernymTree(SynsetInstance,50)).toList();

					// get shortest HypernymTreeDepth from root to common prent
					CommonRootDepth = -1;
					for (Iterator HypernymTreeItr = HypernymTree.iterator(); HypernymTreeItr.hasNext();) 
					{
						HypernymTreeDepth = ((List) HypernymTreeItr.next()).size();
						if (CommonRootDepth == -1) 
						{
							CommonRootDepth = HypernymTreeDepth;
						} 
						else 
						{
							if (HypernymTreeDepth < CommonRootDepth) 
							{
								CommonRootDepth = HypernymTreeDepth;
							}
						}
					}

					MinimumDivergence = (float) CommonLeafDepth / (CommonRootDepth + CommonLeafDepth);
					
					if (MinimumDivergence < Divergence)
						Divergence = MinimumDivergence;
				}
			}
		}

		return Divergence;
	}
}
