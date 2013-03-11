package bp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import semiring.LogSemiring;
import semiring.Semiring;


public class Main 
{
	private static String networkFileName = "";
	private static String cpdFileName = "";
	private static String cliqueTreeFileName = "";
	private static String queryFileName = "";
	
	public static HashMap<String, List<String>> possibleVariableValues = new HashMap<String, List<String>>();
	public static HashMap<List<String>, Factor> factors = new HashMap<List<String>, Factor>();
	public static HashMap<List<String>, Clique> cliques = new HashMap<List<String>, Clique>(); 
	
	public static HashMap<String, Set<String>> sepsets = new HashMap<String, Set<String>>();
	
	// Sets to determine which nodes don't have parents
	public static Set<String> leftHandVariables = new TreeSet<String>();
	public static Set<String> rightHandVariables = new TreeSet<String>();
	
	// a mapping between random variables and the Clique Ids that they are in
	public static HashMap<String, List<Integer>> rvToCliques = new HashMap<String, List<Integer>>();
	
	public static Map<Integer, Clique> cliqueIdMapping = new HashMap<Integer, Clique>();
	
	public static List<String> queries = new ArrayList<String>();
	
	// a mapping from a variable to the value we know it is
	public static HashMap<String, String> knownValues = new HashMap<String, String>();
	
	private static Factor[] beta;
	private static Factor[][] mu;
	
//	public static Semiring sr = new ProbSemiring();
	public static Semiring sr = new LogSemiring();
//	public static Semiring sr = new TropicalSemiring();
	
	public static void main(String[] args) throws Exception 
	{
		// check for command line arguments
		if (args.length != 4)
		{
			System.out.println("Please provide the network, cpd, cliquetree, and the query files.");
			System.exit(0);
		}
		else
		{
			Main.networkFileName = args[0];
			Main.cpdFileName = args[1];
			Main.cliqueTreeFileName = args[2];
			Main.queryFileName = args[3];
		}
		
		// read possible variable values
		Main.possibleVariableValues = Main.readPossibleValuesForVariables(Main.networkFileName);
		
		// begin
		Main.initialize();
		
		// read in queries from the file
		Main.queries = Main.readInQueries(Main.queryFileName);
		
//		List<String> list = new ArrayList<String>();
//		list.add("ObserveLandmark1_N_9");
//		list.add("PositionCol_9");
//		list.add("PositionRow_9");
//		
//		System.out.println(Main.cliques.get(list).getId());
//		
//		List<String> list2= new ArrayList<String>();
//		list2.add("Action_9");
//		list2.add("PositionCol_9");
//		list2.add("PositionRow_9");
//		
//		System.out.println(Main.cliques.get(list2).getId());
//		
//		System.out.println(Main.mu[130][138]);
//		
//		System.out.println(Main.sepsets.get(130 + "," + 138));
//		
//		for (int j = 0; j < Main.mu.length; j++)
//		{
//			for (int i = 0; i < j; i++)
//				System.out.println("Mu[" + j + "][" + i + "] = " + Main.mu[i][j]);
//
//		}
		
		
//		Main.queries = new ArrayList<String>();
//		Main.queries.add("PositionRow_8=1,PositionCol_8=1");
//		Main.queries.add("PositionRow_8=1,PositionCol_8=1 ObserveWall_S_7=Yes,PositionRow_7=2,PositionCol_7=1");
		
		// answer the queries
		Main.answerQueries();
		
		
		
		
		

//		for (String variable : Main.possibleVariableValues.keySet())
//			System.out.println(variable + " = " + Main.queryVariables(variable + "=Yes"));
		
//		System.out.println(Main.queryVariables("PositionRow_1=8,PositionCol_1,));
	}
	
	public static void createCliqueMapping()
	{
		for (List<String> variables : Main.cliques.keySet())
		{
			Clique clique = Main.cliques.get(variables);
			Main.cliqueIdMapping.put(clique.getId(), clique);
		}
	}
	
	public static void initialize() throws FileNotFoundException
	{
		// read in the factors from the CPD file
		Main.factors = Main.readInFactors(Main.cpdFileName);
		
		// add in the uniform priors
		Main.addUniformFactors();
		
		// read in the cliques
		Clique.count = 0;
		Main.cliques = Main.readInCliques(Main.cliqueTreeFileName);
		
		// create cliqueId to Clique mapping
		Main.createCliqueMapping();
		
		// assign every factor to a Clique
		Main.assignFactorsToCliques();
		
		// initialize some variables
		Main.beta = new Factor[Main.cliques.keySet().size()];
		Main.mu = new Factor[Main.cliques.keySet().size()][Main.cliques.keySet().size()];
		Main.sepsets = Main.computeAllSepsets();
		
		// initialize the tree 
		Main.initializeCTree();
		
		// clear out any evidence
		Main.knownValues = new HashMap<String, String>();
		
		// run belief propagation to calibrate the tree
		Main.runBp();
	}
	
