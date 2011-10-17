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
public class WordNetCore implements IWordNetCore
{
	private static final String VERSION = "031";

	/** String constant for Noun part-of-speech */
	public static final String NOUN = "n";

	/** String constant for Verb part-of-speech */
	public static final String VERB = "v";

	/** String constant for Adjective part-of-speech */
	public static final String ADJ = "a";

	/** String constant for Adverb part-of-speech */
	public static final String ADV = "r";

	/**
	 * @invisible debug flag to toggle verbose output
	 */
	public static final boolean DBUG = false;

	private static final String ROOT = "entity";

	/** @invisible */
	public static String m_WordNetHome;
	public static String m_ConfigFile;

	/** @invisible */
	public static String SLASH;

	protected WordNetFilters m_WNFilters;
	protected Dictionary m_Dictionary;
	protected int m_WordSizeMax = 32;
	protected boolean m_DiscardCompoundWord = true;
	protected boolean m_DiscardUpperCase = true;

	static
	{
		SLASH = System.getProperty("file.separator");
		System.out.println("[INFO] WordNet.version [" + VERSION + "]");
	}

	// -------------------- CONSTRUCTORS ----------------------------

	/**
	 * Constructs an instance of <code>WordNetCore</code> using the included data
	 * files.
	 */
	public WordNetCore()
	{
		this(null);
	}

	/**
	 * Constructs an instance of <code>WordNetCore</code> using the WordNet
	 * installation whose location is specified at <code>WordNetInstallDir</code>.
	 * 
	 * @param WordNetInstallDir
	 *          home directory for a pre-installed WordNet installation.
	 */
	public WordNetCore(String WordNetInstallDir)
	{
		this(WordNetInstallDir, GetDefaultConfFile());
	}

	private static String GetDefaultConfFile()
	{
		// set the locale since the default conf is only English
		Locale.setDefault(Locale.ENGLISH);
		return DEFAULT_CONF;
	}

	private WordNetCore(String WordNetHome, String ConfigFile)
	{
		this.SetWordNetHome(WordNetHome);

		if (DBUG)
			System.err.println("WordNetCore.WordNetCore(" + m_WordNetHome + "," + ConfigFile + ")");

		if (!JWNL.isInitialized())
		{
			try
			{
				InitWordNet(ConfigFile);
			}
			catch (Exception e)
			{
				throw new WordNetException("Unable to load Wordnet " + "with $WORDNET_HOME=" + m_WordNetHome + " & CONF_FILE=" + ConfigFile, e);
			}
		}
		if (this.m_Dictionary == null)
			this.m_Dictionary = Dictionary.getInstance();
	}

	/**
	 * for remote creation only
	 * 
	 * @invisible
	 */
	public static WordNetCore createRemote(Map params)
	{
		return new WordNetCore();
	}

	// METHODS =====================================================

	/**
	 * Returns an iterator over all words of the specified 'PartsOfSpeech'
	 */
	public Iterator iterator(String PartsOfSpeech)
	{
		return GetFilters().lemmaIterator(m_Dictionary, TransformPartsOfSpeech(PartsOfSpeech));
	}


