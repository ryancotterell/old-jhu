package bp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class Clique 
{
	public static int count = 0;
	
	private List<String> variables = new ArrayList<String>();
	private List<Clique> neighbors = new ArrayList<Clique>();
	
	private List<Factor> factors = new ArrayList<Factor>();
	
	private double[] phi;
	
	private int id;
	
	/**
	 * Creates a Clique based on an alphabetical List of variables.
	 * @param variables The variables.
	 */
	public Clique(List<String> variables)
	{
		this.id = Clique.count++;
		this.variables = variables;
		
		// add mapping from variables to the clique Id that use them
		for (String variable : variables)
		{
			if (Main.rvToCliques.containsKey(variable))
				Main.rvToCliques.get(variable).add(this.id);
			else
			{
				List<Integer> list = new ArrayList<Integer>();
				list.add(this.id);
				
				Main.rvToCliques.put(variable, list);
			}
		}
	}
	
	/**
	 * Retrieves the unique Id of the Clique.
	 * @return The Id.
	 */
	public int getId()
	{
		return this.id;
	}
	
	/**
	 * Adds a connection between two Cliques.
	 * @param clique The other Clique.
	 */
	public void addNeighbor(Clique clique)
	{
		this.neighbors.add(clique);
	}
	
	public List<String> getVariables()
	{
		return this.variables;
	}
	
	/**
	 * Adds a factor to this Clique.
	 * @param cpd The factor.
	 */
	public void assignFactor(Factor cpd)
	{
		this.factors.add(cpd);
	}
	
	/**
	 * Retrieves the List of factors assigned to this Clique.
	 * @return The factors.
	 */
	public List<Factor> getFactors()
	{
		return this.factors;
	}
	
	/**
	 * Retrieves the neighbors of the Clique.
	 * @return The List of neighbors.
	 */
	public List<Clique> getNeighbors()
	{
		return this.neighbors;
	}

	/**
	 * Computes the sepset between two Cliques.
	 * @param other The other Clique.
	 * @return The sepset.
	 */
	public Set<String> sepset(Clique other)
	{
		Set<String> sepset = new TreeSet<String>();
		
		for (String variable : this.variables)
		{
			if (other.variables.contains(variable))
				sepset.add(variable);
		}
		
		return sepset;
	}
}
