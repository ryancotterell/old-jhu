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

public class MCLDAVariational
{
	
	private static final int E_STEP_ITERATIONS = 20	;
	private static final int MAX_ITERATIONS = 20;

	private static String trainingFile;// = "data/input-train.txt";
	private static String testFile;
	private static String outputFile;

	
	// the number of topics
	
	private static final int L = 0;
	private static final int G = 1;
	// the number of topics
	private static int K = 5;
	
	private static double previousLikelihood;
	
	private static int V;	
	private static int D; // number of documents
	
	private static double alpha = 0.1;
	private static double beta = Math.log(0.01);
	private static double[] lambda = {0.5, 0.5};
	
	private static boolean converged;
	private static int iterations;
	
	static double[][][] zeta_dnc;
	static double[][] gamma_dk ;
	
	static double[][][] delta_dnk;

	static double[][][] phi_local_ckv;
	static double[][] phi_global_kv;
	
	
	
	static List<Document> trainingDocuments;
	
	public static void main(String[] args)
	{
		MCLDAVariational.readParameters(args);
		
		trainingDocuments = DataUtils.loadData(MCLDAVariational.trainingFile);
		

		V = Document.vocabulary.size();
		D = trainingDocuments.size();

		gamma_dk = new double[D][K];
		
		delta_dnk = new double[D][0][0];
		
		zeta_dnc = new double[D][0][0];

		phi_local_ckv = new double[2][K][V];
		phi_global_kv = new double[K][V];
		
		double[][] phi_totals = new double[3][K];
		for (int k = 0; k < K; ++k) {
			for (int v = 0; v < V; ++v) {
				phi_global_kv[k][v] = (1.0 / V)  + getPertubation(); 
				phi_local_ckv[0][k][v] = (1.0 / V) + getPertubation();
				phi_local_ckv[1][k][v] = (1.0 / V) + getPertubation();

				phi_totals[0][k] += phi_global_kv[k][v]; 
				phi_totals[1][k] += phi_local_ckv[0][k][v];
				phi_totals[2][k] += phi_local_ckv[1][k][v];
			}
		}
		//renormalize beta
		//note that beta is in log space
		for (int k = 0; k < K; ++k) {
			for (int v = 0; v < V; ++v) {
				phi_global_kv[k][v] = Math.log(phi_global_kv[k][v] / phi_totals[0][k]); 
				phi_local_ckv[0][k][v] = Math.log(phi_local_ckv[0][k][v] / phi_totals[1][k]); 
				phi_local_ckv[1][k][v] = Math.log(phi_local_ckv[1][k][v] / phi_totals[2][k]); 

				
				
			}
		}
		
		
		//initialize of the variational stuff
		for (int d = 0; d < D; ++d) {
			Document doc = trainingDocuments.get(d);
			int N = doc.size();
			delta_dnk[d] = new double[N][K];
			zeta_dnc[d] = new double[N][2];
			zeta_dnc[d] = new double[N][2];
		
			double[] phi_dnk_t1_total = new double[K];
			for (int n = 0; n < N; ++n) {
				for (int k = 0; k < K; ++k) {
					delta_dnk[d][n][k] = 1.0 / K  + getPertubation();	
					phi_dnk_t1_total[k] += delta_dnk[d][n][k];		
					gamma_dk[d][k] = alpha + ((double) N ) / K + getPertubation();

					//initialize zeta
					zeta_dnc[d][n][0] = .49;
					zeta_dnc[d][n][1] = .51;
					
				}
				for (int k = 0; k < K; ++k) {
					delta_dnk[d][n][k] = delta_dnk[d][n][k] / phi_dnk_t1_total[k];
				}
				
			}
				
		}
		ArrayList<Double> likelihoods = new ArrayList<Double>();
		for (int i = 0; i< MAX_ITERATIONS; ++i) {
			double likelihood = eStep();
			likelihoods.add(likelihood);
			//System.out.println(i + "\t" + likelihood);
			mStep();
			
			if (converged) {
				break;
			}
		}
		//extractTopics();
		writeOutput(likelihoods);
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
			PrintWriter phi0    = new PrintWriter(new FileWriter(filePrefix + "-phi0.txt"));
			PrintWriter phi1    = new PrintWriter(new FileWriter(filePrefix + "-phi1.txt"));
			PrintWriter trainLL = new PrintWriter(new FileWriter(filePrefix + "-trainll.txt"));
			double[][] phiKW = phi_global_kv;
			double[][][] phiCKW = phi_local_ckv;

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
				phi0.printf(word);
				phi1.printf(word);
				for (int k = 0; k < K; ++k) {
					phi.printf(" %.13e", phiKW[k][Document.vocabulary.get(word)]);
					phi0.printf(" %.13e", phiCKW[0][k][Document.vocabulary.get(word)]);
					phi1.printf(" %.13e", phiCKW[1][k][Document.vocabulary.get(word)]);

				}

				if (size != counter) {
                    phi.write("\n");
					phi0.write("\n");
					phi1.write("\n");
				}
				counter += 1;
			}
			phi.close();
			phi0.close();
			phi1.close();
			trainLL.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private static double eStep() {
		double ll = 0.0;
		
		for (int d = 0; d < D; ++d) {
			int numIterations = 0;

			
			Document doc = trainingDocuments.get(d);
			int N = doc.size();
			int c = doc.getCorpus();
			
			boolean converged = false;

			
			while (!converged) {
			
				double[] digammaOfGamma = new double[K];
				
				double digammaOfGammaSum = 0.0;
				for (int k = 0; k < K; ++k) {
					digammaOfGamma[k] = Gamma.digamma(gamma_dk[d][k]);
					digammaOfGammaSum += Gamma.digamma(gamma_dk[d][k]);
				}
				
				++numIterations;
				double[][] oldDelta = new double[N][K];
				for (int n = 0; n < N; ++n) {
					double[] oldZeta = new double[2];
					
					double deltaSum = Double.NEGATIVE_INFINITY;
					int w = Document.vocabulary.get(doc.getWord(n));
					
					
					for (int k = 0; k < K; ++k) {
						oldDelta[n][k] = delta_dnk[d][n][k];
						
						
						delta_dnk[d][n][k] = (phi_global_kv[k][w] * zeta_dnc[d][n][G]
								+ phi_local_ckv[c][k][w] * zeta_dnc[d][n][L]) + digammaOfGamma[k];
						
						
						if (k > 0) {
							deltaSum = logAdd(delta_dnk[d][n][k],deltaSum);
						} else {
							deltaSum = delta_dnk[d][n][k];
						}
					}
					
					
					
					for (int k = 0; k < K; ++k) {
						delta_dnk[d][n][k] = Math.exp(delta_dnk[d][n][k] - deltaSum);
					}
					
					
					assert(sumTest(delta_dnk[d][n]));
					
					
					oldZeta[L] = zeta_dnc[d][n][L];
					oldZeta[G] = zeta_dnc[d][n][G];
					
					double[] tmpZeta = new double[2];
					for (int k = 0; k < K; ++k) {
						tmpZeta[L] += oldDelta[n][k] * phi_local_ckv[c][k][w];
						tmpZeta[G] += oldDelta[n][k] * phi_global_kv[k][w];
					}
					zeta_dnc[d][n][L] = lambda[L] * Math.exp(tmpZeta[L]);
					zeta_dnc[d][n][G] = lambda[G] * Math.exp(tmpZeta[G]);
					
					double zetaNorm = zeta_dnc[d][n][L] + zeta_dnc[d][n][G];
					zeta_dnc[d][n][L] = zeta_dnc[d][n][L] / zetaNorm;
					zeta_dnc[d][n][G] = zeta_dnc[d][n][G] / zetaNorm;
					
					
					assert(sumTest(zeta_dnc[d][n]));
				}
				
				for (int k = 0; k < K; ++k) {
					gamma_dk[d][k] = alpha;
					for (int n = 0; n < N; ++n) {
						gamma_dk[d][k] += oldDelta[n][k];
					}
				}
				
				if (numIterations == E_STEP_ITERATIONS) {
					converged = true;
				}
				
			}
			
			ll += likelihood(d);
			
		}
		

		return ll;
		
	}
	