	/**
	 * Returns up to <code>maxResults</code> full anagram matches for the
	 * specified <code>Unigram</code> and <code>PartsOfSpeech</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	public String[] CreatePermutations(String Unigram, String PartsOfSpeech, int maxResults)
	{
		return Filter(ANAGRAMS, Unigram, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns all full anagram matches for the specified <code>Unigram</code> and
	 * <code>PartsOfSpeech</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 */
	public String[] CreatePermutations(String Unigram, String PartsOfSpeech)
	{
		return CreatePermutations(Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * where each contains the given <code>Unigram</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	public String[] CreateContainments(String Unigram, String PartsOfSpeech, int maxResults)
	{
		return Filter(CONTAINS, Unigram, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns all 'contains' matches for the specified <code>Unigram</code> and
	 * <code>PartsOfSpeech</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 */
	public String[] CreateContainments(String Unigram, String PartsOfSpeech)
	{
		return CreateContainments(Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * ending with the given <code>Unigram</code>.
	 * <p>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	public String[] GetEndsWith(String Unigram, String PartsOfSpeech, int maxResults)
	{
		return Filter(ENDS_WITH, Unigram, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * ending with the given <code>Unigram</code>.
	 * <p>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 */
	public String[] GetEndsWith(String Unigram, String PartsOfSpeech)
	{
		return GetEndsWith(Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * starting with the given <code>Unigram</code>.
	 * <p>
	 * Example: 'turn' returns 'turntable'
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	public String[] GetStartsWith(String Unigram, String PartsOfSpeech, int maxResults)
	{
		return Filter(STARTS_WITH, Unigram, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * starting with the given <code>Unigram</code>.
	 * <p>
	 * Example: 'turn' returns 'turntable'
	 * 
	 * @param Unigram
	 * @param PartsOfSpeech
	 */
	public String[] GetStartsWith(String Unigram, String PartsOfSpeech)
	{
		return GetStartsWith(Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * matching the the given regular expression <code>pattern</code>.
	 * <p>
	 * 
	 * @param pattern
	 * @param PartsOfSpeech
	 * @param maxResults
	 * @see java.util.regex.Pattern
	 */
	public String[] GetRegexMatch(String pattern, String PartsOfSpeech, int maxResults)
	{
		return Filter(REGEX_MATCH, pattern, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param pattern
	 * @param PartsOfSpeech
	 * @see java.util.regex.Pattern
	 */
	public String[] GetRegexMatch(String pattern, String PartsOfSpeech)
	{
		return GetRegexMatch(pattern, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * that match the soundex code of the given <code>Unigram</code>.
	 * <p>
	 * 
	 * @param pattern
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	public String[] GetSoundsLike(String pattern, String PartsOfSpeech, int maxResults)
	{
		return Filter(SOUNDS_LIKE, pattern, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * that match the soundex code of the given <code>Unigram</code>.
	 * 
	 * @param pattern
	 * @param PartsOfSpeech
	 */
	public String[] GetSoundsLike(String pattern, String PartsOfSpeech)
	{
		return GetSoundsLike(pattern, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * matching a wildcard <code>pattern</code>,<br>
	 * with * '*' equals any number of characters, <br>
	 * and '?' equals any single character.
	 * 
	 * @param pattern
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	public String[] GetWildcardMatch(String pattern, String PartsOfSpeech, int maxResults)
	{
		return Filter(WILDCARD_MATCH, pattern, TransformPartsOfSpeech(PartsOfSpeech), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>PartsOfSpeech</code>
	 * matching a wildcard <code>pattern</code>,<br>
	 * with '*' representing any number of characters, <br>
	 * and '?' equals any single character..
	 * 
	 * @param pattern
	 * @param PartsOfSpeech
	 */
	public String[] GetWildcardMatch(String pattern, String PartsOfSpeech)
	{
		return GetWildcardMatch(pattern, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified
	 * <code>PartsOfSpeech</code> matching the Filter specified with
	 * <code>filterFlag</code>
	 * 
	 * @param filterFlag
	 * @param Unigram
	 * @param PartsOfSpeech
	 * @param maxResults
	 * @invisible
	 */
	public String[] Filter(int filterFlag, String Unigram, POS PartsOfSpeech, int maxResults)
	{
		return GetStringVectorFromList(GetFilters().Filter(filterFlag, Unigram, PartsOfSpeech, maxResults));
	}

	public String[] Filter(int filterFlag, String Unigram, POS PartsOfSpeech)
	{
		return Filter(filterFlag, Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified matching ANY of
	 * the m_WNFilters specified with <code>filterFlags</code>.
	 * 
	 * @param filterFlags
	 * @param Unigrams
	 * @param PartsOfSpeech
	 * @param maxResults
	 * @invisible
	 */
	public String[] FilterByOR(int[] filterFlags, String[] Unigrams, POS PartsOfSpeech, int maxResults)
	{
		return GetStringVectorFromList(GetFilters().FilterByOR(filterFlags, Unigrams, PartsOfSpeech, maxResults));
	}

	private WordNetFilters GetFilters()
	{
		if (m_WNFilters == null)
			m_WNFilters = new WordNetFilters(this);
		return m_WNFilters;
	}

	public String[] FilterByOR(int[] filterFlag, String[] Unigram, POS PartsOfSpeech)
	{
		return FilterByOR(filterFlag, Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified matching ALL of
	 * the m_WNFilters specified with <code>filterFlags</code>.
	 * 
	 * @param filterFlags
	 * @param Unigrams
	 * @param PartsOfSpeech
	 * @param maxResults
	 */
	private String[] FilterByAND(int[] filterFlags, String[] Unigrams, POS PartsOfSpeech, int maxResults)
	{
		return GetStringVectorFromList(GetFilters().FilterByAND(filterFlags, Unigrams, PartsOfSpeech, maxResults));
	}

	private String[] FilterByAND(int[] filterFlag, String[] Unigram, POS PartsOfSpeech)
	{
		return FilterByAND(filterFlag, Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}


	/**
	 * Called by the parent calling context upon shutdown
	 */
	public void ClearResource()
	{
		if (m_Dictionary != null)
			m_Dictionary.close();
		m_Dictionary = null;
	}

	public void SetWordNetHome(String WordNetHome)
	{
		if (WordNetHome != null)
		{
			if (!(WordNetHome.endsWith("/") || WordNetHome.endsWith("\\")))
				WordNetHome += SLASH;
		}
		WordNetCore.m_WordNetHome = WordNetHome;
		if (m_WordNetHome != null)
			System.out.println("[INFO] WordNet.home=" + m_WordNetHome);
	}

	// -------------------------- MAIN METHODS ----------------------------
	private List GetSynsetList(int id)
	{
		Synset SynsetInstance = GetSynsetAtId(id);

		if (SynsetInstance == null || SynsetInstance.getWordsSize() < 1)
			return null;
		List l = new LinkedList();
		AddLemmas(SynsetInstance.getWords(), l);
		return l;
	}

	private Synset GetSynsetAtId(int id)
	{
		POS PartsOfSpeech = null;
		String idStr = Integer.toString(id);
		int PartsOfSpeechDigit = Integer.parseInt(idStr.substring(0, 1));
		long offset = Long.parseLong(idStr.substring(1));
		switch (PartsOfSpeechDigit) {
		case 9:
			PartsOfSpeech = POS.NOUN;
			break;
		case 8:
			PartsOfSpeech = POS.VERB;
			break;
		case 7:
			PartsOfSpeech = POS.ADJECTIVE;
			break;
		case 6:
			PartsOfSpeech = POS.ADVERB;
			break;
		}
		try
		{
			return m_Dictionary.getSynsetAt(PartsOfSpeech, offset);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
	}

	/**
	 * Returns String[] of unique ids, one for each 'sense' of <code>Unigram</code>
	 * with <code>PartsOfSpeech</code>, or null if none are found.
	 */
	public int[] GetSenseIds(String Unigram, String PartsOfSpeech)
	{
		POS PartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeech);
		IndexWord XWord = FindIndexWord(PartsOfSpeech, Word);
		return GetSenseIds(XWord);
	}

	/**
	 * Returns String[] of unique ids, one for each sense of <code>Unigram</code>
	 * with <code>PartsOfSpeech</code>, or null if none are found.
	 */
	public int[] GetSenseIds(IndexWord XWord)
	{
		int[] result = null;
		try
		{
			int numSenses = XWord.getSenseCount();
			if (XWord == null || numSenses == 0)
				return null;
			long[] offsets = XWord.getSynsetOffsets();
			result = new int[offsets.length];
			for (int i = 0; i < result.length; i++)
				result[i] = toId(XWord.getPOS(), offsets[i]);
		}
		catch (Exception e)
		{
			throw new WordNetException(e);
		}
		return result;
	}

	private int toId(POS wnPartsOfSpeech, long offset)
	{
		int PartsOfSpeechDigit = -1;
		if (wnPartsOfSpeech == POS.NOUN)
			PartsOfSpeechDigit = 9;
		else if (wnPartsOfSpeech == POS.VERB)
			PartsOfSpeechDigit = 8;
		else if (wnPartsOfSpeech == POS.ADJECTIVE)
			PartsOfSpeechDigit = 7;
		else if (wnPartsOfSpeech == POS.ADVERB)
			PartsOfSpeechDigit = 6;
		else
			throw new WordNetException("Invalid POS type: " + wnPartsOfSpeech);
		return Integer.parseInt((Integer.toString(PartsOfSpeechDigit) + offset));
	}

	/**
	 * Returns full gloss for !st sense of 'Unigram' with 'PartsOfSpeech' or null if not found
	 */
	public String GetGloss(String Unigram, String PartsOfSpeech)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, PartsOfSpeech, 1);
		return GetGloss(SynsetInstance);
	}

	/**
	 * Returns glosses for all senses of 'Unigram' with 'PartsOfSpeech', or null if not found
	 */
	public String[] GetAllGlosses(String Unigram, String PartsOfSpeech)
	{
		List glosses = new LinkedList();

		Synset[] SynsetInstances = GetAllSynsets(Unigram, PartsOfSpeech);
		for (int i = 0; i < SynsetInstances.length; i++)
		{
			String gloss = GetGloss(SynsetInstances[i]);
			if (gloss != null)
				glosses.add(gloss);
		}
		return GetStringVectorFromList(glosses);
	}

	/**
	 * Returns full gloss for Unigram with unique <code>SenseId</code>, or null if
	 * not found
	 */
	public String GetGloss(int SenseId)
	{
		Synset SynsetInstance = GetSynsetAtId(SenseId);
		if (SynsetInstance == null)
			return null;
		return GetGloss(SynsetInstance);
	}

	/**
	 * Returns description for Unigram with unique <code>SenseId</code>, or null if
	 * not found
	 */
	public String GetDescription(int SenseId)
	{
		String gloss = GetGloss(SenseId);
		return WordNetUtil.parseDescription(gloss);
	}

	private String GetGloss(Synset SynsetInstance)
	{
		if (SynsetInstance == null)
			return null;
		return SynsetInstance.getGloss();
	}

	/**
	 * Returns description for <code>Unigram</code> with <code>PartsOfSpeech</code> or null if
	 * not found
	 */
	public String GetDescription(String Unigram, String PartsOfSpeech)
	{
		String gloss = GetGloss(Unigram, PartsOfSpeech);
		return WordNetUtil.parseDescription(gloss);
	}

	/**
	 * Returns all examples for 1st sense of <code>Unigram</code> with
	 * <code>PartsOfSpeech</code>, or null if not found
	 */
	public String[] GetExamples(CharSequence Unigram, CharSequence PartsOfSpeech)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, PartsOfSpeech, 1);
		List l = GetExamples(SynsetInstance);
		return GetStringVectorFromList(l);
	}

	/**
	 * Return a random example from the set of examples from all senses of
	 * <code>Unigram</code> with <code>PartsOfSpeech</code>, assuming they contain
	 * <code>Unigram</code>, or else null if not found
	 */
	public String GetAnyExample(CharSequence Unigram, CharSequence PartsOfSpeech)
	{
		String[] all = GetAllExamples(Unigram, PartsOfSpeech);
		int rand = (int) (Math.random() * all.length);
		return all[rand];
	}

	/**
	 * Returns examples for Unigram with unique <code>SenseId</code>, or null if not
	 * found
	 */
	public String[] GetExamples(int SenseId)
	{
		Synset SynsetInstance = GetSynsetAtId(SenseId);
		if (SynsetInstance == null)
			return null;
		return GetStringVectorFromList(GetExamples(SynsetInstance));
	}

	/**
	 * Returns examples for all senses of <code>Unigram</code> with <code>PartsOfSpeech</code>
	 * if they contain the <code>Unigram</code>, else null if not found
	 */
	public String[] GetAllExamples(CharSequence Unigram, CharSequence PartsOfSpeech)
	{
		Synset[] SynsetInstances = GetAllSynsets(Unigram, PartsOfSpeech);
		if (SynsetInstances == null || SynsetInstances.length < 1)
			return null;
		List l = new LinkedList();
		for (int i = 0; i < SynsetInstances.length; i++)
		{
			if (SynsetInstances[i] != null)
			{
				for (int j = 0; j < SynsetInstances.length; j++)
				{
					List examples = GetExamples(SynsetInstances[i]);
					if (examples == null)
						continue;
					for (Iterator k = examples.iterator(); k.hasNext();)
					{
						String example = (String) k.next();

						if (example.indexOf(Unigram.toString()) < 0)
						  continue;
						if (!l.contains(example))
						  l.add(example);
					}
				}
			}
		}
		l.remove(Unigram);
		return GetStringVectorFromList(l);
	}

	private List GetExamples(Synset SynsetInstance)
	{
		String gloss = GetGloss(SynsetInstance);
		return WordNetUtil.parseExamples(gloss);
	}

	/**
	 * Returns an unordered String[] containing the SynsetInstanceset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order), or null if not
	 * found.
	 */
	public String[] GetAllSynonyms(int SenseId, int maxResults)
	{
		String[] result = null;
		Set set = new HashSet();

		result = GetSynset(SenseId);
		this.addSynsetsToSet(result, set);

		result = GetHyponymTree(SenseId);
		this.AddSynsetsToSet(result, set);

		/*
		 * result = GetHypernyms(SenseId); this.AddSynsetsToSet(result, set);
		 */
		// System.err.println("Hypernyms: "+WordNetUtil.asList(result));

		result = GetSimilar(SenseId);
		this.AddSynsetsToSet(result, set);
		// System.err.println("Similar: "+WordNetUtil.asList(result));

		result = GetCoordinates(SenseId);
		this.AddSynsetsToSet(result, set);
		// System.err.println("Coordinates: "+WordNetUtil.asList(result));

		result = GetAlsoSees(SenseId);
		this.AddSynsetsToSet(result, set);
		// System.err.println("AlsoSees: "+WordNetUtil.asList(result));

		// System.err.println("=======================================");

		return setToStrings(set, maxResults, true);
	}

	public String[] GetAllSynonyms(int id)
	{
		return GetAllSynonyms(id, Integer.MAX_VALUE);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>Unigram</code> with <code>PartsOfSpeech</code>, or null if not found
	 */
	public String[] GetSynonyms(String Unigram, String PartsOfSpeech, int maxResults)
	{
		String[] result = null;
		Set set = new HashSet();

		result = GetSynset(Unigram, PartsOfSpeech, false);
		this.AddSynsetsToSet(result, set);
		result = GetHypernyms(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);

		result = GetSimilar(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);

		result = GetAlsoSees(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);

		result = GetCoordinates(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);

		return setToStrings(set, maxResults, true);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>Unigram</code> with <code>PartsOfSpeech</code>, or null if not found
	 */
	public String[] GetSynonyms(String Unigram, String PartsOfSpeech)
	{
		return GetSynonyms(Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>Unigram</code> with <code>PartsOfSpeech</code>, or null if not found
	 */
	public String[] GetAllSynonyms(String Unigram, String PartsOfSpeech, int maxResults)
	{
		final boolean dbug = false;

		String[] result = null;
		Set set = new HashSet();

		result = GetAllSynsets(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Synsets: " + WordNetUtil.asList(result));

		result = GetAllHyponyms(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Hyponyms: " + WordNetUtil.asList(result));
		if (dbug)
			System.err.println("Set: " + WordNetUtil.asList(set));

		result = GetAllSimilar(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Similar: " + WordNetUtil.asList(result));

		result = GetAllAlsoSees(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("AlsoSees: " + WordNetUtil.asList(result));

		result = GetAllCoordinates(Unigram, PartsOfSpeech);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Coordinates: " + WordNetUtil.asList(result));

		return setToStrings(set, maxResults, true);
	}

	public String[] GetAllSynonyms(String Unigram, String PartsOfSpeech)
	{
		return GetAllSynonyms(Unigram, PartsOfSpeech, Integer.MAX_VALUE);
	}

	private void AddSynsetsToSet(String[] s, Set set)
	{
		AddSynsetsToSet(s, set, Integer.MAX_VALUE);
	}

	private void AddSynsetsToSet(String[] s, Set set, int maxResults)
	{
		if (s == null || s.length < 0)
			return;
		for (int i = 0; i < s.length; i++)
		{
			if (s[i].indexOf(SYNSET_DELIM) > 0)
			{
				String[] t = s[i].split(SYNSET_DELIM);
				for (int u = 0; u < t.length; u++)
				{
					set.add(t[u]);
					if (set.size() >= maxResults)
						return;
				}
			}
			else
			{
				set.add(s[i]);
				if (set.size() >= maxResults)
					return;
			}
		}
	}

	private String[] setToStrings(Set set, int maxSize, boolean shuffle)
	{

		if (set == null || set.size() == 0)
			return null;
		List result = new ArrayList(set.size());
		result.addAll(set);
		Collections.shuffle(result);
		int size = Math.min(maxSize, set.size());

		int idx = 0;
		String[] ret = new String[size];
		for (Iterator i = result.iterator(); i.hasNext();)
		{
			ret[idx++] = (String) i.next();
			if (idx == size)
				break;
		}
		return ret;
	}

	/**
	 * Returns String[] of Common Parents for 1st senses of Unigrams with specified
	 * PartsOfSpeech' or null if not found
	 */
	public String[] GetCommonAncestors(String Unigram1, String word2, String PartsOfSpeech)
	{
		List result = GetCommonAncestorList(Unigram1, word2, PartsOfSpeech);
		return GetStringVectorFromList(result);
	}

	/**
	 * Returns common parent for Unigrams with unique ids <code>id1</code>,
	 * <code>id2</code>, or null if either Unigram or no parent is found
	 */
	public Synset GetCommonAncestor(int id1, int id2) throws JWNLException
	{
		Synset SynsetInstance1 = GetSynsetAtId(id1);
		if (SynsetInstance1 == null)
			return null;
		Synset SynsetInstance2 = GetSynsetAtId(id2);
		if (SynsetInstance2 == null)
			return null;
		RelationshipList list = RelationshipFinder.getInstance().findRelationships(SynsetInstance1, SynsetInstance2, PointerType.HYPERNYM);
		AsymmetricRelationship ar = (AsymmetricRelationship) list.get(0);
		PointerTargetNodeList nl = ar.getNodeList();
		PointerTargetNode ptn = (PointerTargetNode) nl.get(ar.getCommonParentIndex());
		return ptn.getSynset();
	}

	private List GetCommonAncestorList(String Unigram1, String Unigram2, String PartsOfSpeech)
	{
		Synset SynsetInstance = null;
		try
		{
			POS wnPartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeech);
			IndexWord XWord1 = FindIndexWord(wnPartsOfSpeech, Unigram1);
			if (XWord1 == null)
				return null;
			IndexWord XWord2 = FindIndexWord(wnPartsOfSpeech, Unigram2);
			if (XWord2 == null)
				return null;
			SynsetInstance = GetCommonAncestor(XWord1, XWord2);
			if (SynsetInstance == null)
				return null;
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
		List l = new ArrayList();
		AddLemmas(SynsetInstance.getWords(), l);
		return l == null || l.size() < 1 ? null : l;
	}

	private Synset GetCommonAncestor(IndexWord XWord1, IndexWord XWord2) throws JWNLException
	{
		if (XWord1 == null || XWord2 == null)
			return null;

		RelationshipList list = null;
		try
		{
			list = RelationshipFinder.getInstance().findRelationships(XWord1.getSense(1), XWord2.getSense(1), PointerType.HYPERNYM);
		}
		catch (NullPointerException e)
		{
		}
		if (list == null)
			return null;

		AsymmetricRelationship ar = (AsymmetricRelationship) list.get(0);
		PointerTargetNodeList nl = ar.getNodeList();

		PointerTargetNode ptn = (PointerTargetNode) nl.get(ar.getCommonParentIndex());
		return ptn.getSynset();
	}

	/**
	 * Returns String[] of Unigrams in synset for first sense of <code>word</code>
	 * with <code>PartsOfSpeech</code>, or null if not found.
	 */
	public String[] GetSynset(String Unigram, String PartsOfSpeech)
	{
		return GetSynset(Unigram, PartsOfSpeech, false);
	}

	/**
	 * Returns String[] of Unigrams in synset for first sense of <code>word</code>
	 * with <code>PartsOfSpeech</code>, or null if not found.
	 */
	public String[] GetSynset(String Unigram, String PartsOfSpeech, boolean InclusionOfSeedOK)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, PartsOfSpeech, 1);
		if (SynsetInstance == null || SynsetInstance.getWordsSize() < 1)
			return null;
		List l = new LinkedList();
		AddLemmas(SynsetInstance.getWords(), l);
		if (!InclusionOfSeedOK)
			l.remove(Unigram);

		return GetStringVectorFromList(l);
	}

	/**
	 * Returns String[] of Synsets for unique id <code>id</code> or null if not found.
	 */
	public String[] GetSynset(int id)
	{
		return GetStringVectorFromList(GetSynsetList(id));
	}

	/**
	 * Returns String[] of Unigrams in each synset for all senses of
	 * <code>Unigram</code> with <code>PartsOfSpeech</code>, or null if not found
	 */
	public String[] GetAllSynsets(String Unigram, String PartsOfSpeech)
	{
		POS PartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeech);
		IndexWord XWord = null;
		List SynsetList = null;
		try
		{
			XWord = FindIndexWord(PartsOfSpeech, Unigram);
			if (XWord == null || XWord.getSenseCount() < 1)
				return null;
			result = new LinkedList();
			for (int i = 1; i <= XWord.getSenseCount(); i++)
			{
				List SynsetInstances = this.GetSynsetAtIndex(XWord, i);
				if (SynsetInstances == null || SynsetInstances.size() < 1)
					continue;
				for (Iterator j = SynsetInstances.iterator(); j.hasNext();)
				{
					String lemma = (String) j.next();
					AddLemma(lemma, SynsetList);
				}
			}
			SynsetList.remove(Unigram);
			return GetStringVectorFromList(SynsetList);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
	}

	private List GetSynsetAtIndex(IndexWord XWord, int index) throws JWNLException
	{
		if (index < 1)
			throw new IllegalArgumentException("Invalid index: " + index);

		if (XWord == null || XWord.getSenseCount() < 1)
			return null;

		List SynsetList = new ArrayList();
		AddLemmas(XWord.getSense(index).getWords(), SynsetList);
		return SynsetList;
	}

	private Synset[] GetAllSynsets(CharSequence Unigram, CharSequence PartsOfSpeech)
	{
		POS PartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeech);
		IndexWord XWord = FindIndexWord(PartsOfSpeech, Word);
		if (XWord == null)
			return null;
		int senseCount = XWord.getSenseCount();
		if (senseCount < 1)
			return null;
		Synset[] SynsetInstances = new Synset[senseCount];
		for (int i = 0; i < SynsetInstances.length; i++)
		{
			try
			{
				SynsetInstances[i] = XWord.getSense(i + 1);
				if (SynsetInstances[i] == null)
					System.err.println("[WARN] WordNet returned null Synset for: " + Word + "/" + PartsOfSpeech);
			}
			catch (JWNLException e)
			{
				throw new WordNetException(e);
			}
		}
		return SynsetInstances;
	}

	private Synset GetSynsetAtIndex(CharSequence Unigram, CharSequence PartsOfSpeech, int i)
	{
		if (i < 1)
			throw new IllegalArgumentException("Invalid index: " + i);
		POS PartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeech);
		IndexWord XWord = FindIndexWord(PartsOfSpeech, Unigram);
		if (XWord == null || XWord.getSenseCount() < i)
			return null;
		try
		{
			return XWord.getSense(i);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
	}

	/**
	 * Return the # of senses (polysemy) for a given Word/PartsOfSpeech. 
	 * 
	 * @return # of senses or -1 if not found
	 */
	public int GetSenseCount(String Unigram, String PartsOfSpeech)
	{
		int senses = -1;
		try
		{
			IndexWord XWord = FindIndexWord(PartsOfSpeech, Unigram);
			if (XWord != null)
				senses = XWord.getSenseCount();
		}
		catch (WordNetException e)
		{
			System.err.println("[WARN] " + e.getMessage());
		}
		return senses;
	}

	/**
	 * Returns String[] of Antonyms for the 1st sense of <code>Word</code> with
	 * <code>PartsOfSpeech</code> or null if not found<br>
	 */
	public String[] GetAntonyms(String Unigram, String PartsOfSpeech)
	{
		return GetPointerTargetsAtIndex(Unigram, PartsOfSpeech, PointerType.ANTONYM, 1);
	}

	/**
	 * Returns String[] of Antonyms for the specified id, or null if not found<br>
	 * Holds for adjectives only (?)
	 */
	public String[] GetAntonyms(int id)
	{
		return GetPointerTargetsAtId(id, PointerType.ANTONYM);
	}

	/**
	 * Returns String[] of Antonyms for the 1st sense of <code>Word</code> with
	 * <code>PartsOfSpeech</code> or null if not found<br>
	 */
	public String[] GetAllAntonyms(String Unigram, String PartsOfSpeech)
	{
		return GetAllPointerTargets(Unigram, PartsOfSpeech, PointerType.ANTONYM);
	}

	/**
	 * Returns Hypernym String[] for all senses of <code>Word</code> with
	 * <code>PartsOfSpeech</code> or null if not found
	 */
	public String[] GetHypernyms(String Unigram, String PartsOfSpeech)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, PartsOfSpeech, 1);
		PointerTargetNodeList NodeList = null;
		try
		{
			NodeList = PointerUtils.getInstance().getDirectHypernyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
		return TransformNodeListToStrings(Unigram, NodeList);
	}

	/**
	 * Returns Hypernym String[] for id, or null if not found
	 */
	public String[] GetHypernyms(int id)
	{
		Synset SynsetInstance = GetSynsetAtId(id);
		PointerTargetNodeList NodeList = null;
		try
		{
			NodeList = PointerUtils.getInstance().getDirectHypernyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
		return TransformNodeListToStrings(null, NodeList);
	}


	/*
	 * Adds the hypernyms for this 'synset' to List 
	 */
	private void GetHypernyms(Synset SynsetInstance, Collection l) throws JWNLException
	{

		PointerTargetNodeList NodeList = null;
		try
		{
			NodeList = PointerUtils.getInstance().getDirectHypernyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		getLemmaSet(NodeList, l);
	}


	/**
	 * Returns an ordered String[] of hypernym-synsets (each a semi-colon
	 * delimited String) up to the root of WordNet for the 1st sense of the Word,
	 * or null if not found
	 */
	public String[] GetAllHypernyms(String Unigram, String PartsOfSpeech)
	{
		try
		{
			IndexWord XWord = FindIndexWord(TransformPartsOfSpeech(PartsOfSpeech), Unigram);
			return GetStringVectorFromList(this.GetAllHypernyms(XWord));
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

	/**
	 * Returns an ordered String[] of hypernym-synsets (each a semi-colon
	 * delimited String) up to the root of WordNet for the <code>id</code>, or
	 * null if not found
	 */
	public String[] GetHypernymTree(int id) throws JWNLException
	{
		Synset SynsetInstance = GetSynsetAtId(id);
		if (SynsetInstance == null)
			return new String[]{ROOT};
		List HypernymList = GetHypernymTree(SynsetInstance);
		return GetStringVectorFromList(HypernymList);
	}

	private List GetAllHypernyms(IndexWord XWord) throws JWNLException
	{
		if (XWord == null)
			return null;
		Synset[] SynsetInstances = XWord.getSenses();
		if (SynsetInstances == null || SynsetInstances.length <= 0)
			return null;

		int i = 0;
		List result = new LinkedList();
		for (; i < SynsetInstances.length; i++)
			GetHypernyms(SynsetInstances[i], result);
		return result == null || result.size() < 1 ? null : result;
	}

	private List GetHypernymTree(Synset SynsetInstance) throws JWNLException
	{
		if (SynsetInstance == null)
			return null;

		PointerTargetTree HypernymTree = null;
		try
		{
			HypernymTree = PointerUtils.getInstance().getHypernymTree(SynsetInstance);

		}
		catch (NullPointerException e)
		{
		}

		if (HypernymTree == null)
			return null;

		List pointerTargetNodeLists = HypernymTree.toList();
		int count = 0; 
		List l = new ArrayList();
		for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext(); count++)
		{
			PointerTargetNodeList NodeList = (PointerTargetNodeList) i.next();
			List strs = this.GetLemmaStrings(NodeList, SYNSET_DELIM, false);
			for (Iterator it = strs.iterator(); it.hasNext();)
			{
				String lemma = (String) it.next();
				if (lemma.length() > 0 && !l.contains(lemma))
					l.add(lemma);
			}
		}
		if (l.size() == 1)
			l.remove(0); 

		return l == null || l.size() < 1 ? null : l;
	}


	/**
	 * Returns Hyponym String[] for 1st sense of <code>Word</code> with
	 * <code>PartsOfSpeech</code> or null if not found
	 */
	public String[] GetHyponyms(String Unigram, String PartsOfSpeech)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, PartsOfSpeech, 1);
		// System.out.println("SynsetInstance="+(SynsetInstance.toString()));
		PointerTargetNodeList NodeList = null;
		try
		{
			PointerUtils UtilInstance = PointerUtils.getInstance();
			NodeList = UtilInstance.getDirectHyponyms(SynsetInstance);

			if (NodeList == null)
				throw new RuntimeException("JWNL Exception: " + Unigram + "/" + PartsOfSpeech);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
		return TransformNodeListToStrings(Unigram, NodeList);
	}

	/**
	 * Returns Hyponym String[] for id, or null if not found
	 */
	public String[] GetHyponyms(int id)
	{
		Synset SynsetInstance = GetSynsetAtId(id);
		PointerTargetNodeList NodeList = null;
		try
		{
			NodeList = PointerUtils.getInstance().getDirectHyponyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
		return TransformNodeListToStrings(null, NodeList);
	}


	/* Adds the hyponyms for this 'synset' to List */
	private void GetHyponyms(Synset SynsetInstance, Collection HyponymList) throws JWNLException
	{
		PointerTargetNodeList NodeList = null;
		try
		{
			PointerUtils UtilInstance = PointerUtils.getInstance();
			NodeList = UtilInstance.getDirectHyponyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		GetLemmaSet(NodeList, HyponymList);
	}

	/**
	 * Returns an unordered String[] of hyponym-synsets (each a colon-delimited String), or null if not found
	 */
	public String[] GetAllHyponyms(String Unigram, String PartsOfSpeech)
	{
		IndexWord XWord = FindIndexWord(TransformPartsOfSpeech(PartsOfSpeech), Unigram);
		List HyponymList = this.GetAllHyponyms(XWord);
		if (HyponymList == null)
			return null;
		HyponymList.remove(Unigram);
		return GetStringVectorFromList(HyponymList);
	}

	private List GetAllHyponyms(IndexWord XWord)
	{
		if (XWord == null)
			return null;

		Synset[] SynsetInstances = null;
		try
		{
			SynsetInstances = XWord.getSenses();
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
		if (SynsetInstances == null || SynsetInstances.length <= 0)
			return null;

		List l = new LinkedList();
		for (int i = 0; i < SynsetInstances.length; i++)
		{
			try
			{
				GetHyponyms(SynsetInstances[i], l);
			}
			catch (JWNLException e)
			{
				e.printStackTrace();
			}
		}

		return l == null || l.size() < 1 ? null : l;
	}

	/**
	 * Returns an unordered String[] of hyponym-synsets (each a colon-delimited
	 * String) representing all paths to leaves in the ontology (the full hyponym
	 * tree), or null if not found
	 */
	public String[] GetHyponymTree(int id)
	{
		Synset SynsetInstance = GetSynsetAtId(id);
		if (SynsetInstance == null)
			return null;

		List HyponymList = null;
		try
		{
			HyponymList = GetHyponymTree(SynsetInstance);
		}
		catch (JWNLException e)
		{
			e.printStackTrace();
		}
		return GetStringVectorFromList(HyponymList);
	}

	private List GetHyponymTree(Synset SynsetInstance) throws JWNLException
	{
		if (SynsetInstance == null)
			return null;

		PointerTargetTree HyponymTree = null;
		try
		{
			HyponymTree = PointerUtils.getInstance().getHyponymTree(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}

		if (HyponymTree == null)
			return null;

		List pointerTargetNodeLists = HyponymTree.toList();

		List LemmaList = new ArrayList();
		for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext();)
		{
			PointerTargetNodeList NodeList = (PointerTargetNodeList) i.next();
			List TmpLemmaList = this.GetLemmaStrings(NodeList, SYNSET_DELIM, true);

			for (Iterator it = TmpLemmaList.iterator(); it.hasNext();)
			{
				String LemmaStr = (String) it.next();
				LemmaStr = PruneterminalCharacters(LemmaStr);
				if (LemmaStr.length() < 2)
					continue;
				if (!LemmaList.contains(LemmaStr))
					LemmaList.add(LemmaStr);
			}
		}

		// remove all the entries from the current SynsetInstance
		Set SynsetInstances = new HashSet();
		AddLemmas(SynsetInstance.getWords(), SynsetInstances);
		OUTER: for (Iterator iter = LemmaList.iterator(); iter.hasNext();)
		{
			String SynsetInstance = (SYNSET_DELIM + (String) iter.next() + SYNSET_DELIM);
			for (Iterator j = SynsetInstances.iterator(); j.hasNext();)
			{
				String lemma = (SYNSET_DELIM + j.next() + SYNSET_DELIM);
				if (SynsetInstance.indexOf(lemma) > -1)
				{
					iter.remove();
					continue OUTER;
				}
			}
		}

		return LemmaList;
	}


	public boolean isNoun(String Unigram)
	{
		return (GetPartsOfSpeechStr(Unigram).indexOf(Character.toString('n')) > -1);
	}

	public boolean isAdjective(String Unigram)
	{
		return (GetPartsOfSpeechStr(Unigram).indexOf(Character.toString('a')) > -1);
	}

	public boolean isVerb(String Unigram)
	{
		return (GetPartsOfSpeechStr(Unigram).indexOf(Character.toString('v')) > -1);
	}

	public boolean isAdverb(String Unigram)
	{
		return (GetPartsOfSpeechStr(Unigram).indexOf(Character.toString('r')) > -1);
	}

	/**
	 * Returns an array of all stems, or null if not found
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetStems(String Expression, CharSequence PartsOfSpeech)
	{
		List tmp = GetStemList(Expression, PartsOfSpeech);
		return GetStringVectorFromList(tmp);
	}

	/**
	 * Returns true if 'Word' exists with 'PartsOfSpeech' and is equal (via String.equals())
	 * to any of its stem forms, else false;
	 * 
	 * @param Word
	 * @param PartsOfSpeech
	 */
	public boolean isStem(String Unigram, CharSequence PartsOfSpeech)
	{
		String[] stems = GetStems(Unigram, PartsOfSpeech);
		if (stems == null)
			return false;
		for (int i = 0; i < stems.length; i++)
			if (Unigram.equals(stems[i]))
				return true;
		return false;
	}

	private List GetStemList(String Expression, CharSequence PartsOfSpeech)
	{
		try
		{
			return m_Dictionary.getMorphologicalProcessor().lookupAllBaseForms(TransformPartsOfSpeech(PartsOfSpeech), Expression);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

	/**
	 * Checks the existence of a 'Word' in the ontology
	 * 
	 * @param Word
	 */
	public boolean exists(String Unigram)
	{
		if (Unigram.indexOf(' ') > -1)
			throw new WordNetException(this, "expecting Word, got phrase: " + Unigram);

		IndexWord[] XWord = null;
		try
		{
			if (m_Dictionary == null)
			{
				System.err.println("NULL DICT");
				System.exit(1);
			}
			IndexWordSet XWords = m_Dictionary.lookupAllIndexWords(Unigram);

			if (XWords == null || XWords.size() < 1)
				return false;

			XWord = XWords.getIndexWordArray();
		}
		catch (JWNLException e)
		{
			System.err.println("[WARN] " + e.getMessage());
		}
		return (XWord != null && XWord.length > 0);
	}

	/**
	 * Check each Word in 'words' and Removes those that don't exist in the
	 * ontology.
	 * 
	 * @param Unigrams
	 */
	public void RemoveNonExistent(Collection Unigrams)
	{
		for (Iterator i = Unigrams.iterator(); i.hasNext();)
		{
			String Unigram = (String) i.next();
			if (!exists(Unigram))
				i.remove();
		}
	}


	private IndexWord FindIndexWord(String PartsOfSpeech, String Unigram)
	{
		return this.FindIndexWord(TransformPartsOfSpeech(PartsOfSpeech), Unigram);
	}

	private POS TransformPartsOfSpeech(String PartsOfSpeech)
	{
		POS wnPartsOfSpeech = WordNetPos.getPos(PartsOfSpeech);
		if (wnPartsOfSpeech == null)
			throw new WordNetException(this, "Invalid PartsOfSpeech-String: '" + PartsOfSpeech + "'");
		return wnPartsOfSpeech;
	}

	private IndexWord FindIndexWord(POS PartsOfSpeech, CharSequence cs)
	{
		if (cs == null)
			return null;
		String Unigram = cs.toString().replace('-', '_');
		IndexWord XWord = null;
		try
		{
			XWord = m_Dictionary.lookupIndexWord(PartsOfSpeech, Unigram);
		}
		catch (JWNLException e)
		{
		}
		return XWord;
	}


	private String TransformLemmaToString(Word[] Unigrams, String delim, boolean TerminalDelimiterOK)
	{
		if (Unigrams == null || Unigrams.length == 0)
			return null;
		List LemmaSet = new ArrayList();
		AddLemmas(Unigrams, LemmaSet);
		String result = WordNetUtil.join(LemmaSet, delim);
		if (TerminalDelimiterOK)
			result = delim + result + delim;
		return result;
	}

	private void AddLemmas(Word[] Unigrams, Collection LemmaSet)
	{
		if (Unigrams == null || Unigrams.length == 0)
			return;
		for (int k = 0; k < Unigrams.length; k++)
			AddLemma(Unigrams[k], LemmaSet);
	}

	private void AddLemma(Word Unigram, Collection LemmaSet)
	{
		this.AddLemma(Unigram.getLemma(), LemmaSet);
	}

	private void AddLemma(String lemma, Collection LemmaSet)
	{
		if (m_DiscardCompoundWord && isCompound(lemma))
			return;
		if (m_DiscardUpperCase && WordNetUtil.startsWithUppercase(lemma))
			return;
		lemma = NormalizeLemma(lemma);
		if (!LemmaSet.contains(lemma))
			LemmaSet.add(lemma);
	}

	private void GetLemmaSet(PointerTargetNodeList source, Collection LemmaSet)
	{
		if (source == null)
			return;

		for (Iterator i = source.iterator(); i.hasNext();)
		{
			PointerTargetNode targetNode = (PointerTargetNode) i.next();
			if (!targetNode.isLexical())
			{
				Synset SynsetInstance = targetNode.getSynset();
				if (SynsetInstance != null)
					AddLemmas(SynsetInstance.getWords(), LemmaSet);
			}
			else
			{
				AddLemma(targetNode.getWord(), LemmaSet);
			}
		}
	}

	private List GetLemmaStrings(PointerTargetNodeList source, String delim, boolean TerminalDelimiterOK)
	{
		List l = new ArrayList();
		for (Iterator i = source.iterator(); i.hasNext();)
		{
			PointerTargetNode targetNode = (PointerTargetNode) i.next();
			if (!targetNode.isLexical())
			{
				Synset SynsetInstance = targetNode.getSynset();
				if (SynsetInstance != null)
				{
					String s = TransformLemmaToString(SynsetInstance.getWords(), delim, TerminalDelimiterOK);
					l.add(s);
				}
			}
			else
			{ 
				List LemmaSet = new ArrayList();
				AddLemma(targetNode.getWord(), LemmaSet);
				System.err.println("ILLEGAL CALL TO TARGET: " + targetNode.getWord());
			}
		}
		return l == null || l.size() < 1 ? null : l;
	}

	private static String PruneterminalCharacters(String s)
	{
		if (s.length() < 2)
			throw new IllegalArgumentException("Invalid length String: '" + s + "'");
		return s.substring(1, s.length() - 1);
	}

	private String NormalizeLemma(String lemma)
	{
		if (lemma.endsWith(")"))
			lemma = lemma.substring(0, lemma.length() - 3);
		lemma = WordNetUtil.replace(lemma, '_', '-');
		return lemma;
	}

	/**
	 * Returns an array of all parts-of-speech ordered according to their polysemy
	 * count, returning the PartsOfSpeech with the most different senses in the first
	 * position, etc.
	 * 
	 */
	public String[] GetPartsOfSpeech(String Unigram)
	{
		IndexWord[] XWords = GetIndexWords(Unigram);
		if (XWords == null)
			return null;
		String[] PartsOfSpeech = new String[XWords.length];
		for (int i = 0; i < XWords.length; i++)
			PartsOfSpeech[i] = XWords[i].getPOS().getKey();
		return PartsOfSpeech;
	}

	public String GetPartsOfSpeech(int id)
	{
		Synset SynsetInstances = GetSynsetAtId(id);
		if (SynsetInstances == null)
			return null;
		return SynsetInstances.getPOS().getKey();
	}

	/**
	 * Returns a String of characters, 1 for each part of speech: ("a" =
	 * adjective, "n" = noun, "r" = adverb, "v" = verb) or an empty String if not found.
	 */
	private String GetPartsOfSpeechStr(String Unigram)
	{
		String PartsOfSpeech = QQ;
		IndexWord[] XWords = GetIndexWords(Unigram);
		if (XWords == null)
			return PartsOfSpeech;
		for (int i = 0; i < XWords.length; i++)
			PartsOfSpeech += XWords[i].getPOS().getKey();
		return PartsOfSpeech;
	}

	/**
	 * Finds the most-common part-of-speech for the Word, according to its
	 * polysemy count, returning the PartsOfSpeech for the version of the Word with the most
	 * different senses.
	 * 
	 */
	public String GetBestPartsOfSpeech(String Unigram)
	{
		IndexWord[] XWords = GetIndexWords(Unigram);
		if (XWords == null || XWords.length < 1)
			return null;
		POS PartsOfSpeech = XWords[0].getPOS();
		if (PartsOfSpeech == POS.NOUN)
			return NOUN;
		if (PartsOfSpeech == POS.VERB)
			return VERB;
		if (PartsOfSpeech == POS.ADVERB)
			return ADV;
		if (PartsOfSpeech == POS.ADJECTIVE)
			return ADJ;
		throw new WordNetException("no PartsOfSpeech for Word: " + Unigram);
	}

	private IndexWord[] GetIndexWords(CharSequence Word)
	{
		List list = new ArrayList();
		for (Iterator itr = POS.getAllPOS().iterator(); itr.hasNext();)
		{
			IndexWord XWord = FindIndexWord((POS) itr.next(), Word.toString());
			if (XWord != null)
			{
				int polysemy = XWord.getSenseCount();
				list.add(new ComparableIndexWord(XWord, polysemy));
			}
		}
		int idx = 0;
		Collections.sort(list);
		IndexWord[] XWords = new IndexWord[list.size()];
		for (Iterator i = list.iterator(); i.hasNext();)
		{
			ComparableIndexWord cXWord = (ComparableIndexWord) i.next();
			XWords[idx++] = cXWord.XWord;
		}
		return XWords;
	}

	class ComparableIndexWord implements Comparable
	{
		IndexWord XWord;
		int polysemy = -1;

		public ComparableIndexWord(IndexWord XWord, int polysemy)
		{
			this.XWord = XWord;
			this.polysemy = polysemy;
		}

		public String toString()
		{
			return XWord.toString() + "polysemy=" + polysemy;
		}

		public int compareTo(Object arg0)
		{
			ComparableIndexWord cXWord = (ComparableIndexWord) arg0;
			if (cXWord.polysemy == polysemy)
				return 0;
			return (cXWord.polysemy > polysemy) ? 1 : -1;
		}
	}

	/**
	 * @param ConfigFile
	 *          WordNet xml-based configuration file full path.
	 * @throws FileNotFoundException
	 */
	private void InitWordNet(String ConfigFile) throws JWNLException
	{
		if (DBUG)
			System.err.println("[INFO] Initializing WordNet: conf='" + ConfigFile + "'");
		InputStream is = WordNetUtil.GetResourceStream(getClass(), ConfigFile);
		if (DBUG)
			System.err.println("[INFO] Initializing WordNet: stream='" + is + "'");
		try
		{
			JWNL.initialize(is);
		}
		catch (RuntimeException e)
		{
			System.err.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * @param Expression
	 * @param l
	 */
	public static String[] GetStringVectorFromList(List l)
	{
		if (l == null || l.size() == 0)
			return null;
		return (String[]) l.toArray(new String[l.size()]);
	}

	/**
	 * @param l
	 */
	private String[] TransformNodeListToStrings(String Expression, PointerTargetNodeList NodeList)
	{
		if (NodeList == null || NodeList.size() == 0)
			return null;
		List l = new LinkedList();
		GetLemmaSet(NodeList, l);
		
		if (Expression != null)
			l.remove(Expression); // remove original
		return GetStringVectorFromList(l);
	}


	/**
	 * Returns a random example from a random word w' <code>PartsOfSpeech</code>
	 * 
	 * @return random example
	 */
	public String GetRandomExample(CharSequence PartsOfSpeech)
	{
		return GetRandomExamples(PartsOfSpeech, 1)[0];
	}

	/**
	 * Returns <code>numExamples</code> random examples from random words w'
	 * <code>PartsOfSpeech</code>
	 * 
	 * @return random examples
	 */
	public String[] GetRandomExamples(CharSequence PartsOfSpeech, int numExamples)
	{
		int idx = 0;
		String[] result = new String[numExamples];
		WHILE: while (true)
		{
			try
			{
				IndexWord XWord = null;
				while (XWord == null || !m_DiscardCompoundWord && WordNetUtil.contains(XWord.getLemma(), " "))
					XWord = m_Dictionary.getRandomIndexWord(TransformPartsOfSpeech(PartsOfSpeech));

				Synset SynsetInstance = XWord.getSenses()[0];
				List l = GetExamples(SynsetInstance);
				if (l == null || l.size() < 1)
					continue;
				for (Iterator i = l.iterator(); i.hasNext();)
				{
					String example = (String) i.next();
					if (example != null)
					{
						result[idx++] = example;
						break;
					}
				}
				if (idx == result.length)
					break WHILE;
			}
			catch (JWNLException e)
			{
				System.err.println("WARN] Unexpected Exception: " + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * Returns <code>count</code> random words w' <code>PartsOfSpeech</code>
	 * 
	 * @return String[] of random words
	 */
	public String[] GetRandomWords(CharSequence PartsOfSpeech, int count)
	{
		String[] result = new String[count];
		for (int i = 0; i < result.length; i++)
			result[i] = GetRandomWord(PartsOfSpeech, true, m_WordSizeMax);
		return result;
	}

	/**
	 * Returns a random stem with <code>PartsOfSpeech</code> and a max length of
	 * <code>this.m_WordSizeMax</code>.
	 * 
	 * @return random word
	 */
	public String GetRandomWord(CharSequence PartsOfSpeech)
	{
		return this.GetRandomWord(PartsOfSpeech, true, m_WordSizeMax);
	}

	/**
	 * Returns a random word with <code>PartsOfSpeech</code> and a maximum of
	 * <code>maxChars</code>.
	 * 
	 * @return a random word or null if none is found
	 */
	public String GetRandomWord(CharSequence PartsOfSpeech, boolean stemsOnly, int maxChars)
	{
		IndexWord XWord = null;
		POS wnPartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeech);
		while (true)
		{
			try
			{
				FileBackedDictionary d;
				XWord = m_Dictionary.getRandomIndexWord(wnPartsOfSpeech);
			}
			catch (JWNLRuntimeException e)
			{
				continue;
			}
			catch (JWNLException e)
			{
				throw new WordNetException(e);
			}
			String word = XWord.getLemma();
			if (m_DiscardCompoundWord && isCompound(Word))
				continue;
			if (Word.length() > maxChars)
				continue;
			if (!stemsOnly || isStem(Word, PartsOfSpeech))
				return XWord.getLemma();
		}
	}

	/**
	 * Returns true if the Word is considered compound (contains either a space,
	 * dash,or underscore), else false
	 */
	static boolean isCompound(String Unigram)
	{
		return Unigram.indexOf(' ') > 0 || Unigram.indexOf('-') > 0 || Unigram.indexOf('_') > 0;
	}

	public Dictionary GetDictionary()
	{
		return m_Dictionary;
	}

	/**
	 * Prints the full hyponym tree to System.out (primarily for debugging).
	 * 
	 * @param SenseId
	 */
	public void DisplayHyponymTree(int SenseId)
	{
		try
		{
			SerializeHyponymTree(System.out, GetSynsetAtId(SenseId));
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
	}

	void SerializeHyponymTree(String Unigram, String PartsOfSpeech) throws JWNLException
	{
		SerializeHyponymTree(System.err, Unigram, PartsOfSpeech);
	}

	void SerializeHyponymTree(PrintStream OutStream, String Unigram, String PartsOfSpeech) throws JWNLException
	{
		IndexWord XWord = FindIndexWord(PartsOfSpeech, Unigram);
		Synset SynsetInstance = XWord.getSense(1);
		this.SerializeHyponymTree(OutStream, SynsetInstance);
	}

	void SerializeHyponymTree(PrintStream OutStream, Synset SynsetInstance) throws JWNLException
	{
		PointerTargetTree hyponyms = null;
		try
		{
			hyponyms = PointerUtils.getInstance().getHyponymTree(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		if (hyponyms == null)
			return;
		Set SynsetInstances = new HashSet();
		AddLemmas(SynsetInstance.getWords(), SynsetInstances);
		OutStream.println("\nHyponyms of synset" + SynsetInstances + ":\n-------------------------------------------");

		hyponyms.print(OutStream);
		OutStream.println();
	}

	/**
	 * Prints the full hypernym tree to System.out (primarily for debugging).
	 * 
	 * @param SenseId
	 */
	public void DisplayHypernymTree(int SenseId)
	{
		try
		{
			Synset SynsetInstance = GetSynsetAtId(SenseId);
			SerializeHypernymTree(System.out, SynsetInstance);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(e);
		}
	}

	void SerializeHypernymTree(String Unigram, String PartsOfSpeech) throws JWNLException
	{
		SerializeHypernymTree(System.err, Unigram, PartsOfSpeech);
	}

	void SerializeHypernymTree(PrintStream OutStream, String Unigram, String PartsOfSpeech) throws JWNLException
	{
		IndexWord XWord = FindIndexWord(PartsOfSpeech, Unigram);
		Synset SynsetInstance = XWord.getSense(1);
		SerializeHypernymTree(OutStream, SynsetInstance);
	}

	void SerializeHypernymTree(PrintStream OutStream, Synset SynsetInstance) throws JWNLException
	{
		PointerTargetTree hypernyms = null;
		try
		{
			hypernyms = PointerUtils.getInstance().getHypernymTree(SynsetInstance);
		}
		catch (StackOverflowError e)
		{
			PointerUtils.getInstance().setOverflowError(true);
			hypernyms = PointerUtils.getInstance().getHypernymTree(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		if (hypernyms == null)
			return;
		Set SynsetInstances = new HashSet();
		AddLemmas(SynsetInstance.getWords(), SynsetInstances);
		OutStream.println("\nHypernyms of synset" + SynsetInstances + ":\n");
		hypernyms.print(OutStream);
		OutStream.println();
	}

	/**
	 * Returns the min distance between any two senses for the 2 words in the
	 * WordNet tree (result normalized to 0-1) with specified PartsOfSpeech, or 1.0 if
	 * either is not found.
	 */
	public float GetDistance(String Lemma1, String Lemma2, String PartsOfSpeechStr)
	{
		if (Lemma1 == null || lemma1.contains(" "))
			return -1;
		if (Lemma2 == null || lemma2.contains(" "))
			return -1;
		
		IndexWordSet XWords1, XWords2;
		IndexWord XWord1, XWord2;

		float Distance = 1.0f;
		float MinimumDistance = 1.0f;
		POS PartsOfSpeech = TransformPartsOfSpeech(PartsOfSpeechStr);

		if (Lemma1.equals(Lemma2))
		{
			MinimumDistance = 0.0f;
		}
		else
		{
			try
			{
				// Get complete definition for each word (all POS, all senses)
				XWords1 = this.m_Dictionary.lookupAllIndexWords(Lemma1);
				XWords2 = this.m_Dictionary.lookupAllIndexWords(Lemma2);

				if (XWords1.isValidPOS(PartsOfSpeech) && XWords2.isValidPOS(PartsOfSpeech))
				{
					XWord1 = XWords1.getIndexWord(PartsOfSpeech);
					XWord2 = XWords2.getIndexWord(PartsOfSpeech);

					// Get distance between words based on this POS
					try
					{
						Distance = GetWordDistance(XWord1, XWord2);
					}
					catch (NullPointerException e)
					{
					}
					if (Distance < MinimumDistance)
					{
						MinimumDistance = Distance;
					}
				}

			}
			catch (JWNLException e)
			{
				System.err.println("[WARN] Exception obtaining distance: " + e);
				return 1.0f;
			}
		}

		return MinimumDistance;
	}

	// Get distance between words that are the same POS
	private float GetWordDistance(IndexWord XWord1, IndexWord XWord2) throws JWNLException, NullPointerException  
	{
		RelationshipList Relations;
		AsymmetricRelationship Relation;
		int AncestorIndex, RelationLength, NodeLevel, RootDepth, LeafDepth;
		float MinimumDistance, CurrDistance;
		PointerTargetNode CommonAncestor;
		Synset SynsetInstance;
		List AncestralHypernymList;
		MinimumDistance = 1.0f;

		int senseCount1 = XWord1.getSenseCount();
		int senseCount2 = XWord2.getSenseCount();

		// for each pairing of word senses...
		for (int i = 1; i <= senseCount1; i++)
		{
			for (int j = 1; j <= senseCount2; j++)
			{
				try
				{
					Relations = RelationshipFinder.getInstance().findRelationships
						(XWord1.getSense(i), XWord2.getSense(j), PointerType.HYPERNYM);
				}
				catch (Exception e)
				{
					continue;
				}

				// calculate distance for each one
				for (Iterator RelationIter = Relations.iterator(); RelationIter.hasNext();)
				{
					Relation = (AsymmetricRelationship) RelationIter.next();
					AncestorIndex = Relation.getCommonParentIndex();
					RelationLength = Relation.getDepth();

					// distance between items going through the common ancestor
					// (NodeLevel of furthest word from common ancestor)
					LeafDepth = Math.max(RelationLength - AncestorIndex, AncestorIndex);

					CommonAncestor = (PointerTargetNode) Relation.getNodeList().get(AncestorIndex);
					SynsetInstance = CommonAncestor.getSynset();
					
					// get all the hypernyms of the CPI synset
					AncestralHypernymList = (PointerUtils.getInstance().getHypernymTree(SynsetInstance)).toList();

					// get shortest NodeLevel from root to common ancestor
					RootDepth = -1;
					for (Iterator AncestralHypernymListIter = AncestralHypernymList.iterator(); AncestralHypernymListIter.hasNext();)
					{
						NodeLevel = ((List) AncestralHypernymListIter.next()).size();
						if (RootDepth == -1)
						{
							RootDepth = NodeLevel;
						}
						else
						{
							if (NodeLevel < RootDepth)
							{
								RootDepth = NodeLevel;
							}
						}
					}

					// normalize the MinimumDistance
					CurrDistance = (float) LeafDepth / (RootDepth + LeafDepth);
					if (CurrDistance < MinimumDistance)
					{
						MinimumDistance = CurrDistance;
					}
				}
			}
		}
		return MinimumDistance;
	}

	/**
	 * Returns array of whole-to-part relationships for 1st sense of word/PartsOfSpeech, or null if not found
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetMeronyms(String Expression, String PartsOfSpeech)
	{
		try
		{
			Synset SynsetInstance = GetSynsetAtIndex(Expression, PartsOfSpeech, 1);
			if (SynsetInstance == null)
				return null;

			PointerTargetNodeList NodeList = null;
			try
			{
				NodeList = PointerUtils.getInstance().getMeronyms(SynsetInstance);
			}
			catch (NullPointerException e)
			{
			}
			return TransformNodeListToStrings(Expression, NodeList);
		}

		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

	/**
	 * Returns array of whole-to-part relationships for id, or null if not found
	 */
	public String[] GetMeronyms(int id)
	{
		try
		{
			Synset SynsetInstance = GetSynsetAtId(id);
			if (SynsetInstance == null)
				return null;

			PointerTargetNodeList NodeList = null;
			try
			{
				NodeList = PointerUtils.getInstance().getMeronyms(SynsetInstance);
			}
			catch (NullPointerException e)
			{
			}
			return TransformNodeListToStrings(null, NodeList);
		}

		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

	/**
	 * Returns array of whole-to-part relationships for all senses of word/PartsOfSpeech, or null if not found
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetAllMeronyms(String Expression, String PartsOfSpeech)
	{
		try
		{
			Synset[] SynsetInstances = GetAllSynsets(Expression, PartsOfSpeech);
			if (SynsetInstances == null)
				return null;

			List MeronymList = new LinkedList();
			for (int i = 0; i < SynsetInstances.length; i++)
			{
				if (SynsetInstances[i] == null)
					continue;

				PointerTargetNodeList NodeList = null;
				try
				{
					NodeList = PointerUtils.getInstance().getMeronyms(SynsetInstances[i]);
				}
				catch (NullPointerException e)
				{
				}
				GetLemmaSet(NodeList, MeronymList);
			}
			MeronymList.remove(Expression); 
			return GetStringVectorFromList(MeronymList);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

	/**
	 * Returns part-to-whole relationships for 1st sense of word/PartsOfSpeech, or none if not found
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetHolonyms(String Expression, String PartsOfSpeech)
	{
		PointerTargetNodeList NodeList = null;
		try
		{
			Synset SynsetInstance = GetSynsetAtIndex(Expression, PartsOfSpeech, 1);
			if (SynsetInstance == null)
				return null;

			NodeList = PointerUtils.getInstance().getHolonyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}

		return TransformNodeListToStrings(Expression, NodeList);
	}

	/**
	 * Returns part-to-whole relationships for 1st sense of word/PartsOfSpeech, or none if not found
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetHolonyms(int id)
	{
		PointerTargetNodeList NodeList = null;
		try
		{
			Synset SynsetInstance = GetSynsetAtId(id);
			if (SynsetInstance == null)
				return null;

			NodeList = PointerUtils.getInstance().getHolonyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
		return TransformNodeListToStrings(null, NodeList);
	}

	/**
	 * Returns part-to-whole relationships for all sense of word/PartsOfSpeech, or none if not found
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetAllHolonyms(String Expression, String PartsOfSpeech)
	{
		try
		{
			Synset[] SynsetInstances = GetAllSynsets(Expression, PartsOfSpeech);
			if (SynsetInstances == null)
				return null;

			List HolonymList = new LinkedList();
			for (int i = 0; i < SynsetInstances.length; i++)
			{
				if (SynsetInstances[i] == null)
					continue;
				PointerTargetNodeList NodeList = null;
				try
				{
					NodeList = PointerUtils.getInstance().getHolonyms(SynsetInstances[i]);
				}
				catch (NullPointerException e)
				{
				}
				GetLemmaSet(NodeList, HolonymList);
			}
			HolonymList.remove(Expression); 
			return GetStringVectorFromList(HolonymList);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

	/**
	 * Returns coordinate terms for 1st sense of word/PartsOfSpeech, or null if not found<br>
	 * X is a coordinate term of Y if there exists a term Z which is the hypernym of both X and Y.
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetCoordinates(String Expression, String PartsOfSpeech)
	{
		String[] result = null;
		try
		{
			Synset SynsetInstance = GetSynsetAtIndex(Expression, PartsOfSpeech, 1);
			if (SynsetInstance == null)
				return null;
			PointerTargetNodeList NodeList = PointerUtils.getInstance().getCoordinateTerms(SynsetInstance);
			if (NodeList != null)
				result = TransformNodeListToStrings(Expression, NodeList);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
		return result;
	}

	/**
	 * Returns String[] of Coordinates for the specified id, or null if not found<br>
	 */
	public String[] GetCoordinates(int id)
	{
		String[] result = null;
		try
		{
			Synset SynsetInstance = GetSynsetAtId(id);
			if (SynsetInstance == null)
				return null;
			PointerTargetNodeList NodeList = PointerUtils.getInstance().getCoordinateTerms(SynsetInstance);
			if (NodeList != null)
				result = TransformNodeListToStrings(null, NodeList);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
		return result;
	}

	/**
	 * Returns coordinate terms for all sense of word/PartsOfSpeech, or null if not found<br>
	 * X is a coordinate term of Y if there exists a term Z which is the hypernym of both X and Y.
	 * 
	 * @param Expression
	 * @param PartsOfSpeech
	 */
	public String[] GetAllCoordinates(String Expression, String PartsOfSpeech)
	{
		try
		{
			Synset[] SynsetInstances = GetAllSynsets(Expression, PartsOfSpeech);
			if (SynsetInstances == null)
				return null;
			List CoordinateList = new LinkedList();
			for (int i = 0; i < SynsetInstances.length; i++)
			{
				if (SynsetInstances[i] == null)
					continue;
				PointerTargetNodeList NodeList = null;
				try
				{
					NodeList = PointerUtils.getInstance().getCoordinateTerms(SynsetInstances[i]);
				}
				catch (NullPointerException e)
				{
				}
				GetLemmaSet(NodeList, CoordinateList);
			}
			CoordinateList.remove(Expression);
			return GetStringVectorFromList(CoordinateList);
		}
		catch (JWNLException e)
		{
			throw new WordNetException(this, e);
		}
	}

}
