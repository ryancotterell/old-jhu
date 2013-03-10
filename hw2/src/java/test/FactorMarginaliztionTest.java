package test;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import bp.Factor;
import bp.Main;


public class FactorMarginaliztionTest 
{
	private Factor factor;
	
	@Before
	public void setUp()
	{
		Main.possibleVariableValues = new HashMap<String, List<String>>();
		
		List<String> aValues = new ArrayList<String>();
		aValues.add("1");
		aValues.add("2");
		aValues.add("3");
		
		List<String> bValues = new ArrayList<String>();
		bValues.add("1");
		bValues.add("2");
		
		List<String> cValues = new ArrayList<String>();
		cValues.add("1");
		cValues.add("2");
		
		Main.possibleVariableValues.put("a", aValues);
		Main.possibleVariableValues.put("b", bValues);
		Main.possibleVariableValues.put("c", cValues);
		
		List<String> factor1Variables = new ArrayList<String>();
		factor1Variables.add("a");
		factor1Variables.add("b");
		factor1Variables.add("c");

		List<String> factor1Cpd = new ArrayList<String>();
		
		factor1Cpd.add("a=1 b=1,c=1 0.25");
		factor1Cpd.add("a=1 b=1,c=2 0.35");
		factor1Cpd.add("a=1 b=2,c=1 0.08");
		factor1Cpd.add("a=1 b=2,c=2 0.16");
		factor1Cpd.add("a=2 b=1,c=1 0.05");
		factor1Cpd.add("a=2 b=1,c=2 0.07");
		factor1Cpd.add("a=2 b=2,c=1 0");
		factor1Cpd.add("a=2 b=2,c=2 0");
		factor1Cpd.add("a=3 b=1,c=1 0.15");
		factor1Cpd.add("a=3 b=1,c=2 0.21");
		factor1Cpd.add("a=3 b=2,c=1 0.09");
		factor1Cpd.add("a=3 b=2,c=2 0.18");

		
		this.factor = new Factor(factor1Variables, factor1Cpd);
	}
	
	@Test
	public void testMarginalization() 
	{
		List<String> variables = new ArrayList<String>();
		variables.add("b");
		
		System.out.println(factor);
		
		Factor margin = this.factor.marginalizeOverVariables(variables);
		
		double[] table = margin.getTable();
		for (int i = 0; i < table.length; i++)
			table[i] = Main.sr.convertToR(table[i]);
		
		System.out.println("a=1,c=1 : " + margin.query("a=1,c=1"));
		System.out.println("a=1,c=2 : " + margin.query("a=1,c=2"));
		System.out.println("a=2,c=1 : " + margin.query("a=2,c=1"));
		System.out.println("a=2,c=2 : " + margin.query("a=2,c=2"));
		System.out.println("a=3,c=1 : " + margin.query("a=3,c=1"));
		System.out.println("a=3,c=2 : " + margin.query("a=3,c=2"));

		
		System.out.println(Arrays.toString(table));
	}

}