	/**
	 * Answers queries about the network from the List of queries. The
	 * query should be in the form "G=Yes,J=Yes L=Yes,S=No".
	 */
	public static void answerQueries() throws Exception
	{
		double total = 0;
		
		int i = 0;
		
		for (String query : Main.queries)
		{
			String[] split = query.split(" ");
			
			String unknown = split[0];
			
			if (split.length > 1)
			{
				String evidence = split[1];
				Main.addEvidence(evidence);
			}
			
			double p = Main.queryVariables(unknown);
			total += p;
			i++;
			System.out.printf("%.2f & ", p * 100);
			i = i % 10;
			if (i == 0)
				System.out.println();
			
//			System.out.println(query + ": " + p);
		
		}
		System.out.println(total);
	}
	
	/**
	 * Adds evidence into the tree in the form "G=Yes".
	 * @param evidence The evidence to add in.
	 */
	public static void addEvidence(String evidence) throws Exception
	{
		if (!Main.isEvidenceAdditive(evidence))
			Main.initialize();
		
		boolean newEvidence = false;
		for (String variableValue : evidence.split(",")) 
		{
			String variable = variableValue.split("=")[0];
			String value = variableValue.split("=")[1];
			
			if (!Main.knownValues.containsKey(variable))
			{
				Main.multiplyIn(Main.createZeroOutFactor(variable, value));
				Main.knownValues.put(variable, value);				
				newEvidence = true;
			}
		}
		if (newEvidence)
			runBp();
	}	
	
