package driver;

import java.io.FileWriter;
import java.util.*;

import data.*;

public abstract class Sampler {

	/** The list of documents used to train the parameters */
	protected DocumentCollection trainingDocuments;

	/** The list of documents used to test the parameters */
	protected DocumentCollection testDocuments;

	/**
	 * The average value for theta computed over all iterations after burn in on
	 * the training dataset
	 */
	protected double[][] average_train_thetas;
	
	/** The latest point estimate for theta */
	protected double[][] trainThetas;
	
	/** The latest point estimate for theta */
	protected double[][] testThetas;

	/**
	 * The average value for theta computed over all iterations after burn in on
	 * the training dataset
	 */
	protected double[][] average_test_thetas;
	
	/** Keeps track of the number of values we are averaging over */
	double averageCount;

	/** The average value for phi_k_w computed over all iterations after burn in */
	protected double[][] average_phi_k_w;
	
	/** The latest point estimate for phi_k_w */
	protected double[][] phi_k_w;
	
	/**
	 * The average value for phi_c_k_w computed over all iterations after burn
	 * in
	 */
	protected double[][][] average_phi_c_k_w;
	
	/** The latest point estimate for phi_c_k_w */
	protected double[][][] phi_c_k_w;

	/** # of tokens in document d assigned to topic k for training docs */
	protected int[][] n_dk_train;

	/** # of tokens in document d assigned to topic k for test docs */
	protected int[][] n_dk_test;

	/**
	 * # of tokens assigned to topic k of word type w (only computed for train
	 * docs)
	 */
	protected int[][] n_kw;

	/** total # of tokens assigned to topic k (only for train docs) */
	protected int[] n_k;

	/** # of tokens of word type w assigned to topic k in corpus c */
	protected int[][][] n_ckw;

	/** total # of tokens assigned to topic k in corpus c */
	protected int[][] n_ck;

	/** current sampled values for z on training data */
	protected int[][] z_train;

	/** current sampled values for x on training data */
	protected int[][] x_train;

	/** current sampled values for z on test data */
	protected int[][] z_test;

	/** current sampled values for x on test data */
	protected int[][] x_test;

	/** The total number of topics, K */
	protected int numTopics;

	/** Probability of whether to look at corpus specific counts */
	protected double lambda;

	/** Hyperparameter */
	protected double alpha;

	/** Hyperparameter */
	protected double beta;

	public Sampler(double l, double a, double b, int topics, DocumentCollection train,
			DocumentCollection test) {
		lambda = l;
		alpha = a;
		beta = b;
		numTopics = topics;
		trainingDocuments = train;
		testDocuments = test;
		initializeParameters();
		average_train_thetas = new double[trainingDocuments.size()][numTopics];
		average_test_thetas = new double[testDocuments.size()][numTopics];
		int vocabSize = Document.vocabulary.size();
		average_phi_k_w = new double[numTopics][vocabSize];
		average_phi_c_k_w = new double[trainingDocuments.getNumberOfCorpora()][numTopics][vocabSize];
	}

	/**
	 * Initialize counts etc. for both training and test parameters
	 */
	public void initializeParameters() {
		initializeXs();
		initializeTrainCounts();
		initializeTestCounts();
	}

	/**
	 * Initialize all parameters used when passing over training data
	 */
	private void initializeTrainCounts() {
		Random rand = new Random();
		z_train = new int[trainingDocuments.getNumberOfDocuments()][];
		n_dk_train = new int[trainingDocuments.getNumberOfDocuments()][numTopics];

		n_kw = new int[numTopics][Document.vocabulary.size()];
		n_k = new int[numTopics];

		n_ckw = new int[trainingDocuments.getNumberOfCorpora()][numTopics][Document.vocabulary
				.size()];
		n_ck = new int[trainingDocuments.getNumberOfCorpora()][numTopics];

		int docNum = 0;
		for (Document doc : trainingDocuments) {
			z_train[docNum] = new int[doc.size()];
			for (int i = 0; i < doc.size(); ++i) {
				int k = rand.nextInt(numTopics);
				int c = doc.getCorpus();
				String word = doc.getWord(i);
				int w = Document.vocabulary.get(word);
				n_dk_train[docNum][k] += 1;

				if (x_train[docNum][i] == 0) {
					n_kw[k][w] += 1;
					n_k[k] += 1;
				} else {
					n_ckw[c][k][w] += 1;
					n_ck[c][k] += 1;
				}
				z_train[docNum][i] = k;
			}

		}
	}
	
	/**
	 * Initialize parameters specific to test data
	 */
	private void initializeTestCounts()
	{
		Random rand = new Random();
		z_test = new int[testDocuments.getNumberOfDocuments()][];
		n_dk_test = new int[testDocuments.getNumberOfDocuments()][numTopics];

		int docNum = 0;
		for (Document doc : testDocuments) {
			z_test[docNum] = new int[doc.size()];
			for (int i = 0; i < doc.size(); ++i) {
				int k = rand.nextInt(numTopics);
				n_dk_test[docNum][k] += 1;
				z_test[docNum][i] = k;
			}
		}
	}