	private static boolean logSumTest(double[] array) {
		return Math.abs(logSum(array)) < 0.001;
	}
	
	private static boolean sumTest(double[] array) {
		return Math.abs(1 - sum(array)) < 0.001;
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
				phi_global_kv[k][v] = beta;
				phi_local_ckv[0][k][v] = beta;
				phi_local_ckv[1][k][v] = beta;
			}
			
			double normalizationGlobalConstant = Double.NEGATIVE_INFINITY;
			double[] normalizationLocalConstant = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
			
			
			for (int d = 0; d < D; ++d) {
				
				Document doc = trainingDocuments.get(d);
				
				int c = doc.getCorpus();
				int N = doc.size();
				
				for (int n = 0; n < N; ++n) {
					int w = Document.vocabulary.get(doc.getWord(n));
					
					phi_global_kv[k][w] = logAdd(Math.log(delta_dnk[d][n][k] * zeta_dnc[d][n][G]),phi_global_kv[k][w]);
	
					phi_local_ckv[c][k][w] = logAdd(Math.log(delta_dnk[d][n][k] * zeta_dnc[d][n][L]),phi_local_ckv[c][k][w]);
					
					//phi_global_kv[k][w] = logAdd(Math.log(delta_dnk[d][n][k]),phi_global_kv[k][w]);
		
				 //phi_local_ckv[c][k][w] = logAdd(Math.log(delta_dnk[d][n][k]),phi_local_ckv[c][k][w]);


				}
			}
			for (int w = 0; w < V; ++w) {
				normalizationGlobalConstant = logAdd(normalizationGlobalConstant, phi_global_kv[k][w]);
				normalizationLocalConstant[0] = logAdd(normalizationLocalConstant[0], phi_local_ckv[0][k][w]);
				normalizationLocalConstant[1] = logAdd(normalizationLocalConstant[1], phi_local_ckv[1][k][w]);

			}
			
