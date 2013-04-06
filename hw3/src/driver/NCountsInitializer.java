package driver;

import java.util.List;

public class NCountsInitializer
{
	// the number of words in document d assigned to topic i
		// [document][topic] = count
		public int[][] n;
		
		// the number of times "Dan" is assigned to each label
		// Maps from word index in the vocabulary to an array of counts
		// [corpus][word][topic] = count
		public int[][][] n_k;
		
		// the number of words assigned to label k
		// [corpus][label] = count
		public int[][] n_star;
		
		public List<int[]> z;
		
		public NCountsInitializer(int[][]_n, int[][][] _n_k, int[][] _n_star, List<int[]> _z)
		{
			n = _n;
			n_k = _n_k;
			n_star = _n_star;
			z = _z;
		}
}
