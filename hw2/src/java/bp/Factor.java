package bp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import semiring.Semiring;


public class Factor 
{
	private HashMap<String, Integer> strides = new HashMap<String, Integer>();
	
	private List<String> variables = new ArrayList<String>();
	
	private double[] table;
	
	private boolean identity;
	
	/**
	 * Creates a CPD from a List of variables. This constructor should
	 * not be used unless you're going to set the CPT after.
	 * @param variables A List of variables.
	 */
	public Factor(List<String> variables)
	{
		this.variables = variables;
		
		int previousCardinality = 1;
		for (String variable : variables)
		{
			// put the variable's stride
			strides.put(variable, previousCardinality);
			
			// update for the next variable
			previousCardinality *= Main.possibleVariableValues.get(variable).size();;
		}
		
		this.table = new double[previousCardinality];
		
		this.identity = false;
	}
	
	/**
	 * Alternative initialization for identify factor
	 */
	public Factor() {
		this.variables = null;
		this.table = null;
		
		this.identity = true;
	}
	
	/**
	 * Creates a CPD from a List of Strings which specify the values, i.e.,
	 * a section from the CPD input files for one table.
	 * @param variables The list of variables this CPD will use in alphabetical order.
	 * @param cpd The List of Strings.
	 */
	public Factor(List<String> variables, List<String> cpd)
	{
		this(variables);
		
		for (String entry : cpd)
		{
			// break up the line "G=Yes D=Yes,I=No 0.5"
			List<String> vars = new ArrayList<String>();
			
			String[] split = entry.split(" ");
			
			// add "G=Yes"
			vars.add(split[0]);
			
			// add "D=Yes,I=No"
			if (split.length > 2)
			{
				String[] parents = split[1].split(",");
				for (int i = 0; i < parents.length; i++)
					vars.add(parents[i]);
			}
			
			// update the value in the CPT
			int index = this.getIndex(vars);
			
			// does it have parents?
			if (split.length == 3)
				this.table[index] = Main.sr.convertToSemiring(Double.parseDouble(split[2]));
			else
				this.table[index] = Main.sr.convertToSemiring(Double.parseDouble(split[1]));
		}
	}
	
	/**
	 * From a List of Variables in the form "G=Yes", this method will return
	 * the index of the corresponding probability in the table.
	 * @param variables The List of Variables.
	 * @return The index.
	 */
	public int getIndex(List<String> vars)
	{
		int index = 0;
		for (String variable : vars)
		{
			// split the line "G=Yes"
			String[] split = variable.split("=");
			String var = split[0];
			String value = split[1];
			
			int valueIndex = Main.possibleVariableValues.get(var).indexOf(value);
			
			index += valueIndex * this.strides.get(var);
		}
		
		return index;
	}
	
	/**
	 * Finds the union of the set of variables for two CPDs.
	 * @param other The other CPD.
	 * @return A List of the Variables they have in common.
	 */
	private List<String> union(Factor other)
	{
		List<String> union = new ArrayList<String>();
		
		// put all of this.variables into the union
		for (String variable : this.variables)
			union.add(variable);
		
		// put all of other.variables into the union
		for (String variable : other.variables)
		{
			if (!union.contains(variable))
				union.add(variable);
		}
		
		return union;
	}
	
	/**
	 * Computes the factor product between two CPDs taken from
	 * page 359 of the textbook.
	 * @param other The other CPD.
	 * @return The resulting product.
	 */
	public Factor product(Factor other, Semiring sr)
	{
		if (other.identity == true)
			return this;
		
		int j = 0, k = 0;
		
		// generate their union
		List<String> union = this.union(other);
		
		double[] assignment = new double[union.size()];
		
		// set the assignments to 0
		Factor psi = new Factor(union);
		
		for (int i = 0; i < psi.table.length; i++)
		{
//			psi.table[i] = this.table[j] * other.table[k];
			psi.table[i] = sr.times(this.table[j], other.table[k]);
			
			for (int l = 0; l < union.size(); l++)
			{
				assignment[l]++;
				
				String variable = union.get(l);
				int card = Main.possibleVariableValues.get(variable).size();
				
				if (assignment[l] == card)
				{
					assignment[l] = 0;
					j = j - (card - 1) * this.getStride(variable);
					k = k - (card - 1) * other.getStride(variable);
				}
				else
				{
					j = j + this.getStride(variable);
					k = k + other.getStride(variable);
					break;
				}
			}
		}
		
		return psi;
	}
	
