package data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentCollection 
{

	/** All documents in this set */
	private List<Document> documents;
	
	// TODO should this be static/shared across training/test? It won't matter in practice
	/** A set of all the corpus numbers in this DocumentSet.*/
	private Set<Integer> corpora;
	
	public DocumentCollection()
	{
		documents = new ArrayList<Document>();
		corpora = new HashSet<Integer>();
	}
	
	public void addDocument(Document d)
	{
		documents.add(d);
		corpora.add(d.getCorpus());
	}
	
	public int getNumberOfCorpora()
	{
		return corpora.size();
	}
	
}
