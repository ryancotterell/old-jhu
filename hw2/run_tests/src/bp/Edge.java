package bp;

public class Edge
{
	private int to;
	private int from;
	
	public Edge(int from, int to)
	{
		this.to = to;
		this.from = from;
	}
	
	public int getTo()
	{
		return this.to;
	}
	
	public int getFrom()
	{
		return this.from;
	}
	
	public boolean equals(Object edge)
	{
		if (edge instanceof Edge)
		{
			Edge e = (Edge) edge;
			return (this.from == e.from && this.to == e.to); 
		}
		return false;
	}
	
	public int compareTo(Edge edge)
	{
		return 0;
	}
	
	public String toString()
	{
		return this.from + " -> " + this.to;
	}
	
	public int hashCode()
	{
		return this.from + this.to;
	}
}
