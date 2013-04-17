package driver;

import java.util.*;
import data.*;

public abstract class Sampler {
	
	/** The list of documents used to train the parameters */
	private DocumentCollection trainingDocuments;
	
	/** The list of documents used to test the parameters */
	private DocumentCollection testDocuments;
	
	/** The average value for theta computed over all iterations
	 *  after burn in on the training dataset */
	private double[][] average_train_thetas;
	
	/** The average value for theta computed over all iterations
	 *  after burn in on the training dataset */
	private double[][] average_test_thetas;
	
	/** The average value for phi_k_w computed over all iterations after burn in */
	private double[][] average_phi_k_w;
	
	/** The average value for phi_c_k_w computed over all iterations after burn in */
	private double[][][] average_phi_c_k_w;
	
	/** 
	 * Initialize on counts etc. for both training and test parameters
	 */
	public void initializeParameters()
	{
		
	}
	
	/**
	 * Sample over the provided document
	 * @param doc the document to sample from
	 */
	public void sampleDocument(Document doc)
	{
		
	}
	
	
	public abstract void sample(int documentNum, int wordNum, int corpusNum, int documentSize, int k)
	{
		
	}
	
	/**
	 * Calculates the new theta_train and theta_test
	 */
	public void updateThetas()
	{
		
	}
	
	/**
	 * Calculates the new phi_k_w
	 */
	public void updatePhi_k_w()
	{
		
	}
	
	/**
	 * Calculates the new phi_c_k_w
	 */
	public void updatePhi_c_k_w()
	{
		
	}
	
	public void writeOutput(String filePrefix)
	{
		
	}
	
	
	/**
	 * Computes the next iteration of the training data
	 * @return the likelihood of the updated parameters given the training data
	 */
	public abstract double trainParameters();
	
	/**
	 * Computes the likelihood of the test data given the
	 * current parameters
	 * @return The likelihood of the params given the test data
	 */
	public abstract double testParameters();
	
	/**
	 * Computes the likelihood of the data
	 * @param trainingData If true indicates that we compute the likelihood of the training data.
	 * Else, we compute the likelihood of the test data.
	 * @return the likelihood
	 */
	public double computeLikelihood(boolean trainingData)
	{
		
	}
	
	
	/**
	 * Normalizes a likelihood based on the number of tokens in the
	 * dataset the likelihood was computed for.
	 * @param likelihood the unnormalized likelihood
	 * @param numberOfTokens the number of tokens in the dataset (total number of
	 * words in a DocumentCollection)
	 * @return the normalized likelihood
	 */
	public double normalizeLikelihood(double likelihood, int numberOfTokens)
	{
		
	}

}
