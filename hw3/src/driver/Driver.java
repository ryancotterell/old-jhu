package driver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import data.Document;
import data.DocumentCollection;

public class Driver {
	
	private static String trainingFile = "";
	private static String testFile = "";
	private static String outputFile = "";
	private static int numTopics;
	private static double lambda;
	private static double alpha;
	private static double beta;
	private static int iterations;
	private static int burnIn;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		parseCommandLineArgs(args);
		DocumentCollection trainingDocs = loadData(trainingFile);
		DocumentCollection testDocs = loadData(testFile);
		Sampler sampler = new CollapsedSampler(lambda, alpha, beta, numTopics, trainingDocs, testDocs);
		
		for (int i = 0; i < burnIn; ++i) {
			sampler.runIteration(true);
			double trainLikelihood = sampler.computeLikelihood(true);
			double testLikelihood = sampler.computeLikelihood(false);
		}
		
		double[] trainLLs = new double[iterations - burnIn];
		double[] testLLs = new double[iterations - burnIn];
		for (int i = 0; i < iterations - burnIn; ++i) {
			sampler.runIteration(false);
			trainLLs[i] = sampler.computeLikelihood(true);
			testLLs[i] = sampler.computeLikelihood(false);
		}
		
		writeOutput(outputFile, trainLLs, testLLs, sampler);
		
		
		// load data
		// create sampler
		// run iteration
		// compute likelihoods
		// optional normalize likelihoods

	}
	
	private static void parseCommandLineArgs(String[] args) {
		if (args.length == 9)
		{
			trainingFile = args[0];
			testFile = args[1];
			outputFile = args[2];
			numTopics = Integer.parseInt(args[3]);
			lambda = Double.parseDouble(args[4]);
			alpha = Double.parseDouble(args[5]);
			beta = Double.parseDouble(args[6]);
			iterations = Integer.parseInt(args[7]);
			burnIn = Integer.parseInt(args[8]);
		}
	}
	
	private static DocumentCollection loadData(String filename) {
		DocumentCollection documents = new DocumentCollection();
		try {
			Scanner scanner = new Scanner(new FileReader(filename));
			while (scanner.hasNextLine())
				documents.addDocument(new Document(scanner.nextLine()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return documents;
	}
	
	/**
	 * Writes out mean parameter values
	 * @param filePrefix
	 */
	// TODO get 13 digits in output
	private static void writeOutput(String filePrefix, double[] trainlls, double[] testlls, Sampler s) {
		try {
			FileWriter phi = new FileWriter(filePrefix + "-phi.txt");
			FileWriter phi0 = new FileWriter(filePrefix + "-phi0.txt");
			FileWriter phi1 = new FileWriter(filePrefix + "-phi1.txt");
			FileWriter theta = new FileWriter(filePrefix + "-theta.txt");
			FileWriter trainLL = new FileWriter(filePrefix + "-trainll.txt");
			FileWriter testLL = new FileWriter(filePrefix + "-testll.txt");
			double[][] testThetas = s.getAverageTestThetas();
			double[][] trainThetas = s.getAverageTrainThetas();
			double[][] phiKW = s.getAveragePhi_kw();
			double[][][] phiCKW = s.getAveragePhi_ckw();
			
			// write likelihoods
			for (int i = 0; i < trainlls.length; ++i) {
				trainLL.write(trainlls[i] + "\n");
				testLL.write(testlls[i] + "\n");
			}
			
			for (String word : Document.vocabulary.keySet()) {
				phi.write(word);
				phi0.write(word);
				phi1.write(word);
				for (int k = 0; k < numTopics; ++k) {
					
				}
			}
			
			int vocabSize = Document.vocabulary.size();
			
			//write thetas
			for (int d = 0; d < trainThetas.length; ++d) {
				theta.write("" + trainThetas[d][0]);
				for (int k = 1; k < trainThetas[0].length; ++k) {
					theta.write(" " + trainThetas[d][k]);
				}
			}
			
			//write phis
			int counter = 1;
			int size = Document.vocabulary.keySet().size();
			for (String word : Document.vocabulary.keySet())
			{
				phi.write(word);
				phi0.write(word);
				phi1.write(word);
				for (int k = 0; k < numTopics; ++k)
				{
					phi.write(" " + phiKW[k][Document.vocabulary.get(word)]);
					phi0.write(" " + phiCKW[0][k][Document.vocabulary.get(word)]);
					phi1.write(" " + phiCKW[1][k][Document.vocabulary.get(word)]);

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
			theta.close();
			trainLL.close();
			testLL.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}