	private void initializeXs() {
		Random rand = new Random();
		x_train = new int[trainingDocuments.getNumberOfDocuments()][];
		x_test = new int[testDocuments.getNumberOfDocuments()][];
		int trainDocNum = 0;
		for (Document doc : trainingDocuments) {
			x_train[trainDocNum] = new int[doc.size()];
			for (int i = 0; i < doc.size(); ++i) {
				x_train[trainDocNum][i] = rand.nextInt(2);
			}
			++trainDocNum;
		}
		int testDocNum = 0;
		for (Document doc : testDocuments) {
			x_test[testDocNum] = new int[doc.size()];
			for (int i = 0; i < doc.size(); ++i) {
				x_test[testDocNum][i] = rand.nextInt(2);
			}
			++testDocNum;
		}
	}
	
	/**
	 * Iterate through all train and test docs then update params
	 * @param burnIn If true indicates this is a burn in iteration. The param values will not be
	 * used to update the mean param values
	 */
	public void runIteration(boolean burnIn)
	{
		for (int doc = 0; doc < trainingDocuments.size(); ++doc) {
			sampleTrainDocument(doc);
		}
		for (int doc = 0; doc < testDocuments.size(); ++doc) {
			sampleTestDocument(doc);
		}
		updateThetas(burnIn);
		updatePhi_k_w(burnIn);
		updatePhi_c_k_w(burnIn);
		if (!burnIn) {
			averageCount += 1;
		}
	}

	/**
	 * Sample over the provided document
	 * @param doc
	 *            the document to sample from
	 */
	private void sampleTrainDocument(int docNum) {
		Document doc = trainingDocuments.getDocument(docNum);
		int[] currentDocZs = z_train[docNum];
		int[] currentDocXs = x_train[docNum];
		
		int c = doc.getCorpus();
		
		for (int i = 0; i < doc.size(); ++i) {
			String word = doc.getWord(i);
			int k = currentDocZs[i];
			int w = Document.vocabulary.get(word);
			
			//update the counts to exclude the assignments of the current token
			n_dk_train[docNum][k] -= 1;
			
			if (currentDocXs[i] == 0) {
				n_k[k] -= 1;
				n_kw[k][w] -= 1;
			} else {
				n_ck[c][k] -= 1;
				n_ckw[c][k][w] -= 1;
			}
			
			trainingSample(doc, docNum, i, c, currentDocZs, currentDocXs);
			k = currentDocZs[i];
			n_dk_train[docNum][k] += 1;
			
			if (currentDocXs[i] == 0) {
				n_k[k] += 1;
				n_kw[k][w] += 1;
			} else {
				n_ck[c][k] += 1;
				n_ckw[c][k][w] += 1;
			}
		}
	}
	
	private void sampleTestDocument(int docNum) {
		Document doc = testDocuments.getDocument(docNum);
		int[] currentDocZs = z_train[docNum];
		int[] currentDocXs = x_train[docNum];
		
		int c = doc.getCorpus();
		
		for (int i = 0; i < doc.size(); ++i) {
			String word = doc.getWord(i);
			int k = currentDocZs[i];
			int w = Document.vocabulary.get(word);
			
			//update the counts to exclude the assignments of the current token
			n_dk_test[docNum][k] -= 1;
			
			testingSample(doc, docNum, i, c, currentDocZs, currentDocXs);
			k = currentDocZs[i];
			n_dk_train[docNum][k] += 1;
		}
	}

	// Will also need w = Document.vocabulary.get(word) after finding word based on wordnum in doc
	// Will also need doc_size
	public abstract void trainingSample(Document doc, int documentNum, int wordNum, int corpusNum, int[] currentDocZs, int[] currentDocXs);
	
	// Will also need w = Document.vocabulary.get(word) after finding word based on wordnum in doc
	// Will also need 
	public abstract void testingSample(Document doc, int documentNum, int wordNum, int corpusNum, int[] currentDocZs, int[] currentDocXs);

	/**
	 * Calculates the new theta_train and theta_test
	 */
	public void updateThetas(boolean burnIn) {
		double[][] newTrainThetas = new double[trainingDocuments.size()][numTopics];
		for (int docNum = 0; docNum < trainingDocuments.size(); ++docNum) {
			Document doc = trainingDocuments.getDocument(docNum);
			int n_d = doc.size();
			for (int k = 0; k < numTopics; ++k) {
				newTrainThetas[docNum][k] = (n_dk_train[docNum][k] + alpha) / (n_d + numTopics * alpha);
				if (!burnIn) {
					average_train_thetas[docNum][k] += newTrainThetas[docNum][k];
				}
			}
		}
		trainThetas = newTrainThetas;
		
		double[][] newTestThetas = new double[testDocuments.size()][numTopics];
		for (int docNum = 0; docNum < testDocuments.size(); ++docNum) {
			Document doc = testDocuments.getDocument(docNum);
			int n_d = doc.size();
			for (int k = 0; k < numTopics; ++k) {
				newTestThetas[docNum][k] = (n_dk_test[docNum][k] + alpha) / (n_d + numTopics * alpha);
				if (!burnIn) {
					average_test_thetas[docNum][k] += newTestThetas[docNum][k];
				}
			}
		}
		testThetas = newTestThetas;
	}

