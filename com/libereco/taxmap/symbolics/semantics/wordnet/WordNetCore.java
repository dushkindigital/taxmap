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

	public static final boolean DBUG = false;

	private static final String ROOT = "entity";

	public static String m_WordNetHome;
	public static String m_ConfigFile;

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
				throw new WordNetError("Unable to load Wordnet " + "with $WORDNET_HOME=" + m_WordNetHome + " & CONF_FILE=" + ConfigFile, e);
			}
		}
		if (this.m_Dictionary == null)
			this.m_Dictionary = Dictionary.getInstance();
	}

	/**
	 * for remote creation only
	 */
	public static WordNetCore createRemote(Map params)
	{
		return new WordNetCore();
	}


	/**
	 * Returns an iterator over all words of the specified 'pos'
	 */
	public Iterator iterator(String pos)
	{
		return GetFilters().lemmaIterator(m_Dictionary, convertPos(pos));
	}


	/**
	 * Returns up to <code>maxResults</code> full anagram matches for the
	 * specified <code>Unigram</code> and <code>pos</code>
	 * 
	 * @param Unigram
	 * @param Position
	 * @param maxResults
	 */
	public String[] GetAnagrams(String Unigram, String Position, int maxResults)
	{
		return Filter(ANAGRAMS, Unigram, convertPos(Position), maxResults);
	}

	/**
	 * Returns all full anagram matches for the specified <code>Unigram</code> and
	 * <code>pos</code>
	 * 
	 * @param Unigram
	 * @param Position
	 */
	public String[] GetAnagrams(String Unigram, String Position)
	{
		return GetAnagrams(Unigram, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * where each contains the given <code>Unigram</code>
	 * 
	 * @param Unigram
	 * @param Position
	 * @param maxResults
	 */
	public String[] GetContains(String Unigram, String Position, int maxResults)
	{
		return Filter(CONTAINS, Unigram, convertPos(Position), maxResults);
	}

	/**
	 * Returns all 'contains' matches for the specified <code>Unigram</code> and
	 * <code>pos</code>
	 * 
	 * @param Unigram
	 * @param Position
	 */
	public String[] GetContains(String Unigram, String Position)
	{
		return GetContains(Unigram, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * ending with the given <code>Unigram</code>.
	 * <p>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param Unigram
	 * @param Position
	 * @param maxResults
	 */
	public String[] GetEndsWith(String Unigram, String Position, int maxResults)
	{
		return Filter(ENDS_WITH, Unigram, convertPos(Position), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * ending with the given <code>Unigram</code>.
	 * 
	 * @param Unigram
	 * @param Position
	 */
	public String[] GetEndsWith(String Unigram, String Position)
	{
		return GetEndsWith(Unigram, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * starting with the given <code>Unigram</code>.
	 * 
	 * @param Unigram
	 * @param Position
	 * @param maxResults
	 */
	public String[] GetStartsWith(String Unigram, String Position, int maxResults)
	{
		return Filter(STARTS_WITH, Unigram, convertPos(Position), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * starting with the given <code>Unigram</code>.
	 * 
	 * @param Unigram
	 * @param Position
	 */
	public String[] GetStartsWith(String Unigram, String Position)
	{
		return GetStartsWith(Unigram, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * matching the the given regular expression <code>pattern</code>.
	 * <p>
	 * 
	 * @param pattern
	 * @param Position
	 * @param maxResults
	 * @see java.util.regex.Pattern
	 */
	public String[] GetRegexMatch(String pattern, String Position, int maxResults)
	{
		return Filter(REGEX_MATCH, pattern, convertPos(Position), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * 
	 * @param pattern
	 * @param Position
	 * @see java.util.regex.Pattern
	 */
	public String[] GetRegexMatch(String pattern, String Position)
	{
		return GetRegexMatch(pattern, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * that match the soundex code of the given <code>Unigram</code>.
	 * <p>
	 * 
	 * @param pattern
	 * @param Position
	 * @param maxResults
	 */
	public String[] GetSoundsLike(String pattern, String Position, int maxResults)
	{
		return Filter(SOUNDS_LIKE, pattern, convertPos(Position), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * that match the soundex code of the given <code>Unigram</code>.
	 * 
	 * @param pattern
	 * @param Position
	 */
	public String[] GetSoundsLike(String pattern, String Position)
	{
		return GetSoundsLike(pattern, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * matching a wildcard <code>pattern</code>,<br>
	 * with * '*' equals any number of characters, <br>
	 * and '?' equals any single character.
	 * 
	 * @param pattern
	 * @param Position
	 * @param maxResults
	 */
	public String[] GetWildcardMatch(String pattern, String Position, int maxResults)
	{
		return Filter(WILDCARD_MATCH, pattern, convertPos(Position), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * matching a wildcard <code>pattern</code>,<br>
	 * with '*' representing any number of characters, <br>
	 * and '?' equals any single character..
	 * 
	 * @param pattern
	 * @param Position
	 */
	public String[] GetWildcardMatch(String pattern, String Position)
	{
		return GetWildcardMatch(pattern, Position, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified
	 * <code>Position</code> matching the Filter specified with
	 * <code>filterFlag</code>
	 * 
	 * @param filterFlag
	 * @param Unigram
	 * @param pos
	 * @param maxResults
	 * @invisible
	 */
	public String[] Filter(int filterFlag, String Unigram, POS pos, int maxResults)
	{
		return GetStringVectorFromList(GetFilters().Filter(filterFlag, Unigram, pos, maxResults));
	}

	public String[] Filter(int filterFlag, String Unigram, POS pos)
	{
		return Filter(filterFlag, Unigram, pos, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified matching ANY of
	 * the m_WNFilters specified with <code>filterFlags</code>.
	 * 
	 * @param filterFlags
	 * @param Unigrams
	 * @param pos
	 * @param maxResults
	 * @invisible
	 */
	public String[] FilterByOR(int[] filterFlags, String[] Unigrams, POS pos, int maxResults)
	{
		return GetStringVectorFromList(GetFilters().FilterByOR(filterFlags, Unigrams, pos, maxResults));
	}

	private WordNetFilters GetFilters()
	{
		if (m_WNFilters == null)
			m_WNFilters = new WordNetFilters(this);
		return m_WNFilters;
	}

	public String[] FilterByOR(int[] filterFlag, String[] Unigram, POS pos)
	{
		return FilterByOR(filterFlag, Unigram, pos, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified matching ALL of
	 * the m_WNFilters specified with <code>filterFlags</code>.
	 * 
	 * @param filterFlags
	 * @param Unigrams
	 * @param pos
	 * @param maxResults
	 */
	private String[] FilterByAND(int[] filterFlags, String[] Unigrams, POS pos, int maxResults)
	{
		return GetStringVectorFromList(GetFilters().FilterByAND(filterFlags, Unigrams, pos, maxResults));
	}

	private String[] FilterByAND(int[] filterFlag, String[] Unigram, POS pos)
	{
		return FilterByAND(filterFlag, Unigram, pos, Integer.MAX_VALUE);
	}


	/**
	 * Called by the parent calling context upon shutdown
	 */
	public void dispose()
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
		Synset syns = GetSynsetAtId(id);

		if (syns == null || syns.getWordsSize() < 1)
			return null;
		List l = new LinkedList();
		AddLemmas(syns.getWords(), l);
		return l;
	}

	private Synset GetSynsetAtId(int id)
	{
		POS pos = null;
		String idStr = Integer.toString(id);
		int posDigit = Integer.parseInt(idStr.substring(0, 1));
		long offset = Long.parseLong(idStr.substring(1));
		switch (posDigit) {
		case 9:
			pos = POS.NOUN;
			break;
		case 8:
			pos = POS.VERB;
			break;
		case 7:
			pos = POS.ADJECTIVE;
			break;
		case 6:
			pos = POS.ADVERB;
			break;
		}
		try
		{
			return m_Dictionary.getSynsetAt(pos, offset);
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
	}

	/**
	 * Returns String[] of unique ids, one for each 'sense' of <code>Unigram</code>
	 * with <code>pos</code>, or null if none are found.
	 */
	public int[] GetSenseIds(String Unigram, String Position)
	{
		POS pos = convertPos(Position);
		IndexWord XWord = FindIndexWord(pos, Word);
		return GetSenseIds(XWord);
	}

	/**
	 * Returns String[] of unique ids, one for each sense of <code>Unigram</code>
	 * with <code>pos</code>, or null if none are found.
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
			throw new WordNetError(e);
		}
		return result;
	}

	private int toId(POS wnpos, long offset)
	{
		int posDigit = -1;
		if (wnpos == POS.NOUN)
			posDigit = 9;
		else if (wnpos == POS.VERB)
			posDigit = 8;
		else if (wnpos == POS.ADJECTIVE)
			posDigit = 7;
		else if (wnpos == POS.ADVERB)
			posDigit = 6;
		else
			throw new WordNetError("Invalid POS type: " + wnpos);
		return Integer.parseInt((Integer.toString(posDigit) + offset));
	}

	/**
	 * Returns full gloss for !st sense of 'Unigram' with 'pos' or null if not found
	 */
	public String GetGloss(String Unigram, String pos)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, pos, 1);
		return GetGloss(SynsetInstance);
	}

	/**
	 * Returns glosses for all senses of 'Unigram' with 'pos', or null if not found
	 */
	public String[] GetAllGlosses(String Unigram, String pos)
	{
		List glosses = new LinkedList();

		Synset[] SynsetInstances = allSynsets(Unigram, pos);
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
	 * Returns description for <code>Unigram</code> with <code>pos</code> or null if
	 * not found
	 */
	public String GetDescription(String Unigram, String pos)
	{
		String gloss = GetGloss(Unigram, pos);
		return WordNetUtil.parseDescription(gloss);
	}

	/**
	 * Returns all examples for 1st sense of <code>Unigram</code> with
	 * <code>pos</code>, or null if not found
	 */
	public String[] GetExamples(CharSequence Unigram, CharSequence pos)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Unigram, pos, 1);
		List l = GetExamples(SynsetInstance);
		return GetStringVectorFromList(l);
	}

	/**
	 * Return a random example from the set of examples from all senses of
	 * <code>Unigram</code> with <code>pos</code>, assuming they contain
	 * <code>Unigram</code>, or else null if not found
	 */
	public String GetAnyExample(CharSequence Unigram, CharSequence pos)
	{
		String[] all = GetAllExamples(Unigram, pos);
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
	 * Returns examples for all senses of <code>Unigram</code> with <code>pos</code>
	 * if they contain the <code>Unigram</code>, else null if not found
	 */
	public String[] GetAllExamples(CharSequence Unigram, CharSequence pos)
	{
		Synset[] syns = allSynsets(Unigram, pos);
		if (syns == null || syns.length < 1)
			return null;
		List l = new LinkedList();
		for (int i = 0; i < syns.length; i++)
		{
			if (syns[i] != null)
			{
				for (int j = 0; j < syns.length; j++)
				{
					List examples = GetExamples(syns[i]);
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
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
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

		result = GetSimilar(SenseId);
		this.AddSynsetsToSet(result, set);

		result = GetCoordinates(SenseId);
		this.AddSynsetsToSet(result, set);

		result = GetAlsoSees(SenseId);
		this.AddSynsetsToSet(result, set);

		return setToStrings(set, maxResults, true);
	}

	public String[] GetAllSynonyms(int id)
	{
		return GetAllSynonyms(id, Integer.MAX_VALUE);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>Unigram</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetSynonyms(String Unigram, String Position, int maxResults)
	{
		String[] result = null;
		Set set = new HashSet();

		result = GetSynset(Unigram, Position, false);
		this.AddSynsetsToSet(result, set);
		result = GetHypernyms(Unigram, Position);
		this.AddSynsetsToSet(result, set);

		result = GetSimilar(Unigram, Position);
		this.AddSynsetsToSet(result, set);

		result = GetAlsoSees(Unigram, Position);
		this.AddSynsetsToSet(result, set);

		result = GetCoordinates(Unigram, Position);
		this.AddSynsetsToSet(result, set);

		return setToStrings(set, maxResults, true);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>Unigram</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetSynonyms(String Unigram, String Position)
	{
		return GetSynonyms(Unigram, Position, Integer.MAX_VALUE);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>Unigram</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetAllSynonyms(String Unigram, String Position, int maxResults)
	{
		final boolean dbug = false;

		String[] result = null;
		Set set = new HashSet();

		result = GetAllSynsets(Unigram, Position);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Synsets: " + WordNetUtil.asList(result));

		result = GetAllHyponyms(Unigram, Position);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Hyponyms: " + WordNetUtil.asList(result));
		if (dbug)
			System.err.println("Set: " + WordNetUtil.asList(set));

		result = GetAllSimilar(Unigram, Position);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Similar: " + WordNetUtil.asList(result));

		result = GetAllAlsoSees(Unigram, Position);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("AlsoSees: " + WordNetUtil.asList(result));

		result = GetAllCoordinates(Unigram, Position);
		this.AddSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Coordinates: " + WordNetUtil.asList(result));

		return setToStrings(set, maxResults, true);
	}

	public String[] GetAllSynonyms(String Unigram, String Position)
	{
		return GetAllSynonyms(Unigram, Position, Integer.MAX_VALUE);
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
	 * pos' or null if not found
	 */
	public String[] GetCommonParents(String Unigram1, String word2, String pos)
	{
		List result = GetCommonParentList(Unigram1, word2, pos);
		return GetStringVectorFromList(result);
	}

	/**
	 * Returns common parent for Unigrams with unique ids <code>id1</code>,
	 * <code>id2</code>, or null if either Unigram or no parent is found
	 */
	public Synset GetCommonParent(int id1, int id2) throws JWNLException
	{
		Synset syn1 = GetSynsetAtId(id1);
		if (syn1 == null)
			return null;
		Synset syn2 = GetSynsetAtId(id2);
		if (syn2 == null)
			return null;
		RelationshipList list = RelationshipFinder.getInstance().findRelationships(syn1, syn2, PointerType.HYPERNYM);
		AsymmetricRelationship ar = (AsymmetricRelationship) list.get(0);
		PointerTargetNodeList nl = ar.getNodeList();
		PointerTargetNode ptn = (PointerTargetNode) nl.get(ar.getCommonParentIndex());
		return ptn.getSynset();
	}

	private List GetCommonParentList(String Unigram1, String word2, String pos)
	{
		Synset syn = null;
		try
		{
			POS wnpos = convertPos(pos);
			IndexWord XWord1 = FindIndexWord(wnpos, Word1);
			if (XWord1 == null)
				return null;
			IndexWord XWord2 = FindIndexWord(wnpos, Word2);
			if (XWord2 == null)
				return null;
			syn = GetCommonParent(XWord1, XWord2);
			if (syn == null)
				return null;
		}
		catch (JWNLException e)
		{
			throw new WordNetError(this, e);
		}
		List l = new ArrayList();
		AddLemmas(syn.getWords(), l);
		return l == null || l.size() < 1 ? null : l;
	}

	private Synset GetCommonParent(IndexWord XWord1, IndexWord XWord2) throws JWNLException
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
	 * with <code>pos</code>, or null if not found.
	 */
	public String[] GetSynset(String Unigram, String pos)
	{
		return GetSynset(Unigram, pos, false);
	}

	/**
	 * Returns String[] of Unigrams in synset for first sense of <code>word</code>
	 * with <code>pos</code>, or null if not found.
	 */
	public String[] GetSynset(String Unigram, String pos, boolean includeOriginal)
	{
		Synset syns = GetSynsetAtIndex(Unigram, pos, 1);
		if (syns == null || syns.getWordsSize() < 1)
			return null;
		List l = new LinkedList();
		AddLemmas(syns.getWords(), l);
		if (!includeOriginal)
			l.remove(Unigram);

		return GetStringVectorFromList(l);
	}

	/**
	 * Returns String[] of Synsets for unique id <code>id</code> or null if not
	 * found.
	 */
	public String[] GetSynset(int id)
	{
		return GetStringVectorFromList(GetSynsetList(id));
	}

	/**
	 * Returns String[] of Unigrams in each synset for all senses of
	 * <code>Unigram</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetAllSynsets(String Unigram, String Position)
	{
		POS pos = convertPos(Position);
		IndexWord XWord = null;
		List result = null;
		try
		{
			XWord = FindIndexWord(pos, Word);
			if (XWord == null || XWord.getSenseCount() < 1)
				return null;
			result = new LinkedList();
			for (int i = 1; i <= XWord.getSenseCount(); i++)
			{
				List syns = this.GetSynsetAtIndex(XWord, i);
				if (syns == null || syns.size() < 1)
					continue;
				for (Iterator j = syns.iterator(); j.hasNext();)
				{
					String lemma = (String) j.next();
					AddLemma(lemma, result);
				}
			}
			result.remove(Unigram); // not including original
			return GetStringVectorFromList(result);
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
	}

	private List GetSynsetAtIndex(IndexWord XWord, int index) throws JWNLException
	{
		if (index < 1)
			throw new IllegalArgumentException("Invalid index: " + index);

		if (XWord == null || XWord.getSenseCount() < 1)
			return null;

		List l = new ArrayList();
		AddLemmas(XWord.getSense(index).getWords(), l);
		return l;
	}

	private Synset[] allSynsets(CharSequence Unigram, CharSequence Position)
	{
		POS pos = convertPos(Position);
		IndexWord XWord = FindIndexWord(pos, Word);
		if (XWord == null)
			return null;
		int senseCount = XWord.getSenseCount();
		if (senseCount < 1)
			return null;
		Synset[] syns = new Synset[senseCount];
		for (int i = 0; i < syns.length; i++)
		{
			try
			{
				syns[i] = XWord.getSense(i + 1);
				if (syns[i] == null)
					System.err.println("[WARN] WordNet returned null Synset for: " + Word + "/" + pos);
			}
			catch (JWNLException e)
			{
				throw new WordNetError(e);
			}
		}
		return syns;
	}

	private Synset GetSynsetAtIndex(CharSequence Word, CharSequence Position, int i)
	{
		if (i < 1)
			throw new IllegalArgumentException("Invalid index: " + i);
		POS pos = convertPos(Position);
		IndexWord XWord = FindIndexWord(pos, Word);
		if (XWord == null || XWord.getSenseCount() < i)
			return null;
		try
		{
			return XWord.getSense(i);
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
	}

	/**
	 * Return the # of senses (polysemy) for a given Word/pos. A 'sense' refers to
	 * a specific WordNet meaning and maps 1-1 to the concept of synsets. Each
	 * 'sense' of a Word exists in a different synset.
	 * 
	 * @return # of senses or -1 if not found
	 */
	public int GetSenseCount(String Word, String pos)
	{
		int senses = -1;
		try
		{
			IndexWord XWord = FindIndexWord(pos, Word);
			if (XWord != null)
				senses = XWord.getSenseCount();
		}
		catch (WordNetError e)
		{
			System.err.println("[WARN] " + e.getMessage());
		}
		return senses;
	}

	/**
	 * Returns String[] of Antonyms for the 1st sense of <code>Word</code> with
	 * <code>pos</code> or null if not found<br>
	 */
	public String[] GetAntonyms(String Word, String pos)
	{
		return GetPointerTargetsAtIndex(Word, pos, PointerType.ANTONYM, 1);
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
	 * <code>pos</code> or null if not found<br>
	 */
	public String[] GetAllAntonyms(String Word, String pos)
	{
		return GetAllPointerTargets(Word, pos, PointerType.ANTONYM);
	}

	/**
	 * Returns Hypernym String[] for all senses of <code>Word</code> with
	 * <code>pos</code> or null if not found
	 */
	public String[] GetHypernyms(String Word, String Position)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Word, Position, 1);
		PointerTargetNodeList ptnl = null;
		try
		{
			ptnl = PointerUtils.getInstance().getDirectHypernyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
		return ptnlToStrings(Word, ptnl);
	}

	/**
	 * Returns Hypernym String[] for id, or null if not found
	 */
	public String[] GetHypernyms(int id)
	{
		Synset SynsetInstance = GetSynsetAtId(id);
		PointerTargetNodeList ptnl = null;
		try
		{
			ptnl = PointerUtils.getInstance().getDirectHypernyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
		return ptnlToStrings(null, ptnl);
	}


	/*
	 * Adds the hypernyms for this 'synset' to List 
	 */
	private void GetHypernyms(Synset syn, Collection l) throws JWNLException
	{

		PointerTargetNodeList ptnl = null;
		try
		{
			ptnl = PointerUtils.getInstance().getDirectHypernyms(syn);
		}
		catch (NullPointerException e)
		{
		}
		getLemmaSet(ptnl, l);
	}


	/**
	 * Returns an ordered String[] of hypernym-synsets (each a semi-colon
	 * delimited String) up to the root of WordNet for the 1st sense of the Word,
	 * or null if not found
	 */
	public String[] GetAllHypernyms(String Word, String Position)
	{
		try
		{
			IndexWord XWord = FindIndexWord(convertPos(Position), Word);
			return GetStringVectorFromList(this.GetAllHypernyms(XWord));
		}
		catch (JWNLException e)
		{
			throw new WordNetError(this, e);
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
		List l = GetHypernymTree(SynsetInstance);
		return GetStringVectorFromList(l);
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

		PointerTargetTree ptt = null;
		try
		{
			ptt = PointerUtils.getInstance().getHypernymTree(SynsetInstance);

		}
		catch (NullPointerException e)
		{
		}

		if (ptt == null)
			return null;

		List pointerTargetNodeLists = ptt.toList();
		int count = 0; 
		List l = new ArrayList();
		for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext(); count++)
		{
			PointerTargetNodeList ptnl = (PointerTargetNodeList) i.next();
			List strs = this.GetLemmaStrings(ptnl, SYNSET_DELIM, false);
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
	 * <code>pos</code> or null if not found
	 */
	public String[] GetHyponyms(String Word, String Position)
	{
		Synset SynsetInstance = GetSynsetAtIndex(Word, Position, 1);
		// System.out.println("syn="+(SynsetInstance.toString()));
		PointerTargetNodeList ptnl = null;
		try
		{
			PointerUtils pu = PointerUtils.getInstance();
			ptnl = pu.getDirectHyponyms(SynsetInstance);

			if (ptnl == null)
				throw new RuntimeException("JWNL ERR: " + Word + "/" + Position);
		}
		catch (NullPointerException e)
		{
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
		return ptnlToStrings(Word, ptnl);
	}

	/**
	 * Returns Hyponym String[] for id, or null if not found
	 */
	public String[] GetHyponyms(int id)
	{
		Synset SynsetInstance = GetSynsetAtId(id);
		PointerTargetNodeList ptnl = null;
		try
		{
			ptnl = PointerUtils.getInstance().getDirectHyponyms(SynsetInstance);
		}
		catch (NullPointerException e)
		{
			// ignore jwnl bug
		}
		catch (JWNLException e)
		{
			throw new WordNetError(e);
		}
		return ptnlToStrings(null, ptnl);
	}


	/* Adds the hyponyms for this 'synset' to List */
	private void GetHyponyms(Synset syn, Collection l) throws JWNLException
	{
		PointerTargetNodeList ptnl = null;
		try
		{
			PointerUtils pu = PointerUtils.getInstance();
			ptnl = pu.getDirectHyponyms(syn);
		}
		catch (NullPointerException e)
		{
		}
		GetLemmaSet(ptnl, l);
	}

	/**
	 * Returns an unordered String[] of hyponym-synsets (each a colon-delimited
	 * String), or null if not found
	 * 
	 */
	public String[] GetAllHyponyms(String Word, String Position)
	{
		IndexWord XWord = FindIndexWord(convertPos(Position), Word);
		List l = this.GetAllHyponyms(XWord);
		if (l == null)
			return null;
		l.remove(Word);
		return GetStringVectorFromList(l);
	}

	/*
	 * private List GetAllHyponyms(IndexWord XWord) { int[] ids = GetSenseIds(XWord);
	 * for (int i = 0; i < ids.length; i++) { GetHyponyms(ids[i]); } return null;
	 */
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
			throw new WordNetError(e);
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

		List l = null;
		try
		{
			l = GetHyponymTree(SynsetInstance);
		}
		catch (JWNLException e)
		{
			e.printStackTrace();
		}
		return GetStringVectorFromList(l);
	}

	private List GetHyponymTree(Synset SynsetInstance) throws JWNLException
	{
		if (SynsetInstance == null)
			return null;

		PointerTargetTree ptt = null;
		try
		{
			ptt = PointerUtils.getInstance().getHyponymTree(SynsetInstance);
		}
		catch (NullPointerException e)
		{
		}
		if (ptt == null)
			return null;

		List pointerTargetNodeLists = ptt.toList();

		List l = new ArrayList();
		for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext();)
		{
			PointerTargetNodeList ptnl = (PointerTargetNodeList) i.next();
			List tmp = this.GetLemmaStrings(ptnl, SYNSET_DELIM, true);

			for (Iterator it = tmp.iterator(); it.hasNext();)
			{
				String syn = (String) it.next();
				syn = trimFirstandLastChars(syn);
				if (syn.length() < 2)
					continue;
				if (!l.contains(syn))
					l.add(syn);
			}
		}

		// remove all the entries from the current SynsetInstance
		Set syns = new HashSet();
		AddLemmas(SynsetInstance.getWords(), syns);
		OUTER: for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			String syn = (SYNSET_DELIM + (String) iter.next() + SYNSET_DELIM);
			for (Iterator j = syns.iterator(); j.hasNext();)
			{
				String lemma = (SYNSET_DELIM + j.next() + SYNSET_DELIM);
				if (syn.indexOf(lemma) > -1)
				{
					iter.remove();
					continue OUTER;
				}
			}
		}

		return l;
	}


	public boolean isNoun(String Word)
	{
		return (GetPosStr(Word).indexOf(Character.toString('n')) > -1);
	}

	public boolean isAdjective(String Word)
	{
		return (GetPosStr(Word).indexOf(Character.toString('a')) > -1);
	}

	public boolean isVerb(String Word)
	{
		return (GetPosStr(Word).indexOf(Character.toString('v')) > -1);
	}

	public boolean isAdverb(String Word)
	{
		return (GetPosStr(Word).indexOf(Character.toString('r')) > -1);
	}

	/**
	 * Returns an array of all stems, or null if not found
	 * 
	 * @param query
	 * @param pos
	 */
	public String[] GetStems(String query, CharSequence pos)
	{
		List tmp = GetStemList(query, pos);
		return GetStringVectorFromList(tmp);
	}

	/**
	 * Returns true if 'Word' exists with 'pos' and is equal (via String.equals())
	 * to any of its stem forms, else false;
	 * 
	 * @param Word
	 * @param pos
	 */
	public boolean isStem(String Word, CharSequence pos)
	{
		String[] stems = GetStems(Word, pos);
		if (stems == null)
			return false;
		for (int i = 0; i < stems.length; i++)
			if (Word.equals(stems[i]))
				return true;
		return false;
	}

	private List GetStemList(String query, CharSequence pos)
	{
		try
		{
			return m_Dictionary.getMorphologicalProcessor().lookupAllBaseForms(convertPos(pos), query);
		}
		catch (JWNLException e)
		{
			throw new WordNetError(this, e);
		}
	}

}
