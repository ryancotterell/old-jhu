package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import semiring.ProbSemiring;
import bp.Factor;
import bp.Main;

public class FactorDivisionTest 
{
	private Factor factor1;
	private Factor factor2;
	
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
		
		Main.possibleVariableValues.put("a", aValues);
		Main.possibleVariableValues.put("b", bValues);
		
		List<String> factor1Variables = new ArrayList<String>();
		factor1Variables.add("a");
		factor1Variables.add("b");

		List<String> factor2Variables = new ArrayList<String>();
		factor2Variables.add("a");
		
		List<String> factor1Cpd = new ArrayList<String>();
		
		factor1Cpd.add("a=1 b=1 0.5");
		factor1Cpd.add("a=1 b=2 0.2");
		factor1Cpd.add("a=2 b=1 0");
		factor1Cpd.add("a=2 b=2 0");
		factor1Cpd.add("a=3 b=1 0.3");
		factor1Cpd.add("a=3 b=2 0.45");

		List<String> factor2Cpd = new ArrayList<String>();
		
		factor2Cpd.add("a=1 0.8");
		factor2Cpd.add("a=2 0");
		factor2Cpd.add("a=3 0.6");
		
		this.factor1 = new Factor(factor1Variables, factor1Cpd);
		this.factor2 = new Factor(factor2Variables, factor2Cpd);
	}
	
	@Test
	public void testDivision() 
	{
		Factor div = factor1.dividedBy(factor2, Main.sr);
		System.out.println(div);
		
		double[] table = div.getTable();
		for (int i = 0; i < table.length; i++)
			table[i] = Main.sr.convertToR(table[i]);
		
		System.out.println(Arrays.toString(table));
		
		assertEquals(div.toString(), "[0.625, 0.0, 0.5, 0.25, 0.0, 0.75]");
	}

}
