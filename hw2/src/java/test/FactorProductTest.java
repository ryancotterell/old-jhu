package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import semiring.ProbSemiring;
import bp.Factor;
import bp.Main;

public class FactorProductTest 
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
		
		List<String> cValues = new ArrayList<String>();
		cValues.add("1");
		cValues.add("2");
		
		Main.possibleVariableValues.put("a", aValues);
		Main.possibleVariableValues.put("b", bValues);
		Main.possibleVariableValues.put("c", cValues);
		
		List<String> factor1Variables = new ArrayList<String>();
		factor1Variables.add("a");
		factor1Variables.add("b");

		List<String> factor2Variables = new ArrayList<String>();
		factor2Variables.add("b");
		factor2Variables.add("c");
		
		List<String> factor1Cpd = new ArrayList<String>();
		
		factor1Cpd.add("a=1 b=1 0.5");
		factor1Cpd.add("a=1 b=2 0.8");
		factor1Cpd.add("a=2 b=1 0.1");
		factor1Cpd.add("a=2 b=2 0");
		factor1Cpd.add("a=3 b=1 0.3");
		factor1Cpd.add("a=3 b=2 0.9");
		
		List<String> factor2Cpd = new ArrayList<String>();
		
		factor2Cpd.add("b=1 c=1 0.5");
		factor2Cpd.add("b=1 c=2 0.7");
		factor2Cpd.add("b=2 c=1 0.1");
		factor2Cpd.add("b=2 c=2 0.2");
		
		this.factor1 = new Factor(factor1Variables, factor1Cpd);
		this.factor2 = new Factor(factor2Variables, factor2Cpd);
	}
	
	@Test
	public void test() 
	{
		Factor product = this.factor1.product(this.factor2, Main.sr);
		System.out.println(product);
		assertEquals("[0.25, 0.05, 0.15, 0.08000000000000002, 0.0, 0.09000000000000001, 0.35, 0.06999999999999999, 0.21, 0.16000000000000003, 0.0, 0.1800000000000000]",
				product.toString());
	}
}
