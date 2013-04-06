package data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import driver.Main;

public class DataUtils
{
	public static List<Document> loadData(String fileName)
	{
		List<Document> documents = new ArrayList<Document>();
		
		try
		{
			Scanner scanner = new Scanner(new FileReader(fileName));
			while (scanner.hasNextLine())
				documents.add(new Document(scanner.nextLine()));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return documents;
	}
	
	/**
	 * Initializes the z data member of the Main class and updates the n data member.
	 */
	public static void intializeZ(List<Document> documents, int[][] n_dk, int[][] n_kw, int[] n_k, int[][][] n_ckw, int[][] n_ck, int z[][])
	{
		Random random = new Random();
		
		for (int d = 0; d < documents.size(); d++)
		{
			Document document = documents.get(d);
			z[d] = new int[document.size()];
			
			for (int i = 0; i < document.size(); i++)
			{
				int k = random.nextInt(Main.getNumberOfLabels());
				
				int c = document.getCorpus();
				
				String word = document.getWord(i);
				int w = Document.vocabulary.get(word);
				
				n_dk[d][k] += 1;
				n_kw[k][w] += 1;
				n_k[k] += 1;
				n_ckw[c][k][w] += 1;
				n_ck[c][k] += 1;
				
				z[d][i] = k;
				
				
			}

		}
	}
	
	public static void initializeX(List<Document> documents,int[][] x)
	{
		Random random = new Random();
		
		int d = 0;
		for (Document document : documents)
		{
			x[d] = new int[document.size()];
			
			for (int i = 0; i < document.size(); i++) {
				x[d][i] = random.nextInt(1);
			}
		
			d++;
		}
		
	}
}
