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
		// AnalyzeMorphology(N_GRAM);
		// GetHypernym(DEVELOP);
		// GetHyponym(SHARK);
		// DeriveAssociation(SHARK, DOLPHIN);
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


  public String[] getHypernyms(String word, String position)
  {
    Synset synset = getSynsetAtIndex(word, position, 1);
    PointerTargetNodeList ptnl = null;
    try
    {
      ptnl = PointerUtils.getInstance().getDirectHypernyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
      System.err.println("[WARN] JWNL Error: " + word + "/" + position);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    return ptnlToStrings(word, ptnl);
  }


  public String[] getHypernyms(int id)
  {
    Synset synset = getSynsetAtId(id);
    PointerTargetNodeList ptnl = null;
    try
    {
      ptnl = PointerUtils.getInstance().getDirectHypernyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    return ptnlToStrings(null, ptnl);
  }

  private void getHypernyms(Synset syn, Collection l) throws JWNLException
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
    getLemmaSet(ptnl, l);
  }

  private List getAllHypernyms(IndexWord idw) throws JWNLException
  {
    if (idw == null)
      return null;
    Synset[] synsets = idw.getSenses();
    if (synsets == null || synsets.length <= 0)
      return null;

    int i = 0;
    List result = new LinkedList();
    for (; i < synsets.length; i++)
      getHypernyms(synsets[i], result);
    return result == null || result.size() < 1 ? null : result;
  }


  public String[] getHypernymTree(int id) throws JWNLException
  {
    Synset synset = getSynsetAtId(id);
    if (synset == null)
      return new String[]{ROOT};
    List l = getHypernymTree(synset);
    return toStrArr(l);
  }


  private List getHypernymTree(Synset synset) throws JWNLException
  {
    if (synset == null)
      return null;

    PointerTargetTree ptt = null;
    try
    {
      ptt = PointerUtils.getInstance().getHypernymTree(synset);

    }
    catch (NullPointerException e)
    {
      // ignore exception; jwnl bug here
    }

    if (ptt == null)
      return null;

    List pointerTargetNodeLists = ptt.toList();
    int count = 0; 
    List l = new ArrayList();
    for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext(); count++)
    {
      PointerTargetNodeList ptnl = (PointerTargetNodeList) i.next();
      List strs = this.getLemmaStrings(ptnl, SYNSET_DELIM, false);
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

}

