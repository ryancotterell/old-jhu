package variational;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.special.Gamma;

import data.DataUtils;
import data.Document;
import driver.Sampler;

public class Variational
{
	
	private static final int E_STEP_ITERATIONS = 10;
	private static final int MAX_ITERATIONS = 100;

	private static String trainingFile;// = "data/input-train.txt";
	private static String testFile;
	private static String outputFile;

	
	// the number of topics
	private static int K;// = 10;
	

	
	private static int V;	
	private static int D; // number of documents
	
	private static double alpha;// = 1.0;
	private static double beta;// = -10000;
	private static double lambda;
	
	static double[][] gamma_dk ;
	
	static double[][][] delta_dnk;

	static double[][] phi_kv;
	
	
	static List<Document> trainingDocuments;
	
	public static void main(String[] args)
	{
		Variational.readParameters(args);
		
		trainingDocuments = DataUtils.loadData(Variational.trainingFile);
		
		V = Document.vocabulary.size();
		D = trainingDocuments.size();

		gamma_dk = new double[D][K];
		
		delta_dnk = new double[D][0][0];

		phi_kv = new double[K][V];
		
		double[] beta_total = new double[K];
		for (int k = 0; k < K; ++k) {
			for (int v = 0; v < V; ++v) {
				phi_kv[k][v] = (1.0 / V)  + getPertubation(); 
				beta_total[k] += phi_kv[k][v];
			}
		}
		//renormalize beta
		//note that beta is in log space
		for (int k = 0; k < K; ++k) {
			for (int v = 0; v < V; ++v) {
				phi_kv[k][v] = Math.log(phi_kv[k][v] / beta_total[k]);
				
			}
		}
		
		
		//initialize of the variational stuff
		for (int d = 0; d < D; ++d) {
			Document doc = trainingDocuments.get(d);
			int N = doc.size();
			delta_dnk[d] = new double[N][K];

		
			double[] phi_dnk_t1_total = new double[K];
			for (int n = 0; n < N; ++n) {
				for (int k = 0; k < K; ++k) {
					delta_dnk[d][n][k] = 1.0 / K  + getPertubation();	
					phi_dnk_t1_total[k] += delta_dnk[d][n][k];		
					gamma_dk[d][k] = alpha + ((double) N ) / K + getPertubation();

				}
				for (int k = 0; k < K; ++k) {
					delta_dnk[d][n][k] = delta_dnk[d][n][k] / phi_dnk_t1_total[k];
				}
				
			}
				
		}
		ArrayList<Double> lls = new ArrayList<Double>();
		for (int i = 0; i< MAX_ITERATIONS; ++i) {
			double likelihood = eStep();
			//System.out.println(likelihood);
			lls.add(likelihood);
			mStep();
		}
		
		writeOutput(lls);
	}

	
	private static double eStep() {
		double ll = 0.0;
		
		for (int d = 0; d < D; ++d) {
			int numIterations = 0;

			
			Document doc = trainingDocuments.get(d);
			int N = doc.size();
			
			boolean converged = false;

			double[] oldDelta = new double[K];
			double[] digammaOfGamma = new double[K];
				
			double digammaOfGammaSum = 0.0;
			for (int k = 0; k < K; ++k) {
				digammaOfGamma[k] = Gamma.digamma(gamma_dk[d][k]);
				digammaOfGammaSum += Gamma.digamma(gamma_dk[d][k]);
			}
			
			while (!converged) {
				++numIterations;
				for (int n = 0; n < N; ++n) {
					double deltaSum = Double.NEGATIVE_INFINITY;
					int w = Document.vocabulary.get(doc.getWord(n));
					
					double deltaKSum = Double.NEGATIVE_INFINITY;
					//for (int k = 0; k < K; ++k) {
					//	deltaKSum = logAdd(deltaKSum,delta_dnk[d][n][k]);
					//}
					
					for (int k = 0; k < K; ++k) {
						oldDelta[k] = delta_dnk[d][n][k];
						
						
						delta_dnk[d][n][k] = digammaOfGamma[k] + phi_kv[k][w];
						
						if (k > 0) {
							deltaSum = logAdd(delta_dnk[d][n][k],deltaSum);
						} else {
							deltaSum = delta_dnk[d][n][k];
						}
											}
					
					for (int k = 0; k < K; ++k) {
						delta_dnk[d][n][k] = Math.exp(delta_dnk[d][n][k] - deltaSum);
					}
					
					 
				}
				
				for (int k = 0; k < K; ++k) {
					gamma_dk[d][k] = alpha;
					for (int n = 0; n < N; ++n) {
						gamma_dk[d][k] += delta_dnk[d][n][k];
					}
				}
				
				if (numIterations == E_STEP_ITERATIONS) {
					converged = true;
				}
				
			}
			
			ll += likelihood(d);
			
		}
		System.out.println(ll);
		return ll;
		
	}
	
