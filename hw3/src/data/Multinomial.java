package data;

import java.util.Random;

public class Multinomial
{
	private Random random = new Random();
	private double[] distribution;
	
	public Multinomial(double[] distribution)
	{
		double sum = 0;
		for (int i = 0; i < distribution.length; i++)
			sum += distribution[i];
		
		for (int i = 0; i < distribution.length; i++)
			distribution[i] = distribution[i] / sum;
		
		this.distribution = distribution;
	}
	
	public int sample()
	{
		double random = this.random.nextDouble();
		
		double sum = 0;
		int index = 0;
		while (true)
		{
			sum += this.distribution[index];
			if (sum > random)
				return index;
			
			index++;
		}
	}
}
