package driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import data.DataUtils;
import data.Document;
import data.Multinomial;

public class Main
{
	private static final String INPUT_FILE = "data/input-train.txt";
	
	private static final int BURN_IN = 1000;
	private static final int ITERATIONS = 1100;
	// the number of topics
	private static int K = 25;
	
	private static int[][] n_dk;
	private static int[][] n_kw;
	private static int[] n_k;
	
	private static int[][][] n_ckw;
	private static int[][] n_ck;
	
	private static int[][] z;
	private static int[][] x;
	
	// P(collection-specific)
	private static double lambda = 0.5;
	
	private static double alpha = 0.1;
	private static double beta = 0.01;
	
	private static double[][] theta_samples;
	private static double[][] phi_k_w_samples;
	private static double[][][] phi_c_k_w_samples;
	
	private static int V;
	
	public static void main(String[] args)
	{
		List<Document> trainingDocuments = DataUtils.loadData(Main.INPUT_FILE);
		
		V = Document.vocabulary.size();
		
		// initialize variables
		
		//initialize z and x
		z = new int[trainingDocuments.size()][0];
		x = new int[trainingDocuments.size()][0];
		
		//initial sample bins
		theta_samples = new double[V][K];
		phi_k_w_samples = new double[K][V]; 
		phi_c_k_w_samples = new double[2][K][V];
		
		//initialize n values 
		n_dk = new int[trainingDocuments.size()][Main.getNumberOfLabels()];
		n_kw = new int[Main.getNumberOfLabels()][Document.vocabulary.size()];
		n_k = new int[Main.getNumberOfLabels()];
		
		n_ckw = new int[Document.getNumberOfCorpora()][Main.getNumberOfLabels()][Document.vocabulary.size()];
		n_ck  = new int[Document.getNumberOfCorpora()][Main.getNumberOfLabels()];
		
		DataUtils.intialize(trainingDocuments, n_dk,n_kw,n_k,n_ckw,n_ck,z,x);
		
		// start sampling
		for (int t = 0; t < Main.ITERATIONS; t++)
		{
			// for each token (d,i)
			for (int d = 0; d < trainingDocuments.size(); d++)
			{
				Document document = trainingDocuments.get(d);
				int[] localZ = Main.z[d];
				int[] localX = Main.x[d];
				
				int c = document.getCorpus();
				int n_d = document.size();
				
				for (int i = 0; i < document.size(); i++)
				{
					String word = document.getWord(i);
					// update the counts to exclude the assignments of the current token
					int k = localZ[i];
					
					int w = Document.vocabulary.get(word);
					
					n_dk[d][k] -= 1;
					
					if (localX[i] == 0) {
						n_k[k] -= 1;
						n_kw[k][w] -= 1;
					} else {
						n_ckw[c][k][w] -= 1;
						n_ck[c][k] -= 1;
					}
						
					// randomly sample a new value for z_d,i
					localZ[i] = sampleZ(d,i,w,c,n_d);
					localX[i] = sampleX(w,c,k);

					// update the counts to include the newly sampled assignments of the current token
					k = localZ[i];
					
					n_dk[d][k] += 1;
					
					if (localX[i] == 0) {
						n_k[k] += 1;
						n_kw[k][w] += 1;
					} else {
						n_ckw[c][k][w] += 1;
						n_ck[c][k] += 1;
					}
					
				}
			}
			// estimate the parameters
			double[][] theta = Main.calculateTheta(t,trainingDocuments);
			double[][] phi_k_w = Main.calculatePhi_k_w(t);
			double[][][] phi_c_k_w = Main.calculatePhi_c_k_w(t);
		
			
			if (t > BURN_IN) {
				
			}
			// compute the log-likelihood
			System.out.println("Likelihood: " + Main.computeLikelihood(trainingDocuments, theta, phi_k_w, phi_c_k_w));
	}
		
		Main.extractTopcis();
		
	}
	
	private static double computeLikelihood(List<Document> documents, double[][] theta, double[][] phi_k_w, double[][][] phi_c_k_w)
	{
		double likelihood = 0.0;
		for (int d = 0; d < documents.size(); ++d)
		{
			Document currentDocument = documents.get(d);
			for (int i = 0; i < currentDocument.size(); ++i)
			{
				double logSum = 0;
				for (int k = 0; k < Main.K; ++k)
				{
					int vocabIndex = Document.vocabulary.get(currentDocument.getWord(i));
					logSum += theta[d][k]*(
							(1-Main.lambda)*phi_k_w[k][vocabIndex]
							+ Main.lambda*phi_c_k_w[currentDocument.getCorpus()][k][vocabIndex] );
				}
				likelihood += Math.log(logSum);
			}
		}
		
		return likelihood;
	}
	
