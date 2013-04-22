package driver;

import data.Document;
import data.DocumentCollection;
import data.Multinomial;

public class CollapsedSampler extends Sampler {

	public CollapsedSampler(double l, double a, double b, int topics, DocumentCollection train,
			DocumentCollection test) {
		super(l, a, b, topics, train, test);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void trainingSample(Document doc, int documentNum, int wordNum,
			int corpusNum, int[] currentDocZs, int[] currentDocXs) {

		
		//		int k = localZ[i];
		
//		// randomly sample a new value for z_d,i
//		localZ[i] = sampleZ(d,i,w,c,n_d);
//		localX[i] = sampleX(w,c,k);
//
//		// update the counts to include the newly sampled assignments of the current token
//		k = localZ[i];
		
		
		
		// first sample Z
		int V = Document.vocabulary.size();
		double[] probabilities = new double[numTopics];
		String word = doc.getWord(wordNum);
		int w = Document.vocabulary.get(word);
		int n_d = doc.size();
		
		for (int k = 0; k < numTopics; k++)
		{
			
			if (currentDocXs[wordNum] == 0) {
				probabilities[k] = ((n_dk_train[documentNum][k] + alpha) / (n_d + numTopics * alpha)) * ((n_kw[k][w]  + beta) / ( n_k[k] + V * beta));
			} else {
				probabilities[k] = ((n_dk_train[documentNum][k] + alpha) / (n_d + numTopics * alpha)) * ((n_ckw[corpusNum][k][w] + beta) / (n_ck[corpusNum][k] + V * beta));
			}
		}
		
		Multinomial mult = new Multinomial(probabilities);
		int old_k = currentDocZs[wordNum];
		currentDocZs[wordNum] = mult.sample();
		
		// Now sample x using new value for k
		double[] xProbabilities = new double[2];
		//int k = currentDocZs[wordNum];
		
		//xProbabilities[0] = (1 - lambda) * ((n_kw[k][w] + beta) / (n_k[k] + V * beta));
		//xProbabilities[1] = lambda * (( n_ckw[corpusNum][k][w] ) / (n_ck[corpusNum][k] + V * beta));

		xProbabilities[0] = (1 - lambda) * ((n_kw[old_k][w] + beta) / (n_k[old_k] + V * beta));
		xProbabilities[1] = lambda * (( n_ckw[corpusNum][old_k][w] ) / (n_ck[corpusNum][old_k] + V * beta));

		
		Multinomial xMult = new Multinomial(xProbabilities);
		currentDocXs[wordNum] = xMult.sample();
	}

	@Override
	public void testingSample(Document doc, int documentNum, int wordNum,
			int corpusNum, int[] currentDocZs, int[] currentDocXs) {
		
		// first sample Z
		int V = Document.vocabulary.size();
		double[] probabilities = new double[numTopics];
		String word = doc.getWord(wordNum);
		int w = Document.vocabulary.get(word);
		int n_d = doc.size();

		for (int k = 0; k < numTopics; k++) {

			if (currentDocXs[wordNum] == 0) {
				probabilities[k] = ((n_dk_test[documentNum][k] + alpha) / (n_d + numTopics
						* alpha))
						* ((n_kw[k][w] + beta) / (n_k[k] + V * beta));
			} else {
				probabilities[k] = ((n_dk_test[documentNum][k] + alpha) / (n_d + numTopics
						* alpha))
						* ((n_ckw[corpusNum][k][w] + beta) / (n_ck[corpusNum][k] + V
								* beta));
			}

		}
		Multinomial mult = new Multinomial(probabilities);
		currentDocZs[wordNum] = mult.sample();

		// Now sample x using new value for k
		double[] xProbabilities = new double[2];
		int k = currentDocZs[wordNum];

		xProbabilities[0] = (1 - lambda)
				* ((n_kw[k][w] + beta) / (n_k[k] + V * beta));
		xProbabilities[1] = lambda
				* ((n_ckw[corpusNum][k][w]) / (n_ck[corpusNum][k] + V * beta));

		Multinomial xMult = new Multinomial(xProbabilities);
		currentDocXs[wordNum] = xMult.sample();

	}

}