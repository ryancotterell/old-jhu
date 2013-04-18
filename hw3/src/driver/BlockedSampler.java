package driver;

import data.Document;
import data.DocumentCollection;
import data.Multinomial;

public class BlockedSampler extends Sampler {
	
	public BlockedSampler(double l, double a, double b, int topics, DocumentCollection train,
			DocumentCollection test) {
		super(l, a, b, topics, train, test);
		// TODO Auto-generated constructor stub
	}

	// d = docNum, i = wordNum, w = vocab word num, c = corpusNum, n_d = documentSize
	@Override
	public void trainingSample(Document doc, int documentNum, int wordNum,
			int corpusNum, int[] currentDocZs, int[] currentDocXs) {
		
		int V = Document.vocabulary.size();
		double[] probabilities = new double[numTopics * 2];
		int n_d = doc.size();
		String word = doc.getWord(wordNum);
		int w = Document.vocabulary.get(word);
		
		
		for (int k = 0; k < numTopics * 2; k++)
		{
			
			if (k < numTopics) {
				probabilities[k] = (1 - lambda) * ((n_dk_train[documentNum][k] + alpha) / (n_d + numTopics * alpha)) * ((n_kw[k][w]  + beta) / ( n_k[k] + V * beta));
			} else {
				int k_tmp = k - numTopics;
				probabilities[k] = (lambda) * ((n_dk_train[documentNum][k_tmp] + alpha) / (n_d + numTopics * alpha)) * ((n_ckw[corpusNum][k_tmp][w] + beta) / (n_ck[corpusNum][k_tmp] + V * beta));
			}

		}
		
		Multinomial mult = new Multinomial(probabilities);
		int sample =  mult.sample();
		
		int z;
		int x;
		if (sample >= numTopics) {
			z = sample - numTopics;
			x = 1;
		} else {
			z = sample;
			x = 0;
		}
		
		currentDocZs[wordNum] = z;
		currentDocXs[wordNum] = x;
	}

	@Override
	public void testingSample(Document doc, int documentNum, int wordNum,
			int corpusNum, int[] currentDocZs, int[] currentDocXs) {

		int V = Document.vocabulary.size();
		double[] probabilities = new double[numTopics * 2];
		int n_d = doc.size();
		String word = doc.getWord(wordNum);
		int w = Document.vocabulary.get(word);
		
		
		for (int k = 0; k < numTopics * 2; k++)
		{
			
			if (k < numTopics) {
				probabilities[k] = (1 - lambda) * ((n_dk_test[documentNum][k] + alpha) / (n_d + numTopics * alpha)) * ((n_kw[k][w]  + beta) / ( n_k[k] + V * beta));
			} else {
				int k_tmp = k - numTopics;
				probabilities[k] = (lambda) * ((n_dk_test[documentNum][k_tmp] + alpha) / (n_d + numTopics * alpha)) * ((n_ckw[corpusNum][k_tmp][w] + beta) / (n_ck[corpusNum][k_tmp] + V * beta));
			}

		}
		
		Multinomial mult = new Multinomial(probabilities);
		int sample =  mult.sample();
		
		int z;
		int x;
		if (sample >= numTopics) {
			z = sample - numTopics;
			x = 1;
		} else {
			z = sample;
			x = 0;
		}
		
		currentDocZs[wordNum] = z;
		currentDocXs[wordNum] = x;

	}

}
