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
 * Provides library support for application access to Wordnet.
 * @author Chiranjit Acharya
 */
public class WordNetCore implements Wordnet
{
	private static final String VERSION = "029";

	/** String constant for Noun part-of-speech */
	public final static String NOUN = "n";

	/** String constant for Verb part-of-speech */
	public final static String VERB = "v";

	/** String constant for Adjective part-of-speech */
	public final static String ADJ = "a";

	/** String constant for Adverb part-of-speech */
	public final static String ADV = "r";

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

	protected WordnetFilters m_WNFilters;
	protected Dictionary m_Dictionary;
	protected int maxCharsPerWord = 10;
	protected boolean ignoreCompoundWords = true;
	protected boolean ignoreUpperCaseWords = true;

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
	 * Constructs an instance of <code>WordNetCore</code> using the Wordnet
	 * installation whose location is specified at <code>wordnetInstallDir</code>.
	 * 
	 * @param wordnetInstallDir
	 *          home directory for a pre-installed Wordnet installation.
	 */
	public WordNetCore(String wordnetInstallDir)
	{
		this(wordnetInstallDir, GetDefaultConfFile());
	}

	private static String GetDefaultConfFile()
	{
		// set the locale since the default conf is only English
		Locale.setDefault(Locale.ENGLISH);
		return DEFAULT_CONF;
	}

