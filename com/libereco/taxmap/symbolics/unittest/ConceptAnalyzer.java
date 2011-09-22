import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Collection;

import java.lang.String;
import java.lang.StringBuffer;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;

import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;

import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;

import net.didion.jwnl.dictionary.Dictionary;

public class ConceptAnalyzer 
{
	private static final String ConceptAnalyzerUsage = "java ConceptAnalyzer <wordnet properties file>";

	public static void main(String[] args) 
	{
		if (args.length != 1) 
		{
			System.out.println(ConceptAnalyzerUsage);
			System.exit(-1);
		}

		String WordNetPropertyFile = args[0];
		try 
		{
			// initialize the JWNL platform
			JWNL.initialize(new FileInputStream(WordNetPropertyFile));
			new ConceptAnalyzer().runTest();
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private IndexWord CAR;
	private IndexWord SHARK;
	private IndexWord DOLPHIN;
	private IndexWord EXCELLENT;
	private IndexWord FABULOUS;
	private String N_GRAM = "software development life-cycle";

	public ConceptAnalyzer() throws JWNLException 
	{
		CAR = Dictionary.getInstance().getIndexWord(POS.NOUN, "car");
		SHARK = Dictionary.getInstance().getIndexWord(POS.NOUN, "shark");
		DOLPHIN = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "dolphin");
		EXCELLENT = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, "excellent");
		FABULOUS = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, "fabulous");
	}

	public void runTest() throws JWNLException 
	{
		System.out.println("AnalyzeMorphology ...");
		System.out.println("");
		this.AnalyzeMorphology(N_GRAM);
		System.out.println("=====================================================================");
		System.out.println("");
		System.out.println("");
		System.out.println("DeriveAssociation ...");
		System.out.println("");
		this.DeriveAssociation(SHARK, DOLPHIN);
		System.out.println("=====================================================================");
		System.out.println("");
		System.out.println("");
		System.out.println("GetHyponym ...");
		System.out.println("");
		this.GetHyponym(SHARK);
		System.out.println("=====================================================================");
		System.out.println("");
		System.out.println("");
		System.out.println("GetHypernym ...");
		System.out.println("");
		this.GetHypernym(CAR);
		System.out.println("=====================================================================");
		System.out.println("");
		// DeriveCommonMeaning(EXCELLENT, FABULOUS);
	}

	private void AnalyzeMorphology(String phrase) throws JWNLException 
	{
		System.out.println("Lemma form for \"" + phrase + "\": " +
		                   Dictionary.getInstance().lookupIndexWord(POS.VERB, phrase));
	}

	private void GetHypernym(IndexWord word) throws JWNLException 
	{
		// PointerTargetNodeList hypernyms = PointerUtils.getInstance().getDirectHypernyms(word.getSense(1));
		// System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
		// hypernyms.print();
		if (word == null)
			return;
		Synset[] synsets = word.getSenses();
		if (synsets == null || synsets.length <= 0)
			return;

		int i = 0;
		List result = new LinkedList();
		for (; i < synsets.length; i++)
		{
			GetHypernymFromSynset(synsets[i], result);
		}

		ListIterator itr = result.listIterator();
		System.out.println("Iterating through elements of Java LinkedList using ListIterator in forward direction...");
		while(itr.hasNext())
		{
			System.out.println(itr.next());
		}
		return; // result == null || result.size() < 1 ? null : result;
	}

	private void GetHypernymFromSynset(Synset syn, Collection l) throws JWNLException
	{
		PointerTargetNodeList ptnl = null;
		try
		{
			ptnl = PointerUtils.getInstance().getDirectHypernyms(syn);
		}
		catch (NullPointerException e)
		{
			// bug from jwnl, ignore
		}
		GetLemmaSet(ptnl, l);
	}

	private void GetLemmaSet(PointerTargetNodeList source, Collection dest)
	{
		if (source == null)
			return;

		for (Iterator i = source.iterator(); i.hasNext();)
		{
			PointerTargetNode targetNode = (PointerTargetNode) i.next();
			if (!targetNode.isLexical())
			{
				Synset syn = targetNode.getSynset();
				if (syn != null)
					AddLemmas(syn.getWords(), dest);
			}
			else
			{
				AddLemma(targetNode.getWord(), dest);
			}
		}
		return;
	}

	private void AddLemmas(Word[] words, Collection dest)
	{
		if (words == null || words.length == 0)
			return;
		for (int k = 0; k < words.length; k++)
			AddLemma(words[k], dest);
	}

	private void AddLemma(Word word, Collection dest)
	{
		this.AddLemma(word.getLemma(), dest);
	}

	private void AddLemma(String lemma, Collection dest)
	{
		// if (ignoreCompoundWords && isCompound(lemma))
		// 	return;
		// if (ignoreUpperCaseWords && WordnetUtil.startsWithUppercase(lemma))
		// 	return;
		if (lemma.endsWith(")"))
			lemma = lemma.substring(0, lemma.length() - 3);
		lemma = Replace(lemma, '_', '-');

		if (!dest.contains(lemma)) // no duplicate lemma
			dest.add(lemma);
	}

	static String Replace(String src, char c, char r)
	{
		String QQ="";
		if (src.indexOf(c) < 0) return src;
			StringBuffer buffer = new StringBuffer(src);

		for (int i = 0; i < buffer.length(); i++)
			if (buffer.charAt(i) == c)
				buffer.replace(i, i + 1, (r + QQ));
		return buffer.toString();
	}

	private void GetHyponym(IndexWord word) throws JWNLException 
	{
		PointerTargetTree hyponyms = PointerUtils.getInstance().getHyponymTree(word.getSense(1));
		System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
		hyponyms.print();
	}

	private void DeriveAssociation(IndexWord word1, IndexWord word2) throws JWNLException 
	{
		RelationshipList list = RelationshipFinder.getInstance().findRelationships(word1.getSense(1), word2.getSense(1), PointerType.HYPERNYM);
		System.out.println("Hypernym relationship between \"" + word1.getLemma() + "\" and \"" + word2.getLemma() + "\":");
		for (Iterator itr = list.iterator(); itr.hasNext();) 
		{
			((Relationship) itr.next()).getNodeList().print();
		}
		System.out.println("Common Parent Index: " + ((AsymmetricRelationship) list.get(0)).getCommonParentIndex());
		System.out.println("Depth: " + ((Relationship) list.get(0)).getDepth());
	}

	private void DeriveCommonMeaning(IndexWord word1, IndexWord word2) throws JWNLException 
	{
		RelationshipList list = RelationshipFinder.getInstance().findRelationships(word1.getSense(1), word2.getSense(1), PointerType.SIMILAR_TO);
		System.out.println("Synonym relationship between \"" + word1.getLemma() + "\" and \"" + word2.getLemma() + "\":");
		for (Iterator itr = list.iterator(); itr.hasNext();) 
		{
			((Relationship) itr.next()).getNodeList().print();
		}
		System.out.println("Depth: " + ((Relationship) list.get(0)).getDepth());
	}

}

