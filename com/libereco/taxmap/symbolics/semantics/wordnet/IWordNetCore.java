/**
  *  Copyright (C) 2011 Dushkin Digital Media, LLC
  *  500 E 77th Street, Ste. 806
  *  New York, NY 10162
  *
  *  All rights reserved.
  **/

package com.libereco.taxmap.symbolics.semantics.wordnet;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.FileBackedDictionary;

/**
 * Provides library support for application and applet access to Wordnet.
 * @author Chiranjit Acharya
 */
public interface IWordNetCore
{
	public String[] GetAnagrams(String Unigram, String Position, int maxResults);

	public String[] GetAnagrams(String Unigram, String Position);

	public String[] GetRegexMatch(String pattern, String Position, int maxResults)

	public String[] GetRegexMatch(String pattern, String Position)

	public String[] GetSoundsLike(String pattern, String Position, int maxResults)

	public String[] GetSoundsLike(String pattern, String Position)

	public String[] GetWildcardMatch(String pattern, String Position, int maxResults)

	public String[] GetWildcardMatch(String pattern, String Position)

	private List GetSynsetList(int id)

	private Synset GetSynsetAtId(int id)

	public int[] GetSenseIds(String Unigram, String Position)

	public int[] GetSenseIds(IndexWord XWord)

	public String GetGloss(String Unigram, String pos)

	public String[] GetAllGlosses(String Unigram, String pos)

	public String GetGloss(int SenseId)

	public String GetDescription(int SenseId)

	private String GetGloss(Synset SynsetInstance)

	public String GetDescription(String Unigram, String pos)

	public String[] GetAllSynonyms(int SenseId, int maxResults)

	public String[] GetAllSynonyms(int id)

	public String[] GetSynonyms(String Unigram, String Position, int maxResults)

	public String[] GetSynonyms(String Unigram, String Position)

	public String[] GetAllSynonyms(String Unigram, String Position, int maxResults)

	public String[] GetAllSynonyms(String Unigram, String Position)

	public String[] GetCommonParents(String Unigram1, String word2, String pos)

	public Synset GetCommonParent(int id1, int id2) throws JWNLException

	private List GetCommonParentList(String Unigram1, String word2, String pos)

	private Synset GetCommonParent(IndexWord XWord1, IndexWord XWord2) throws JWNLException

	public String[] GetSynset(String Unigram, String pos)

	public String[] GetSynset(String Unigram, String pos, boolean includeOriginal)

	public String[] GetSynset(int id)

	public String[] GetAllSynsets(String Unigram, String Position)

	public int GetSenseCount(String Word, String pos)

	public String[] GetAntonyms(String Word, String pos)

	public String[] GetAntonyms(int id)

	public String[] GetAllAntonyms(String Word, String pos)

	public String[] GetHypernyms(String Word, String Position)

	public String[] GetHypernyms(int id)

	public String[] GetAllHypernyms(String Word, String Position)

	public String[] GetHypernymTree(int id) throws JWNLException

	public String[] GetHyponyms(String Word, String Position)

	public String[] GetHyponyms(int id)

	public String[] GetAllHyponyms(String Word, String Position)

	public String[] GetHyponymTree(int id)

	public String[] GetStems(String query, CharSequence pos)

	public boolean isStem(String Word, CharSequence pos)

	public String[] GetMeronyms(int id)

	public String[] GetAllMeronyms(String query, String pos)

	public String[] GetHolonyms(String query, String pos)

	public String[] GetHolonyms(int id)

	public String[] GetAllHolonyms(String query, String pos)

}

