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
	public static Map<String, Integer> vocabulary = new HashMap<String, Integer>();
	private static int wordCounter = 0;
	private static Set<Integer> corpora = new HashSet<Integer>();
	
	private int corpus;
	private List<String> words = new ArrayList<String>();
	
	public Document(String line)
	{
		String[] split = line.split(" ");
		this.corpus = Integer.parseInt(split[0]);
		
		if (!Document.corpora.contains(this.corpus))
			Document.corpora.add(this.corpus);
		
		for (int i = 1; i < split.length; i++)
		{
			this.words.add(split[i]);
			
			// add to entire vocabulary
			if (!Document.vocabulary.keySet().contains(split[i]))
				Document.vocabulary.put(split[i], wordCounter++);
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
	
	public static int getNumberOfCorpora()
	{
		return Document.corpora.size();
	}
}
