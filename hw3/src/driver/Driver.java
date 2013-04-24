package driver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.*;

import data.Document;
import data.DocumentCollection;

public class Driver {

	private static String trainingFile;
	private static String testFile;
	private static String outputFile;
	private static int numTopics;
	private static double lambda;
	private static double alpha;
	private static double beta;
	private static int iterations;
	private static int burnIn;
	private static boolean collapsedSampler;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		parseCommandLineArgs(args);
		DocumentCollection trainingDocs = loadData(trainingFile);
		DocumentCollection testDocs = loadData(testFile);
		Sampler sampler;
		if (collapsedSampler) {
			sampler = new CollapsedSampler(lambda, alpha, beta, numTopics,
					trainingDocs, testDocs);
		} else {
			sampler = new BlockedSampler(lambda, alpha, beta, numTopics,
					trainingDocs, testDocs);
		}

        
		for (int i = 0; i < burnIn; ++i) {
			sampler.runIteration(true);
		}

		double[] trainLLs = new double[iterations - burnIn];
		double[] testLLs = new double[iterations - burnIn];
		for (int i = 0; i < iterations - burnIn; ++i) {
			sampler.runIteration(false);
			double trainLikelihood = sampler.computeLikelihood(true);
			double testLikelihood = sampler.computeLikelihood(false);
			trainLLs[i] = trainLikelihood;
			testLLs[i] = testLikelihood;
		}

		writeOutput(outputFile, trainLLs, testLLs, sampler);

	}

	private static void parseCommandLineArgs(String[] args) {
		if (args.length == 10) {
			collapsedSampler = Boolean.parseBoolean(args[0]);
			trainingFile = args[1];
			testFile = args[2];
			outputFile = args[3];
			numTopics = Integer.parseInt(args[4]);
			lambda = Double.parseDouble(args[5]);
			alpha = Double.parseDouble(args[6]);
			beta = Double.parseDouble(args[7]);
			iterations = Integer.parseInt(args[8]);
			burnIn = Integer.parseInt(args[9]);
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
	 * 
	 * @param filePrefix
	 */
	private static void writeOutput(String filePrefix, double[] trainlls,
			double[] testlls, Sampler s) {
		filePrefix += "-" + numTopics + "-" + lambda + "-" + alpha;
		try {
			
			PrintWriter phi     = new PrintWriter(new FileWriter(filePrefix + "-phi.txt"));
			PrintWriter phi0    = new PrintWriter(new FileWriter(filePrefix + "-phi0.txt"));
			PrintWriter phi1    = new PrintWriter(new FileWriter(filePrefix + "-phi1.txt"));
			PrintWriter theta   = new PrintWriter(new FileWriter(filePrefix + "-theta.txt"));
			PrintWriter trainLL = new PrintWriter(new FileWriter(filePrefix + "-trainll.txt"));
			PrintWriter testLL  = new PrintWriter(new FileWriter(filePrefix + "-testll.txt"));
			double[][] testThetas = s.getAverageTestThetas();
			double[][] trainThetas = s.getAverageTrainThetas();
			double[][] phiKW = s.getAveragePhi_kw();
			double[][][] phiCKW = s.getAveragePhi_ckw();

			// write likelihoods
			for (int i = 0; i < trainlls.length; ++i) {
				trainLL.printf("%.13e\n", trainlls[i]);
				testLL.printf("%.13e\n", testlls[i]);
			}

			int vocabSize = Document.vocabulary.size();

			// write thetas
			for (int d = 0; d < trainThetas.length; ++d) {
				theta.printf("%.13e", trainThetas[d][0]);
				for (int k = 1; k < trainThetas[0].length; ++k) {
					theta.printf(" %.13e", trainThetas[d][k]);
				}
			}

			// write phis
			int counter = 1;
			int size = Document.vocabulary.keySet().size();
			for (String word : Document.vocabulary.keySet()) {
				phi.printf(word);
				phi0.printf(word);
				phi1.printf(word);
				for (int k = 0; k < numTopics; ++k) {
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
			theta.close();
			trainLL.close();
			testLL.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
