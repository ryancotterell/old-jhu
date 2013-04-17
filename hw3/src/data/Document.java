package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Document implements Iterable<String>
{
	/**
	 * This is static because the vocabulary is shared among all documents.
	 */
	public static Map<String, Integer> vocabulary = new HashMap<String, Integer>();
	//
	private static int vocabularySize = 0;
	// All corpu
	//private static Set<Integer> corpora = new HashSet<Integer>();
	
	private int corpus;
	private List<String> words; // = new ArrayList<String>();
	
	public Document(String line)
	{
		words = new ArrayList<String>();
		String[] split = line.split(" ");
		this.corpus = Integer.parseInt(split[0]);
		
		for (int i = 1; i < split.length; i++)
		{
			this.words.add(split[i]);
			
			// add to entire vocabulary
			if (!Document.vocabulary.keySet().contains(split[i]))
				Document.vocabulary.put(split[i], vocabularySize++);
		}
	}
	
	public int size()
	{
		return this.words.size();
	}
	
	public int getCorpus()
	{
		return this.corpus;
	}
	
	public String getWord(int index)
	{
		return this.words.get(index);
	}
	
	public Iterator<String> iterator()
	{
		return this.words.iterator();
	}
}
