package driver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import data.DataUtils;
import data.Document;
import data.Multinomial;

public class CollapsedSampler
{
	private static String trainingFile = "data/input-train.txt";
	private static String testFile = "";
	private static String outputFile = "";
	
	private static int burnIn = 1000;
	private static int iterations = 1100;
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
		CollapsedSampler.readParameters(args);
		
		List<Document> trainingDocuments = DataUtils.loadData(CollapsedSampler.trainingFile);
		
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
		n_dk = new int[trainingDocuments.size()][CollapsedSampler.getNumberOfLabels()];
		n_kw = new int[CollapsedSampler.getNumberOfLabels()][Document.vocabulary.size()];
		n_k = new int[CollapsedSampler.getNumberOfLabels()];
		
		n_ckw = new int[Document.getNumberOfCorpora()][CollapsedSampler.getNumberOfLabels()][Document.vocabulary.size()];
		n_ck  = new int[Document.getNumberOfCorpora()][CollapsedSampler.getNumberOfLabels()];
		
		DataUtils.intialize(trainingDocuments, n_dk,n_kw,n_k,n_ckw,n_ck,z,x);
		
		// start sampling
		for (int t = 0; t < CollapsedSampler.iterations; t++)
		{
			// for each token (d,i)
			for (int d = 0; d < trainingDocuments.size(); d++)
			{
				Document document = trainingDocuments.get(d);
				int[] localZ = CollapsedSampler.z[d];
				int[] localX = CollapsedSampler.x[d];
				
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
			double[][] theta = CollapsedSampler.calculateTheta(t,trainingDocuments);
			double[][] phi_k_w = CollapsedSampler.calculatePhi_k_w(t);
			double[][][] phi_c_k_w = CollapsedSampler.calculatePhi_c_k_w(t);
		
			
			if (t > burnIn) {
				
			}
			// compute the log-likelihood
			System.out.println("Likelihood: " + CollapsedSampler.computeLikelihood(trainingDocuments, theta, phi_k_w, phi_c_k_w));
	}
		
		CollapsedSampler.extractTopcis();
		
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
				for (int k = 0; k < CollapsedSampler.K; ++k)
				{
					int vocabIndex = Document.vocabulary.get(currentDocument.getWord(i));
					logSum += theta[d][k]*(
							(1-CollapsedSampler.lambda)*phi_k_w[k][vocabIndex]
							+ CollapsedSampler.lambda*phi_c_k_w[currentDocument.getCorpus()][k][vocabIndex] );
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
				for (int k = 0; k < CollapsedSampler.K; k++)
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
		double[][] phi_k_w = new double[CollapsedSampler.K][Document.vocabulary.size()];
		
		for (int k = 0; k < CollapsedSampler.K; k++)
		{
			for (int w = 0; w < Document.vocabulary.size(); w++)
			{
				phi_k_w[k][w] = (( n_kw[k][w] + beta) / (n_k[k] + V * beta));
				
				if (t > CollapsedSampler.burnIn) {
					phi_k_w_samples[k][w] += phi_k_w[k][w] / (iterations - burnIn);
				}
			}
		}
		
		return phi_k_w;
	}
	
	private static double[][][] calculatePhi_c_k_w(int t)
	{
		double[][][] phi_c_k_w = new double[Document.getNumberOfCorpora()][CollapsedSampler.K][Document.vocabulary.size()];

		for (int k = 0; k < CollapsedSampler.K; k++)
		{
			for (int w = 0; w < Document.vocabulary.size(); w++)
			{
				for (int c = 0; c < Document.getNumberOfCorpora(); c++) {
					phi_c_k_w[c][k][w] = (( n_ckw[c][k][w] + beta) / (n_ck[c][k] + V * beta));
					
					if (t > CollapsedSampler.burnIn) {
						phi_c_k_w_samples[c][k][w] += phi_c_k_w[c][k][w] / (iterations - burnIn);
					}
				}
			}
		}
		
		return phi_c_k_w;
	}
	
	private static double[][] calculateTheta(int t, List<Document> documents)
	{
		double[][] theta = new double[documents.size()][CollapsedSampler.K];

		for (int d = 0; d < documents.size(); d++)
		{
			Document document = documents.get(d);
		
			int n_d = document.size();
			
			for (int k = 0; k < CollapsedSampler.K; k++) {
				theta[d][k] = (n_dk[d][k] + CollapsedSampler.alpha) / (n_d + CollapsedSampler.K * CollapsedSampler.alpha);
				if (t > CollapsedSampler.burnIn) {
					theta_samples[d][k] += theta[d][k] / (iterations - burnIn);
				}
			}
		}
		
		return theta;
	}
	
	
	private static int sampleZ(int d, int i,int w, int c, int n_d)
	{
		
		int V = Document.vocabulary.size();
		double[] probabilities = new double[CollapsedSampler.K];
		
		
		for (int k = 0; k < CollapsedSampler.K; k++)
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
		
		probabilities[0] = (1 - CollapsedSampler.lambda) * ((n_kw[k][w] + CollapsedSampler.beta) / (n_k[k] + V * beta));
		probabilities[1] = CollapsedSampler.lambda * (( n_ckw[c][k][w] ) / (n_ck[c][k] + V * beta));

		Multinomial mult = new Multinomial(probabilities);
		return mult.sample();
	}
	
	public static int getNumberOfLabels()
	{
		return CollapsedSampler.K;
	}
	
	private static void readParameters(String[] args)
	{
		if (args.length == 9)
		{
			CollapsedSampler.trainingFile = args[0];
			CollapsedSampler.testFile = args[1];
			CollapsedSampler.outputFile = args[2];
			CollapsedSampler.K = Integer.parseInt(args[3]);
			CollapsedSampler.lambda = Double.parseDouble(args[4]);
			CollapsedSampler.alpha = Double.parseDouble(args[5]);
			CollapsedSampler.beta = Double.parseDouble(args[6]);
			CollapsedSampler.iterations = Integer.parseInt(args[7]);
			CollapsedSampler.burnIn = Integer.parseInt(args[8]);
		}
	}
}
