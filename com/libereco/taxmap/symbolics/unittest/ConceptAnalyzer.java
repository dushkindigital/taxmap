import java.io.FileInputStream;
import java.util.Iterator;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;

import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;

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

	private IndexWord DEVELOP;
	private IndexWord SHARK;
	private IndexWord DOLPHIN;
	private IndexWord EXCELLENT;
	private IndexWord FABULOUS;
	private String N_GRAM = "software development life-cycle";

	public ConceptAnalyzer() throws JWNLException 
	{
		DEVELOP = Dictionary.getInstance().getIndexWord(POS.VERB, "developing");
		SHARK = Dictionary.getInstance().getIndexWord(POS.NOUN, "shark");
		DOLPHIN = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "dolphin");
		EXCELLENT = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, "excellent");
		FABULOUS = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, "fabulous");
	}

	public void runTest() throws JWNLException 
	{
		AnalyzeMorphology(N_GRAM);
		GetHypernym(DEVELOP);
		GetHyponym(SHARK);
		DeriveAssociation(SHARK, DOLPHIN);
		DeriveCommonMeaning(EXCELLENT, FABULOUS);
	}

	private void AnalyzeMorphology(String phrase) throws JWNLException 
	{
		System.out.println("Lemma form for \"" + phrase + "\": " +
		                   Dictionary.getInstance().lookupIndexWord(POS.VERB, phrase));
	}

	private void GetHypernym(IndexWord word) throws JWNLException 
	{
		PointerTargetNodeList hypernyms = PointerUtils.getInstance().getDirectHypernyms(word.getSense(1));
		System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
		hypernyms.print();
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