	private static void extractTopcis()
	{
		try
		{
			FileWriter writer = new FileWriter("data/output-phi.txt");
			FileWriter writer0 = new FileWriter("data/output-phi0.txt");
			FileWriter writer1 = new FileWriter("data/output-phi1.txt");
//			FileWriter writer = new FileWriter("data/output.txt");
			double[][] phi_k_w = phi_k_w_samples;
			double[][][] phi_c_k_w = phi_c_k_w_samples;
			
			int counter = 1;
			int size = Document.vocabulary.keySet().size();
			for (String word : Document.vocabulary.keySet())
			{
				writer.write(word);
				writer0.write(word);
				writer1.write(word);
				for (int k = 0; k < Main.K; k++)
				{
					writer.write(" " + phi_k_w[k][Document.vocabulary.get(word)]);
					writer0.write(" " + phi_c_k_w[0][k][Document.vocabulary.get(word)]);
					writer1.write(" " + phi_c_k_w[1][k][Document.vocabulary.get(word)]);

				}
				
				if (size != counter) {
					writer.write("\n");
					writer0.write("\n");
					writer1.write("\n");
				}
				
				
			}
			
			writer.close();
			writer0.close();
			writer1.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static double[][] calculatePhi_k_w(int t)
	{
		double[][] phi_k_w = new double[Main.K][Document.vocabulary.size()];
		
		for (int k = 0; k < Main.K; k++)
		{
			for (int w = 0; w < Document.vocabulary.size(); w++)
			{
				phi_k_w[k][w] = (( n_kw[k][w] + beta) / (n_k[k] + V * beta));
				
				if (t > Main.BURN_IN) {
					phi_k_w_samples[k][w] += phi_k_w[k][w];
				}
			}
		}
		
		return phi_k_w;
	}
	
	private static double[][][] calculatePhi_c_k_w(int t)
	{
		double[][][] phi_c_k_w = new double[Document.getNumberOfCorpora()][Main.K][Document.vocabulary.size()];

		for (int k = 0; k < Main.K; k++)
		{
			for (int w = 0; w < Document.vocabulary.size(); w++)
			{
				for (int c = 0; c < Document.getNumberOfCorpora(); c++) {
					phi_c_k_w[c][k][w] = (( n_ckw[c][k][w] + beta) / (n_ck[c][k] + V * beta));
					
					if (t > Main.BURN_IN) {
						phi_c_k_w_samples[c][k][w] += phi_c_k_w[c][k][w];
					}
				}
			}
		}
		
		return phi_c_k_w;
	}
	
	private static double[][] calculateTheta(int t, List<Document> documents)
	{
		double[][] theta = new double[documents.size()][Main.K];

		for (int d = 0; d < documents.size(); d++)
		{
			Document document = documents.get(d);
		
			int n_d = document.size();
			
			for (int k = 0; k < Main.K; k++) {
				theta[d][k] = (n_dk[d][k] + Main.alpha) / (n_d + Main.K * Main.alpha);
				if (t > Main.BURN_IN) {
					theta_samples[d][k] += theta[d][k];
				}
			}
		}
		
		return theta;
	}
	
	
	private static int sampleZ(int d, int i,int w, int c, int n_d)
	{
		
		int V = Document.vocabulary.size();
		double[] probabilities = new double[Main.K];
		
		
		for (int k = 0; k < Main.K; k++)
		{
			
			if (x[d][i] == 0) {
				probabilities[k] = ((n_dk[d][k] + alpha) / (n_d + K * alpha)) * ((n_kw[k][w]  + beta) / ( n_k[k] + V * beta));
			} else {
				probabilities[k] = ((n_dk[d][k] + alpha) / (n_d + K * alpha)) * ((n_ckw[c][k][w] + beta) / (n_ck[c][k] + V * beta));
			}

		}
		
		Multinomial mult = new Multinomial(probabilities);
		return mult.sample();
	}
	
	private static int sampleX(int w, int c, int k)
	{
		int V = Document.vocabulary.size();

		double[] probabilities = new double[2];
		
		probabilities[0] = (1 - Main.lambda) * ((n_kw[k][w] + Main.beta) / (n_k[k] + V * beta));
		probabilities[1] = Main.lambda * (( n_ckw[c][k][w] ) / (n_ck[c][k] + V * beta));

		Multinomial mult = new Multinomial(probabilities);
		return mult.sample();
	}
	
	public static int getNumberOfLabels()
	{
		return Main.K;
	}
}
