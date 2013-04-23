package data;

import java.util.Random;

public class Multinomial
{
	private Random random = new Random();
	private double[] distribution;
	
	public Multinomial(double[] dist)
	{
		double sum = 0;
		for (int i = 0; i < dist.length; i++)
			sum += dist[i];
		
		for (int i = 0; i < dist.length; i++)
			dist[i] = dist[i] / sum;
		
		this.distribution = dist;
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