	/**
	 * Determines whether or not any of the evidence in the format
	 * "L=Yes,S=No" is additive.
	 * @param evidence The evidence to check.
	 * @return True if it is, false otherwise.
	 */
	public static boolean isEvidenceAdditive(String evidence)
	{
		for (String variableValue : evidence.split(","))
		{
			String variable = variableValue.split("=")[0];
			String value = variableValue.split("=")[1];
			
			if (!Main.knownValues.containsKey(variable) || Main.knownValues.get(variable).equals(value))
			{
				// then it is additive, do nothing
			}
			else
			{
				// then it is restrictive
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This will multiply a Factor into a given potential specified
	 * by index i.
	 * @param i The index of the potential.
	 * @param phi The Factor.
	 * @throws Exception 
	 */
	public static void multiplyIn(Factor phi) throws Exception
	{
		int cliqueId = Main.getCliqueIds(phi.getScope()).get(0);
		Main.beta[cliqueId] = Main.beta[cliqueId].product(phi, Main.sr);
	}
	
	/**
	 * Creates the Factor that will add evidence to another CPD. The table
	 * of the Factor will be 1 in the index of the value, 0 everywhere else.
	 * @param variable The variable.
	 * @param value The value that is true.
	 * @return The new Factor.
	 */
	public static Factor createZeroOutFactor(String variable, String value)
	{
		List<String> possibleValues = Main.possibleVariableValues.get(variable);
		int valueIndex = possibleValues.indexOf(value);
		
		List<String> list = new ArrayList<String>();
		list.add(variable);
		
		// create the factor
		Factor factor = new Factor(list);
		
		// put 1 where that corresponds to the value index
		double[] table = new double[possibleValues.size()];
		
		// remember the semirings!
		for (int i = 0; i < table.length; i++)
		{
			if (i == valueIndex)
				table[i] = Main.sr.unit();
			else
				table[i] = Main.sr.zero();
		}
		
		factor.setTable(table);
		return factor;
	}
	
	public static void dfs(Clique root,Set<Integer> seen) {
	    
	    int i = root.getId();
	    System.out.println(i);
	    if (i == 138) {
	        System.out.println("Found 138");
	    }

	    for (Clique n : root.getNeighbors()) {
	        
	        int j = n.getId();    
	        if (!seen.contains(j)) {
	            seen.add(j);
	            dfs(n,seen);
	        }
	    }

	}
	
	/**
	 * Runs belief propagation and calibrates the tree so that 
	 * it is able to answer queries.
	 */
	public static void runBp()
	{
		Stack<Edge> stack = new Stack<Edge>();
		Queue<Edge> queue = new LinkedList<Edge>();
		Clique root = null;
		
		for (List<String> variables : Main.cliques.keySet())
		{
			root = Main.cliques.get(variables);
			break;
		}
		
//		Set<Integer> seen = new HashSet<Integer>();
//		Main.dfs(root, seen);

		Main.generatePropagationOrder(stack, queue, root);
		
		while (!stack.isEmpty())
		{
			Edge e = stack.pop();
			Main.BUMessage(e.getFrom(), e.getTo());
		}
		for (Edge e : queue)
			Main.BUMessage(e.getFrom(), e.getTo());
	}
	
	/**
	 * This method will find a Clique whose scope is a superset of the variables,
	 * otherwise, it should throw an Exception.
	 * @param queryVariables The query variables.
	 * @return The List of Clique Ids.
	 */
	public static List<Integer> getCliqueIds(List<String> queryVariables) throws Exception
	{
		String variable = queryVariables.get(0).split("=")[0];
		List<Integer> cliqueIds = Main.rvToCliques.get(variable);
		
		for (int i = 1; i < queryVariables.size(); i++)
		{
			List<Integer> variableIds = Main.rvToCliques.get(queryVariables.get(i).split("=")[0]);
			
			List<Integer> list = new ArrayList<Integer>();
			for (Integer index : variableIds)
			{
				if (cliqueIds.contains(index))
					list.add(index);
			}
			
			cliqueIds = list;
		}
		
		if (cliqueIds.isEmpty())
			throw new Exception("Could not find a Clique to match: " + queryVariables);
		
		return cliqueIds;
	}
	
	/**
	 * Computes the probability of a query. The query is in the form
	 * "G=Yes,I=Yes", which is the left hand side of a query.
	 * @param query The query.
	 * @return The probability of the query in probability space (not log space).
	 */
	public static double queryVariables(String query)
	{
		String[] split = query.split(",");
		List<String> variablesInQuery = new ArrayList<String>();
		
		// get the variables that are in the query
		for (int i = 0; i < split.length; i++)
			variablesInQuery.add(split[i].split("=")[0]);
		
		List<String> list = Arrays.asList(query.split(","));
		int cliqueId = -1;
		try 
		{
//			cliqueId = Main.getCliqueIds(list).get(0);
			
			List<Integer> cliqueIds = Main.getCliqueIds(list);
			Random rand = new Random();
			cliqueId = cliqueIds.get(rand.nextInt(cliqueIds.size()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<String> variablesInClique = Main.beta[cliqueId].getScope();
		
		// get the variables which are in the Clique but not in the query
		List<String> variablesToMarginalize = new ArrayList<String>();
		for (String variable : variablesInClique)
		{
			if (!variablesInQuery.contains(variable))
				variablesToMarginalize.add(variable);
		}
		
		Factor marginal = Main.beta[cliqueId].marginalizeOverVariables(variablesToMarginalize);
		
		double p = marginal.query(query);
		return Main.sr.convertToR(Main.sr.divide(p, marginal.sumOfTable()));
	}
	
	
	/**
	 * Initialize-Clique Tree.
	 */
	public static void initializeCTree() {
		
		//Initialize the beta
		for (List<String> variables : Main.cliques.keySet()) {
			Clique c = Main.cliques.get(variables);
			int id = c.getId();
			
			if (c.getFactors().size() == 0)
				beta[id] = new Factor();
			
			boolean first = true;
			for (Factor phi : c.getFactors()) {
				if (first == true) {
					beta[id] = phi;
					first = false;
				} else {
					beta[id] = beta[id].product(phi, Main.sr);
				}
			}
		}
		
		//Initialize edge
		for (int j = 0; j < Main.cliques.keySet().size(); j++) {
			for (int i = 0;  i < j; i++) {
				Main.mu[i][j] = new Factor();
			}
		}
	}
	/**
	 * 
	 * Reads in the possible values for each variable from the network file.
	 * @param fileName The network file name.
	 */
	private static HashMap<String, List<String>> readPossibleValuesForVariables(String fileName) throws FileNotFoundException
	{
		Scanner scanner = new Scanner(new FileReader(fileName));
		
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		int numberOfVariables = Integer.parseInt(scanner.nextLine());
		
		for (int i = 0; i < numberOfVariables; i++)
		{
			// break up the line "C Yes,No"
			String[] line = scanner.nextLine().split(" ");
			String variable = line[0];
			
			String[] values = line[1].split(",");
			
			// put the values into an ArrayList
			List<String> list = new ArrayList<String>();
			for (int j = 0; j < values.length; j++)
				list.add(values[j]);
			
			// add the mapping
			map.put(variable, list);
		}
		
		return map;
	}
	
	/**
	 * Reads in the conditional probability distributions.
	 * @param fileName The file name of the CPD.
	 */
	private static HashMap<List<String>, Factor> readInFactors(String fileName) throws FileNotFoundException
	{
		Scanner scanner = new Scanner(new FileReader(fileName));
		
		// mapping from variables to their Factors
		HashMap<List<String>, List<String>> map = new HashMap<List<String>, List<String>>();
		
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			List<String> variables = new ArrayList<String>();
			
			// parse the variables from "G=Yes D=Yes,I=No 0.5"
			String[] split = line.split(" ");
			
			// adds the first variable
			variables.add(split[0].split("=")[0]);
			Main.leftHandVariables.add(split[0].split("=")[0]);

			// requires parents
			if (split.length > 2)
			{
				// adds the parents, "D=Yes,I=No"
				String[] parents = split[1].split(",");
				for (int i = 0; i < parents.length; i++)
				{
					variables.add(parents[i].split("=")[0]);
					Main.rightHandVariables.add(parents[i].split("=")[0]);
				}
			}
			else
				System.out.println("here");
			
			// put in alphabetical order
			Collections.sort(variables);
			
			// put into the hash map
			if (map.containsKey(variables))
				map.get(variables).add(line);
			else
			{
				List<String> list = new ArrayList<String>();
				list.add(line);
				
				map.put(variables, list);
			}
		}

		// create the Factors
		HashMap<List<String>, Factor> factors = new HashMap<List<String>, Factor>();
		for (List<String> variables : map.keySet())
			factors.put(variables, new Factor(variables, map.get(variables)));
		
		return factors;
	}
	
	/**
	 * Reads in the List of Cliques from the input file.
	 * @param fileName The Clique file name.
	 * @return The List of Cliques.
	 */
	public static HashMap<List<String>, Clique> readInCliques(String fileName) throws FileNotFoundException
	{
		HashMap<List<String>, Clique> cliques = new HashMap<List<String>, Clique>();
		
		Scanner scanner = new Scanner(new FileReader(fileName));
		int numberOfCliques = Integer.parseInt(scanner.nextLine()); 
		
		// read in the List of Cliques
		for (int i = 0; i < numberOfCliques; i++)
		{
			String[] split = scanner.nextLine().split(",");
			
			List<String> variables = new ArrayList<String>();
			for (int index = 0; index < split.length; index++)
				variables.add(split[index]);
			
			Collections.sort(variables);
			
			cliques.put(variables, new Clique(variables));
		}
		
		// read in the connections
		while (scanner.hasNextLine())
		{
			String[] split = scanner.nextLine().split(" -- ");
			
			List<String> firstVariables = new ArrayList<String>();
			List<String> secondVariables = new ArrayList<String>();
			
			// break up the first Clique's variables
			String[] vars1 = split[0].split(",");
			for (int i = 0; i < vars1.length; i++)
				firstVariables.add(vars1[i]);
			
			// break up the second Clique's variables
			String[] vars2 = split[1].split(",");
			for (int i = 0; i < vars2.length; i++)
				secondVariables.add(vars2[i]);
			
			// sort them to maintain the correct order
			Collections.sort(firstVariables);
			Collections.sort(secondVariables);
			
			// retrieve the corresponding Cliques 
			Clique clique1 = cliques.get(firstVariables);
			Clique clique2 = cliques.get(secondVariables);
			
			// add the edge
			clique1.addNeighbor(clique2);
			clique2.addNeighbor(clique1);
		}
		
		return cliques;
	}
	
	public static void assignFactorsToCliques()
	{
		for (List<String> factorVariables : Main.factors.keySet())
		{
			Factor factor = Main.factors.get(factorVariables);
			int i = 0;
			boolean assigned = false;
			
			while (!assigned)
			{
				for (List<String> cliqueVariables : Main.cliques.keySet())
				{
					Clique clique = Main.cliques.get(cliqueVariables);
					
					if (Main.isSubset(factorVariables, cliqueVariables) && clique.getFactors().size() == i)
					{
						clique.assignFactor(factor);
						assigned = true;
						break;
					}
				}
				
				i++;
			}
		}
		
		
		
		
		
		// iterate over cliques and find a factor that can go there. 
		// then with the leftover factors, assign them to cliques, that way
		// we know that every clique has at least 1 factor.
		
//		for (List<String> cliqueVariables : Main.cliques.keySet())
//		{
//			for (List<String> factorVariables : Main.factors.keySet())
//			{
//				if (Main.isSubset(factorVariables, cliqueVariables))
//				{
//					Clique clique = Main.cliques.get(cliqueVariables);
//					Factor factor = Main.factors.get(factorVariables);
//					
//					clique.assignFactor(factor);
//					Main.factors.remove(factor);
//					break;
//				}
//			}
//		}
		
		// at this point, each Clique "should" have a factor assigned to it
		// now we should assign the rest of the factors to any Clique
//		for (List<String> factorVariables : Main.factors.keySet())
//		{
//			for (List<String> cliqueVariables : Main.cliques.keySet())
//			{
//				if (Main.isSubset(factorVariables, cliqueVariables))
//				{
//					Clique clique = Main.cliques.get(cliqueVariables);
//					Factor factor = Main.factors.get(factorVariables);
//					
//					clique.assignFactor(factor);
////					Main.cpds.remove(cpd);
//					break;
//				}
//			}
//		}
	}
	
	/**
	 * Determines if the first List is a subset of the second List.
	 * @param list1 The first List.
	 * @param list2 The second List.
	 * @return True if it is, false otherwise.
	 */
	public static boolean isSubset(List<String> list1, List<String> list2)
	{
		for (String variable : list1)
		{
			if (!list2.contains(variable))
				return false;
		}
		return true;
	}
	
	/**
	 * Generating the order of propagation for the message passing.
	 * @param stack The first pass.
	 * @param queue The second pass.
	 * @param root The root of the tree.
	 */
	private static void generatePropagationOrder(Stack<Edge> stack, Queue<Edge> queue, Clique root)
	{
		int i = root.getId();
		
//		System.out.println("root = " + root.getVariables());
		
		for (Clique neighbor : root.getNeighbors())
		{
			int j = neighbor.getId();
			
			Edge e1 = new Edge(j, i);
			Edge e2 = new Edge(i, j);
			
			if (!queue.contains(e2) && !queue.contains(e1))
			{
				stack.push(e1);
				queue.add(e2);
				
				generatePropagationOrder(stack, queue, neighbor);
			}
		}
	}
	
	private static HashMap<String, Set<String>> computeAllSepsets()
	{
		HashMap<String, Set<String>> set = new HashMap<String, Set<String>>();
		
		for (List<String> variables1 : Main.cliques.keySet())
		{
			for (List<String> variables2 : Main.cliques.keySet())
			{
				Clique clique1 = Main.cliques.get(variables1);
				Clique clique2 = Main.cliques.get(variables2);
				
				int i = clique1.getId();
				int j = clique2.getId();
				
				Set<String> sep = clique1.sepset(clique2);
				set.put(i + "," + j, sep);
			}
		}
		
		return set;
	}
	
	
	private static void BUMessage(int i, int j)
	{
		Factor sigma_ij = Main.beta[i];
		
		for (String variable : Main.beta[i].getScope())
		{
			Set<String> sepset = Main.sepsets.get(i + "," + j);
			if (!sepset.contains(variable))
			{
				sigma_ij = sigma_ij.marginalizeOverVariable(variable);
			}
		}
		
		int mu_i = Math.min(i, j);
		int mu_j = Math.max(i, j);
		
		Main.beta[j] = beta[j].product(sigma_ij.dividedBy(Main.mu[mu_i][mu_j], Main.sr), Main.sr);
		Main.mu[mu_i][mu_j] = sigma_ij;
	}
	
	public static void addUniformFactors()
	{
		List<String> variablesToAdd = new ArrayList<String>();
		
		for (String variable : Main.rightHandVariables)
		{
			if (!Main.leftHandVariables.contains(variable))
				variablesToAdd.add(variable);
		}
		
		for (String variable : variablesToAdd)
		{
			List<String> list = new ArrayList<String>();
			list.add(variable);
			
			Factor factor = new Factor(list);
			double[] table = new double[Main.possibleVariableValues.get(variable).size()];
			for (int i = 0; i < table.length; i++)
				table[i] = 1.0 / table.length;
			
			factor.setTable(table);
			
			Main.factors.put(list, factor);
		}
	}
	
	public static List<String> readInQueries(String fileName) throws FileNotFoundException
	{
		List<String> list = new ArrayList<String>();
		Scanner scanner = new Scanner(new FileReader(fileName));
		
		while (scanner.hasNextLine())
			list.add(scanner.nextLine());
		
		return list;
	}
}