	/**
	 * Divides a Factor by another.
	 * @param other The Factor to be the divisor.
	 * @return The result.
	 */
	public Factor dividedBy(Factor other, Semiring sr)
	{
		if (other.identity == true)
			return this;
		
		
		int j = 0, k = 0;
		
		// generate their union
		List<String> union = this.union(other);
		
		double[] assignment = new double[union.size()];
		
		// set the assignments to 0
		Factor psi = new Factor(union);
		
		for (int i = 0; i < psi.table.length; i++)
		{
//			if (other.table[k] == 0)
//				psi.table[i] = 0;
//			else
//				psi.table[i] = this.table[j] / other.table[k];
			
			if (other.table[k] == Main.sr.zero())
				psi.table[i] = Main.sr.zero();
			else
				psi.table[i] = sr.divide(this.table[j], other.table[k]);
			
			for (int l = 0; l < union.size(); l++)
			{
				assignment[l]++;
				
				String variable = union.get(l);
				int card = Main.possibleVariableValues.get(variable).size();
				
				if (assignment[l] == card)
				{
					assignment[l] = 0;
					j = j - (card - 1) * this.getStride(variable);
					k = k - (card - 1) * other.getStride(variable);
				}
				else
				{
					j = j + this.getStride(variable);
					k = k + other.getStride(variable);
					break;
				}
			}
		}
		
		return psi;
	}
	
	/**
	 * This method will marginalize over all of the variables as 
	 * a parameter.
	 * @param variables The variables.
	 * @return A new Factor.
	 */
	public Factor marginalizeOverVariables(List<String> variables)
	{
		// marginalize out the first value
		Factor factor = this.marginalizeOverVariable(variables.get(0));
		
		for (int i = 1; i < variables.size(); i++)
			factor = factor.marginalizeOverVariable(variables.get(i));
		
		return factor;
	}
	
	/**
	 * Marginalizes over exactly one variable and creates a new Factor
	 * to return.
	 * @param variable The variable to marginalize over.
	 * @return The new Factor.
	 */
	public Factor marginalizeOverVariable(String variable)
	{
		int cardinality = Main.possibleVariableValues.get(variable).size();
		
		double[] psi = new double[this.table.length / cardinality];
		
		boolean[] usedIndex = new boolean[this.table.length];
		
		// the location pointers to sum
		int[] pointers = new int[cardinality];
		
		// begin the pointers stride(variable) apart
		int stride = this.strides.get(variable);
		for (int i = 1; i < pointers.length; i++)
			pointers[i] += pointers[i - 1] + stride;
		
		// do the marginalization
		for (int i = 0; i < psi.length; i++)
		{
			double value = Main.sr.zero();
			
			while (usedIndex[pointers[0]] == true)
			{
				for (int j = 0; j < pointers.length; j++)
					pointers[j]++;
			}
			
			// sum over the values of the pointers
			for (int j = 0; j < pointers.length; j++)
			{
				int pointerIndex = pointers[j]++;
				value = Main.sr.plus(value, this.table[pointerIndex]);
				
				// mark this spot as used
				usedIndex[pointerIndex] = true;
			}
			
			psi[i] = value;
		}
		
		List<String> newVariables = new ArrayList<String>();
		for (String var : this.variables)
		{
			if (!var.equals(variable))
				newVariables.add(var);
		}
//		Collections.sort(newVariables);
		
		Factor factor = new Factor(newVariables);
		factor.table = psi;
		
		return factor;
	}
	
	/**
	 * Queries the Factor for a value for a Query in the form "C=Yes,G=No".
	 * @param query The query.
	 * @return The result.
	 */
	public double query(String query)
	{
		String[] split = query.split(",");
		List<String> list = new ArrayList<String>();
		
		for (int i = 0; i < split.length; i++)
			list.add(split[i]);
		
		int index = this.getIndex(list);
		return this.table[index];
	}

	/**
	 * Gets the stride of a variable, 0 if it does not exist.
	 * @param variable The variable.
	 * @return The stride.
	 */
	public int getStride(String variable)
	{
		if (!this.strides.containsKey(variable))
			return 0;
		return this.strides.get(variable);
	}
	
	/**
	 * Generates a String representation of the Factor.
	 */
	public String toString()
	{
		if (this.identity) {
			return "Identity";
		}
		String result = "[";
		
		for (int i = 0; i < this.table.length; i++)
			result += this.table[i] + ", ";
		
		result = result.substring(0, result.length() - 2) + "]";
		return result;
	}
	
	/**
	 * Retrieves the CPT.
	 * @return The CPT.
	 */
	public double[] getTable()
	{
		return this.table;
	}
	
	/**
	 * Getter for identity
	 */
	public boolean getIdentity() {
		return this.identity;
	}
	
	public List<String> getScope()
	{
		return this.variables;
	}
	
	public void setTable(double[] table)
	{
		this.table = table;
	}
	
	public double sumOfTable()
	{
		double sum = Main.sr.zero();
		
		for (int i = 0; i < table.length; i++)
			sum = Main.sr.plus(sum, this.table[i]);
		
		return sum;
	}
}