	private static double logSum(double[] array) {
		double total = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < array.length; ++i) {
			total = logAdd(total,array[i]);
		}
		return total;
	}
	
	private static double sum(double[] array) {
		double total = 0.0;
		for (int i = 0; i < array.length; ++i)
			total += array[i];
		
		return total;
	}
	
	private static void mStep() {
		for (int k = 0; k < K; ++k) {
			//initialize beta
			for (int v = 0; v < V; ++v) {
				phi_kv[k][v] = beta;
			}
			
			double normalizationConstant = Double.NEGATIVE_INFINITY;
			
			
			for (int d = 0; d < D; ++d) {
				
				Document doc = trainingDocuments.get(d);
				int N = doc.size();
				
				for (int n = 0; n < N; ++n) {
					int w = Document.vocabulary.get(doc.getWord(n));
					
					phi_kv[k][w] = logAdd(Math.log(delta_dnk[d][n][k]),phi_kv[k][w]);
	
				}
			}
			for (int w = 0; w < V; ++w) {
				normalizationConstant = logAdd(normalizationConstant, phi_kv[k][w]);
			}
			
			for (int w = 0; w < V; ++w) {
				phi_kv[k][w] = phi_kv[k][w] - normalizationConstant;
			}
			
		}
	}
	
	
	/**
	 * Writes out mean parameter values
	 * 
	 * @param filePrefix
	 */
	private static void writeOutput(ArrayList<Double> trainlls) {
		String filePrefix = outputFile + "-" + K + "-" + alpha;
		try {
			
			PrintWriter phi     = new PrintWriter(new FileWriter(filePrefix + "-phi.txt"));
			PrintWriter trainLL = new PrintWriter(new FileWriter(filePrefix + "-trainll.txt"));
			PrintWriter theta   = new PrintWriter(new FileWriter(filePrefix + "-theta.txt"));
			double[][] phiKW = phi_kv;
			double[][] trainThetas = gamma_dk;
			
			// write thetas
			for (int d = 0; d < trainThetas.length; ++d) {
				theta.printf("%.13e", trainThetas[d][0]);
				for (int k = 1; k < trainThetas[0].length; ++k) {
					theta.printf(" %.13e", trainThetas[d][k]);
				}
			}

			// write likelihoods
			for (int i = 0; i < trainlls.size(); ++i) {
				trainLL.printf("%.13e\n", trainlls.get(i));
			}

			int vocabSize = Document.vocabulary.size();

			// write phis
			int counter = 1;
			int size = Document.vocabulary.keySet().size();
			for (String word : Document.vocabulary.keySet()) {
				phi.printf(word);
				
				for (int k = 0; k < K; ++k) {
					phi.printf(" %.13e", phiKW[k][Document.vocabulary.get(word)]);
				}

				if (size != counter) {
                    phi.write("\n");
				}
				counter += 1;
			}
			phi.close();
			trainLL.close();
			theta.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Extract the topics
	 */
	private static void extractTopics()
	{
		try
		{
			FileWriter writer = new FileWriter(outputFile);

			
			int counter = 1;
			int size = Document.vocabulary.keySet().size();
			for (String word : Document.vocabulary.keySet())
			{
				writer.write(word);
				for (int k = 0; k < K; k++)
				{
					writer.write(" " + phi_kv[k][Document.vocabulary.get(word)]);
					
				}
				
				if (size != counter) {
					writer.write("\n");
				
				}
				
				
			}
			
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Computes the lower bound on the log likelihood
	 * @param d
	 * @return
	 */
	private static double likelihood(int d) {
		double ll = 0.0;
		double gammaSum = 0.0;
		
		Document doc = trainingDocuments.get(d);
		int N = doc.size();
		
		for (int k = 0; k < K; ++k) {
			gammaSum += gamma_dk[d][k];
		}
		ll += Gamma.logGamma(K * alpha);
		ll -= K * Gamma.logGamma(alpha);
		ll -= Gamma.logGamma(gammaSum); 
		
		for (int k = 0; k < K; ++k) {
			ll += (alpha - 1) * Gamma.digamma(gamma_dk[d][k]) - Gamma.digamma(gammaSum);
			ll += Gamma.logGamma(gamma_dk[d][k]);
			ll -= (gamma_dk[d][k] - 1) * Gamma.digamma(gamma_dk[d][k]) - Gamma.digamma(gammaSum);
			
			for (int n = 0; n < N; ++n) {
				int w = Document.vocabulary.get(doc.getWord(n));
				ll += delta_dnk[d][n][k] * (Gamma.digamma(gamma_dk[d][k]) - Gamma.digamma(gammaSum)) ;
				ll -= delta_dnk[d][n][k] * Math.log(delta_dnk[d][n][k]);
				ll += delta_dnk[d][n][k] * phi_kv[k][w];
			}
		}
		
		return ll;
	}
	
	private static double logAdd(double x, double y) {
		
		if (y <= x) {
			if (y == Double.NEGATIVE_INFINITY) {
				return x;
			}
			return x + Math.log1p(Math.exp(y - x));
		} else {
			if (x == Double.NEGATIVE_INFINITY) {
				return y;
			}
			return y + Math.log1p(Math.exp(x - y));
		} 
		
	}
	
	private static double getPertubation() {
		return Math.random();
	}
		
	
	private static void readParameters(String[] args)
	{
	   
	    Variational.trainingFile = args[0];
	    Variational.testFile = args[1];
	    Variational.outputFile = args[2];
	    Variational.K = Integer.parseInt(args[3]);
	    Variational.lambda = Double.parseDouble(args[4]);
	    Variational.alpha = Double.parseDouble(args[5]);
	    Variational.beta = Double.parseDouble(args[6]);
	    
	}



}