	/**
	 * Calculates the new phi_k_w
	 */
	public void updatePhi_k_w(boolean burnIn) {
		int vocabSize = Document.vocabulary.size();
		double[][] newPhi_k_w = new double[numTopics][vocabSize];
		for (int k = 0; k < numTopics; ++k) {
			for (int w = 0; w < vocabSize; ++w) {
				newPhi_k_w[k][w] = (( n_kw[k][w] + beta) / (n_k[k] + vocabSize * beta));
				if (!burnIn) {
					average_phi_k_w[k][w] += newPhi_k_w[k][w];
				}
			}
		}
		phi_k_w = newPhi_k_w;
	}

	/**
	 * Calculates the new phi_c_k_w
	 */
	public void updatePhi_c_k_w(boolean burnIn) {
		int vocabSize = Document.vocabulary.size();
		double[][][] newPhi_c_k_w = new double[trainingDocuments.getNumberOfCorpora()][numTopics][vocabSize];
		for (int k = 0; k < numTopics; ++k) {
			for (int w = 0; w < vocabSize; ++k) {
				for (int c = 0; c < trainingDocuments.getNumberOfCorpora(); ++k) {
					newPhi_c_k_w[c][k][w] = (( n_ckw[c][k][w] + beta) / (n_ck[c][k] + vocabSize * beta));
					if (!burnIn) {
						average_phi_c_k_w[c][k][w] += newPhi_c_k_w[c][k][w];
					}
				}
			}
		}
		phi_c_k_w = newPhi_c_k_w;
	}
	
	public double[][] getAverageTrainThetas() {
		double[][] averages = new double[trainingDocuments.size()][numTopics];
		for (int i = 0; i < trainingDocuments.size(); ++i) {
			for (int j = 0; j < numTopics; ++j) {
				averages[i][j] = average_train_thetas[j][j] / averageCount;
			}
		}
		return averages;
		
	}
	
	public double[][] getAverageTestThetas() {
		double[][] averages = new double[testDocuments.size()][numTopics];
		for (int i = 0; i < trainingDocuments.size(); ++i) {
			for (int j = 0; j < numTopics; ++j) {
				averages[i][j] = average_test_thetas[j][j] / averageCount;
			}
		}
		return averages;
	}
	
	public double[][] getAveragePhi_kw() {
		int vocabSize = Document.vocabulary.size();
		double[][] averages = new double[numTopics][vocabSize];
		for (int i = 0; i < numTopics; ++i) {
			for (int j = 0; j < vocabSize; ++j) {
				averages[i][j] = average_phi_k_w[j][j] / averageCount;
			}
		}
		return averages;
	}
	
	public double[][][] getAveragePhi_ckw() {
		int vocabSize = Document.vocabulary.size();
		double[][][] averages = new double[trainingDocuments.getNumberOfCorpora()][numTopics][vocabSize];
		for (int c = 0; c < trainingDocuments.getNumberOfCorpora(); ++c) {
			for (int i = 0; i < numTopics; ++i) {
				for (int j = 0; j < vocabSize; ++j) {
					averages[c][i][j] = average_phi_c_k_w[c][j][j] / averageCount;
				}
			}
		}
		return averages;
	}

	

	/**
	 * Computes the likelihood of the data
	 * 
	 * @param trainingData
	 *            If true indicates that we compute the likelihood of the
	 *            training data. Else, we compute the likelihood of the test
	 *            data.
	 * @return the likelihood
	 */
	public double computeLikelihood(boolean trainingData) {
		double[][] theta;
		DocumentCollection currentDocuments;
		if (trainingData) {
			theta = trainThetas;
			currentDocuments = trainingDocuments;
		} else {
			theta = testThetas;
			currentDocuments = testDocuments;
		}
		
		double likelihood = 0.0;
		for (int d = 0; d < currentDocuments.size(); ++d) {
			Document currentDoc = currentDocuments.getDocument(d);
			for (int i = 0; i < currentDoc.size(); ++i) {
				double logSum = 0;
				for (int k = 0; k < numTopics; ++k) {
					int vocabIndex = Document.vocabulary.get(currentDoc.getWord(i));
					logSum += theta[d][k]*(
							(1-lambda)*phi_k_w[k][vocabIndex]
							+ lambda*phi_c_k_w[currentDoc.getCorpus()][k][vocabIndex] );
				}
				likelihood += Math.log(logSum);
			}
		}
		return likelihood;
	}

	/**
	 * Normalizes a likelihood based on the number of tokens in the dataset the
	 * likelihood was computed for.
	 * 
	 * @param likelihood
	 *            the unnormalized likelihood
	 * @param numberOfTokens
	 *            the number of tokens in the dataset (total number of words in
	 *            a DocumentCollection)
	 * @return the normalized likelihood
	 */
	/*public double normalizeLikelihood(double likelihood, ) {
		return 0;
	}*/

}