			for (int w = 0; w < V; ++w) {
				phi_global_kv[k][w] = phi_global_kv[k][w] - normalizationGlobalConstant;
				phi_local_ckv[0][k][w] = phi_local_ckv[0][k][w] - normalizationLocalConstant[0];
				phi_local_ckv[1][k][w] = phi_local_ckv[1][k][w] - normalizationLocalConstant[1];

			}
			
			assert(logSumTest(phi_global_kv[k]));
			assert(logSumTest(phi_local_ckv[0][k]));
			assert(logSumTest(phi_local_ckv[1][k]));

			
		}
	}
	


	/**
	 * Computes the lower bound on the log likelihood
	 * @param d
	 * @return
	 */
	private static double likelihood(int d) {
		double[][] phi_kv = phi_global_kv;
		double ll = 0.0;
		double gammaSum = 0.0;
		
		Document doc = trainingDocuments.get(d);
		int N = doc.size();
		int c = doc.getCorpus();
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
				ll += delta_dnk[d][n][k] * phi_local_ckv[c][k][w];
			}
		}
		
		return ll;
	}
	
	
	/**
	 * Extract the topics
	 */
	private static void extractTopics()
	{
		try
		{
			FileWriter writer = new FileWriter("data/variational-output-phi.txt");
			FileWriter writer0 = new FileWriter("data/variational-output-phi0.txt");
			FileWriter writer1 = new FileWriter("data/variational-output-phi1.txt");

			
			int counter = 1;
			int size = Document.vocabulary.keySet().size();
			for (String word : Document.vocabulary.keySet())
			{
				writer.write(word);
				writer0.write(word);
				writer1.write(word);
				for (int k = 0; k < K; k++)
				{
					writer.write(" " + phi_global_kv[k][Document.vocabulary.get(word)]);
					writer0.write(" " + phi_local_ckv[0][k][Document.vocabulary.get(word)]);
					writer1.write(" " + phi_local_ckv[1][k][Document.vocabulary.get(word)]);
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
		if (args.length == 9)
		{
			MCLDAVariational.trainingFile = args[0];
			MCLDAVariational.testFile = args[1];
			MCLDAVariational.outputFile = args[2];
			MCLDAVariational.K = Integer.parseInt(args[3]);
			//MCLDAVariational.lambda = Double.parseDouble(args[4]);
			MCLDAVariational.alpha = Double.parseDouble(args[5]);
			MCLDAVariational.beta = Double.parseDouble(args[6]);
			
		}
	}



}