	private WordNetCore(String wordnetHome, String ConfigFile)
	{
		this.SetWordnetHome(wordnetHome);

		if (DBUG)
			System.err.println("WordNetCore.WordNetCore(" + m_WordNetHome + "," + ConfigFile + ")");

		if (!JWNL.isInitialized())
		{
			try
			{
				InitWordnet(ConfigFile);
			}
			catch (Exception e)
			{
				throw new WordnetError("Unable to load Wordnet " + "with $WORDNET_HOME=" + m_WordNetHome + " & CONF_FILE=" + ConfigFile, e);
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
	 * Returns an iterator over all words of the specified 'pos'
	 */
	public Iterator iterator(String pos)
	{
		return GetFilters().lemmaIterator(m_Dictionary, convertPos(pos));
	}


	/**
	 * Returns up to <code>maxResults</code> full anagram matches for the
	 * specified <code>word</code> and <code>pos</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param word
	 * @param posStr
	 * @param maxResults
	 */
	public String[] GetAnagrams(String word, String posStr, int maxResults)
	{
		return Filter(ANAGRAMS, word, convertPos(posStr), maxResults);
	}

	/**
	 * Returns all full anagram matches for the specified <code>word</code> and
	 * <code>pos</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param word
	 * @param posStr
	 */
	public String[] GetAnagrams(String word, String posStr)
	{
		return GetAnagrams(word, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * where each contains the given <code>word</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param word
	 * @param posStr
	 * @param maxResults
	 */
	public String[] GetContains(String word, String posStr, int maxResults)
	{
		return Filter(CONTAINS, word, convertPos(posStr), maxResults);
	}

	/**
	 * Returns all 'contains' matches for the specified <code>word</code> and
	 * <code>pos</code>
	 * <p>
	 * Example: 'table' returns 'bleat' (but not 'tale').
	 * 
	 * @param word
	 * @param posStr
	 */
	public String[] GetContains(String word, String posStr)
	{
		return GetContains(word, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * ending with the given <code>word</code>.
	 * <p>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param word
	 * @param posStr
	 * @param maxResults
	 */
	public String[] GetEndsWith(String word, String posStr, int maxResults)
	{
		return Filter(ENDS_WITH, word, convertPos(posStr), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * ending with the given <code>word</code>.
	 * <p>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param word
	 * @param posStr
	 */
	public String[] GetEndsWith(String word, String posStr)
	{
		return GetEndsWith(word, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * starting with the given <code>word</code>.
	 * <p>
	 * Example: 'turn' returns 'turntable'
	 * 
	 * @param word
	 * @param posStr
	 * @param maxResults
	 */
	public String[] GetStartsWith(String word, String posStr, int maxResults)
	{
		return Filter(STARTS_WITH, word, convertPos(posStr), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * starting with the given <code>word</code>.
	 * <p>
	 * Example: 'turn' returns 'turntable'
	 * 
	 * @param word
	 * @param posStr
	 */
	public String[] GetStartsWith(String word, String posStr)
	{
		return GetStartsWith(word, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * matching the the given regular expression <code>pattern</code>.
	 * <p>
	 * 
	 * @param pattern
	 * @param posStr
	 * @param maxResults
	 * @see java.util.regex.Pattern
	 */
	public String[] GetRegexMatch(String pattern, String posStr, int maxResults)
	{
		return Filter(REGEX_MATCH, pattern, convertPos(posStr), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * Example: 'table' returns 'turntable' & 'uncomfortable'
	 * 
	 * @param pattern
	 * @param posStr
	 * @see java.util.regex.Pattern
	 */
	public String[] GetRegexMatch(String pattern, String posStr)
	{
		return GetRegexMatch(pattern, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * that match the soundex code of the given <code>word</code>.
	 * <p>
	 * 
	 * @param pattern
	 * @param posStr
	 * @param maxResults
	 */
	public String[] GetSoundsLike(String pattern, String posStr, int maxResults)
	{
		return Filter(SOUNDS_LIKE, pattern, convertPos(posStr), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * that match the soundex code of the given <code>word</code>.
	 * 
	 * @param pattern
	 * @param posStr
	 */
	public String[] GetSoundsLike(String pattern, String posStr)
	{
		return GetSoundsLike(pattern, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * matching a wildcard <code>pattern</code>,<br>
	 * with * '*' equals any number of characters, <br>
	 * and '?' equals any single character.
	 * <p>
	 * Example: 't?le' returns (tale,tile,tole)<br>
	 * Example: 't*le' returns (tatumble, turtle, tussle, etc.)<br>
	 * Example: 't?le*' returns (telex, tile,tilefish,tile,talent, tiles, etc.)
	 * <br>
	 * 
	 * @param pattern
	 * @param posStr
	 * @param maxResults
	 */
	public String[] GetWildcardMatch(String pattern, String posStr, int maxResults)
	{
		return Filter(WILDCARD_MATCH, pattern, convertPos(posStr), maxResults);
	}

	/**
	 * Returns up to <code>maxResults</code> of the specified <code>pos</code>
	 * matching a wildcard <code>pattern</code>,<br>
	 * with '*' representing any number of characters, <br>
	 * and '?' equals any single character..
	 * <p>
	 * Example: 't?le' returns (tale,tile,tole)<br>
	 * Example: 't*le' returns (tatumble, turtle, tussle, etc.)<br>
	 * Example: 't?le*' returns (telex, tile,tilefish,tile,talent, tiles, etc.)
	 * <br>
	 * 
	 * @param pattern
	 * @param posStr
	 */
	public String[] GetWildcardMatch(String pattern, String posStr)
	{
		return GetWildcardMatch(pattern, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified
	 * <code>posStr</code> matching the Filter specified with
	 * <code>filterFlag</code>
	 * <p>
	 * Filter types include:
	 * 
	 * <pre>
	 * WordNetCore.EXACT_MATCH
	 *         WordNetCore.ENDS_WITH
	 *         WordNetCore.STARTS_WITH
	 *         WordNetCore.ANAGRAMS 
	 *         WordNetCore.CONTAINS_ALL
	 *         WordNetCore.CONTAINS_SOME  
	 *         WordNetCore.CONTAINS
	 *         WordNetCore.SIMILAR_TO
	 *         WordNetCore.SOUNDS_LIKE
	 *         WordNetCore.WILDCARD_MATCH
	 *         WordNetCore.REGEX_MATCH
	 * </pre>
	 * 
	 * @param filterFlag
	 * @param word
	 * @param pos
	 * @param maxResults
	 * @invisible
	 */
	public String[] Filter(int filterFlag, String word, POS pos, int maxResults)
	{
		return toStrArr(GetFilters().Filter(filterFlag, word, pos, maxResults));
	}

	/**
	 * @invisible Return all instances of specified <code>posStr</code> matching
	 *            the Filter specified with <code>filterFlag</code>.
	 *            <p>
	 *            Filter types include:
	 * 
	 *            <pre>
	 * WordNetCore.EXACT_MATCH
	 *         WordNetCore.ENDS_WITH
	 *         WordNetCore.STARTS_WITH
	 *         WordNetCore.ANAGRAMS 
	 *         WordNetCore.CONTAINS_ALL
	 *         WordNetCore.CONTAINS_SOME  
	 *         WordNetCore.CONTAINS
	 *         WordNetCore.SIMILAR_TO
	 *         WordNetCore.SOUNDS_LIKE
	 *         WordNetCore.WILDCARD_MATCH
	 *         WordNetCore.REGEX_MATCH
	 * </pre>
	 * @example SimpleFilterExample.pde
	 * @param word
	 * @param pos
	 * @param filterFlag
	 */
	public String[] Filter(int filterFlag, String word, POS pos)
	{
		return Filter(filterFlag, word, pos, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified matching ANY of
	 * the m_WNFilters specified with <code>filterFlags</code>.
	 * <p>
	 * Filter types include:
	 * 
	 * <pre>
	 * WordNetCore.EXACT_MATCH
	 *         WordNetCore.ENDS_WITH
	 *         WordNetCore.STARTS_WITH
	 *         WordNetCore.ANAGRAMS 
	 *         WordNetCore.CONTAINS_ALL
	 *         WordNetCore.CONTAINS_SOME  
	 *         WordNetCore.CONTAINS
	 *         WordNetCore.SIMILAR_TO
	 *         WordNetCore.SOUNDS_LIKE
	 *         WordNetCore.WILDCARD_MATCH
	 *         WordNetCore.REGEX_MATCH
	 * </pre>
	 * 
	 * @param filterFlags
	 * @param words
	 * @param pos
	 * @param maxResults
	 * @invisible
	 */
	public String[] FilterByOR(int[] filterFlags, String[] words, POS pos, int maxResults)
	{
		return toStrArr(GetFilters().FilterByOR(filterFlags, words, pos, maxResults));
	}

	private WordnetFilters GetFilters()
	{
		if (m_WNFilters == null)
			m_WNFilters = new WordnetFilters(this);
		return m_WNFilters;
	}

	/**
	 * @invisible Return all instances of specified <code>posStr</code> matching
	 *            ANY of the m_WNFilters specified with <code>filterFlags</code>.
	 *            <p>
	 *            Filter types include:
	 * 
	 *            <pre>
	 * WordNetCore.EXACT_MATCH
	 *         WordNetCore.ENDS_WITH
	 *         WordNetCore.STARTS_WITH
	 *         WordNetCore.ANAGRAMS 
	 *         WordNetCore.CONTAINS_ALL
	 *         WordNetCore.CONTAINS_SOME  
	 *         WordNetCore.CONTAINS
	 *         WordNetCore.SIMILAR_TO
	 *         WordNetCore.SOUNDS_LIKE
	 *         WordNetCore.WILDCARD_MATCH
	 *         WordNetCore.REGEX_MATCH
	 * </pre>
	 * @example SimpleFilterExample.pde
	 * @param word
	 * @param pos
	 * @param filterFlag
	 */
	public String[] FilterByOR(int[] filterFlag, String[] word, POS pos)
	{
		return FilterByOR(filterFlag, word, pos, Integer.MAX_VALUE);
	}

	/**
	 * Return up to <code>maxResults</code> instances of specified matching ALL of
	 * the m_WNFilters specified with <code>filterFlags</code>.
	 * <p>
	 * Filter types include:
	 * 
	 * <pre>
	 * WordNetCore.EXACT_MATCH
	 *         WordNetCore.ENDS_WITH
	 *         WordNetCore.STARTS_WITH
	 *         WordNetCore.ANAGRAMS 
	 *         WordNetCore.CONTAINS_ALL
	 *         WordNetCore.CONTAINS_SOME  
	 *         WordNetCore.CONTAINS
	 *         WordNetCore.SIMILAR_TO
	 *         WordNetCore.SOUNDS_LIKE
	 *         WordNetCore.WILDCARD_MATCH
	 *         WordNetCore.REGEX_MATCH
	 * </pre>
	 * 
	 * @param filterFlags
	 * @param words
	 * @param pos
	 * @param maxResults
	 * @invisible
	 */
	private String[] FilterByAND(int[] filterFlags, String[] words, POS pos, int maxResults)
	{
		return toStrArr(GetFilters().FilterByAND(filterFlags, words, pos, maxResults));
	}

	/**
	 * @invisible Return all instances of specified <code>posStr</code> matching
	 *            ALL of the m_WNFilters specified with <code>filterFlags</code>.
	 *            <p>
	 *            Filter types include:
	 * 
	 *            <pre>
	 *         WordNetCore.EXACT_MATCH
	 *         WordNetCore.ENDS_WITH
	 *         WordNetCore.STARTS_WITH
	 *         WordNetCore.ANAGRAMS 
	 *         WordNetCore.CONTAINS_ALL
	 *         WordNetCore.CONTAINS_SOME  
	 *         WordNetCore.CONTAINS
	 *         WordNetCore.SIMILAR_TO
	 *         WordNetCore.SOUNDS_LIKE
	 *         WordNetCore.WILDCARD_MATCH
	 *         WordNetCore.REGEX_MATCH
	 * </pre>
	 * @example SimpleFilterExample.pde
	 * @param word
	 * @param pos
	 * @param filterFlag
	 */
	private String[] FilterByAND(int[] filterFlag, String[] word, POS pos)
	{
		return FilterByAND(filterFlag, word, pos, Integer.MAX_VALUE);
	}

	// ---------------- end Filter methods -------------------

	/**
	 * Called by the parent calling context upon shutdown
	 * 
	 * @invisible
	 */
	public void dispose()
	{
		// System.err.println("[INFO] Wordnet.dispose()...");
		if (m_Dictionary != null)
			m_Dictionary.close();
		m_Dictionary = null;
	}

	/**
	 * @invisible
	 */
	public void SetWordnetHome(String wordnetHome)
	{
		if (wordnetHome != null)
		{
			if (!(wordnetHome.endsWith("/") || wordnetHome.endsWith("\\")))
				wordnetHome += SLASH;
		}
		WordNetCore.m_WordNetHome = wordnetHome;
		if (m_WordNetHome != null)
			System.out.println("[INFO] Wordnet.home=" + m_WordNetHome);
	}

	// -------------------------- MAIN METHODS ----------------------------
	private List GetSynsetList(int id)
	{
		Synset syns = GetSynsetAtId(id);

		// System.out.println("GetSynsetList("id+") -> "+syns);

		if (syns == null || syns.getWordsSize() < 1)
			return null;
		List l = new LinkedList();
		addLemmas(syns.getWords(), l);
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
			throw new WordnetError(e);
		}
	}

	/**
	 * Returns String[] of unique ids, one for each 'sense' of <code>word</code>
	 * with <code>pos</code>, or null if none are found.
	 * <p>
	 * A Wordnet 'sense' refers to a specific Wordnet meaning and maps 1-1 to the
	 * concept of synsets. Each 'sense' of a word exists in a different synset.
	 * <p>
	 * For more info, see: {@link http://wordnet.princeton.edu/gloss}
	 */
	public int[] GetSenseIds(String word, String posStr)
	{
		POS pos = convertPos(posStr);
		// System.out.println("GetSenseIds()="+posStr+" -> "+pos);
		IndexWord idw = lookupIndexWord(pos, word);
		return GetSenseIds(idw);
	}

	/**
	 * Returns String[] of unique ids, one for each sense of <code>word</code>
	 * with <code>pos</code>, or null if none are found.
	 */
	public int[] GetSenseIds(IndexWord idw)
	{
		int[] result = null;
		try
		{
			int numSenses = idw.getSenseCount();
			if (idw == null || numSenses == 0)
				return null;
			long[] offsets = idw.getSynsetOffsets();
			result = new int[offsets.length];
			for (int i = 0; i < result.length; i++)
				result[i] = toId(idw.getPOS(), offsets[i]);
		}
		catch (Exception e)
		{
			throw new WordnetError(e);
		}
		// System.err.println("ids: "+Util.asList(result));
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
			throw new WordnetError("Invalid POS type: " + wnpos);
		return Integer.parseInt((Integer.toString(posDigit) + offset));
	}

	/**
	 * Returns full gloss for !st sense of 'word' with 'pos' or null if not found
	 */
	public String GetGloss(String word, String pos)
	{
		Synset synset = GetSynsetAtIndex(word, pos, 1);
		return GetGloss(synset);
	}

	/**
	 * Returns glosses for all senses of 'word' with 'pos', or null if not found
	 */
	public String[] GetAllGlosses(String word, String pos)
	{
		List glosses = new LinkedList();

		Synset[] synsets = allSynsets(word, pos);
		for (int i = 0; i < synsets.length; i++)
		{
			String gloss = GetGloss(synsets[i]);
			if (gloss != null)
				glosses.add(gloss);
		}
		return toStrArr(glosses);
	}

	/**
	 * Returns full gloss for word with unique <code>senseId</code>, or null if
	 * not found
	 */
	public String GetGloss(int senseId)
	{
		Synset synset = GetSynsetAtId(senseId);
		if (synset == null)
			return null;
		return GetGloss(synset);
	}

	/**
	 * Returns description for word with unique <code>senseId</code>, or null if
	 * not found
	 */
	public String GetDescription(int senseId)
	{
		String gloss = GetGloss(senseId);
		return WordNetUtil.parseDescription(gloss);
	}

	private String GetGloss(Synset synset)
	{
		if (synset == null)
			return null;
		return synset.getGloss();
	}

	/**
	 * Returns description for <code>word</code> with <code>pos</code> or null if
	 * not found
	 */
	public String GetDescription(String word, String pos)
	{
		String gloss = GetGloss(word, pos);
		return WordNetUtil.parseDescription(gloss);
	}

	/**
	 * Returns all examples for 1st sense of <code>word</code> with
	 * <code>pos</code>, or null if not found
	 */
	public String[] GetExamples(CharSequence word, CharSequence pos)
	{
		Synset synset = GetSynsetAtIndex(word, pos, 1);
		List l = GetExamples(synset);
		return toStrArr(l);
	}

	/**
	 * Return a random example from the set of examples from all senses of
	 * <code>word</code> with <code>pos</code>, assuming they contain
	 * <code>word</code>, or else null if not found
	 */
	public String GetAnyExample(CharSequence word, CharSequence pos)
	{
		String[] all = GetAllExamples(word, pos);
		int rand = (int) (Math.random() * all.length);
		return all[rand];
	}

	/**
	 * Returns examples for word with unique <code>senseId</code>, or null if not
	 * found
	 */
	public String[] GetExamples(int senseId)
	{
		Synset synset = GetSynsetAtId(senseId);
		if (synset == null)
			return null;
		return toStrArr(GetExamples(synset));
	}

	/**
	 * Returns examples for all senses of <code>word</code> with <code>pos</code>
	 * if they contain the <code>word</code>, else null if not found
	 */
	public String[] GetAllExamples(CharSequence word, CharSequence pos)
	{
		Synset[] syns = allSynsets(word, pos);
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
						// does it contain the word
						if (example.indexOf(word.toString()) < 0)
						  continue;
						if (!l.contains(example))
						  l.add(example);
					}
				}
			}
		}
		l.remove(word);
		return toStrArr(l);
	}

	private List GetExamples(Synset synset)
	{
		String gloss = GetGloss(synset);
		return WordNetUtil.parseExamples(gloss);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order), or null if not
	 * found.
	 */
	public String[] GetAllSynonyms(int senseId, int maxResults)
	{
		String[] result = null;
		Set set = new HashSet();

		result = GetSynset(senseId);
		this.addSynsetsToSet(result, set);
		// System.err.println("Synsets: "+WordNetUtil.asList(result));

		result = GetHyponymTree(senseId);
		this.addSynsetsToSet(result, set);
		// System.err.println("Hypornyms: "+WordNetUtil.asList(result));

		/*
		 * result = GetHypernyms(senseId); this.addSynsetsToSet(result, set);
		 */
		// System.err.println("Hypernyms: "+WordNetUtil.asList(result));

		result = GetSimilar(senseId);
		this.addSynsetsToSet(result, set);
		// System.err.println("Similar: "+WordNetUtil.asList(result));

		result = GetCoordinates(senseId);
		this.addSynsetsToSet(result, set);
		// System.err.println("Coordinates: "+WordNetUtil.asList(result));

		result = GetAlsoSees(senseId);
		this.addSynsetsToSet(result, set);
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
	 * <code>word</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetSynonyms(String word, String posStr, int maxResults)
	{
		String[] result = null;
		Set set = new HashSet();

		result = GetSynset(word, posStr, false);
		this.addSynsetsToSet(result, set);
		// System.err.println("Synsets: "+WordNetUtil.asList(result));
		/*
		 * result = GetHyponyms(word, posStr); this.addSynsetsToSet(result, set);
		 * //System.err.println("Hyponyms: "+WordNetUtil.asList(result));
		 */
		result = GetHypernyms(word, posStr);
		this.addSynsetsToSet(result, set);
		// System.err.println("Hypernyms: "+WordNetUtil.asList(result));

		result = GetSimilar(word, posStr);
		this.addSynsetsToSet(result, set);
		// System.err.println("Similar: "+WordNetUtil.asList(result));

		result = GetAlsoSees(word, posStr);
		this.addSynsetsToSet(result, set);
		// System.err.println("AlsoSees: "+WordNetUtil.asList(result));

		result = GetCoordinates(word, posStr);
		this.addSynsetsToSet(result, set);
		// System.err.println("Coordinates: "+WordNetUtil.asList(result));

		// System.err.println("=======================================");

		return setToStrings(set, maxResults, true);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>word</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetSynonyms(String word, String posStr)
	{
		return GetSynonyms(word, posStr, Integer.MAX_VALUE);
	}

	/**
	 * Returns an unordered String[] containing the synset, hyponyms, similars,
	 * alsoSees, and coordinate terms (checking each in order) for all senses of
	 * <code>word</code> with <code>pos</code>, or null if not found
	 */
	public String[] GetAllSynonyms(String word, String posStr, int maxResults)
	{
		final boolean dbug = false;

		String[] result = null;
		Set set = new HashSet();

		result = GetAllSynsets(word, posStr);
		this.addSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Synsets: " + WordNetUtil.asList(result));

		result = GetAllHyponyms(word, posStr);
		this.addSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Hyponyms: " + WordNetUtil.asList(result));
		if (dbug)
			System.err.println("Set: " + WordNetUtil.asList(set));

		/*
		 * result = GetAllHypernyms(word, posStr); this.addSynsetsToSet(result,
		 * set); if
		 * (dbug)System.err.println("Hypernyms: "+WordNetUtil.asList(result));
		 */

		result = GetAllSimilar(word, posStr);
		this.addSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Similar: " + WordNetUtil.asList(result));

		result = GetAllAlsoSees(word, posStr);
		this.addSynsetsToSet(result, set);
		if (dbug)
			System.err.println("AlsoSees: " + WordNetUtil.asList(result));

		result = GetAllCoordinates(word, posStr);
		this.addSynsetsToSet(result, set);
		if (dbug)
			System.err.println("Coordinates: " + WordNetUtil.asList(result));

		// System.err.println("=======================================");
		return setToStrings(set, maxResults, true);
	}

	public String[] GetAllSynonyms(String word, String posStr)
	{
		return GetAllSynonyms(word, posStr, Integer.MAX_VALUE);
	}
}

