package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DocumentCollection// implements Iterable<Document>
{

	/** All documents in this set */
	private ArrayList<Document> documents;
	
	// TODO should this be static/shared across training/test? It won't matter in practice
	/** A set of all the corpus numbers in this DocumentSet.*/
	private Set<Integer> corpora;
	
	private int numTokens;
	private boolean calculatedNumTokens;
	
	public DocumentCollection()
	{
		documents = new ArrayList<Document>();
		corpora = new HashSet<Integer>();
		calculatedNumTokens = false;
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
	
	public int getNumberOfDocuments()
	{
		return documents.size();
	}
	
	/**
	 * Returns an unmodifiable iterator over the list of documents in the collection
	 */
	public Iterator<Document> iterator()
	{
		List<Document> unmodifiableList = Collections.unmodifiableList(documents);
		return unmodifiableList.iterator();
	}
	
	public Document getDocument(int index)
	{
		return documents.get(index);
	}
	
	public int size()
	{
		return documents.size();
	}
	
	public int getNumberOfTokens() {
		if (calculatedNumTokens) {
			return numTokens;
		}
		numTokens = 0;
		for (Document d: documents) {
			numTokens += d.size();
		}
		calculatedNumTokens = true;
		return numTokens;
	}
	
	
	
}
