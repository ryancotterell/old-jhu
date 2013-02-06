import java.util.ArrayList;
import java.util.List;


public class Node
{
	private String name;	
	private List<Node> parents = new ArrayList<Node>();
	private List<Node> children = new ArrayList<Node>();
	private List<String> values = new ArrayList<String>();
	
	public Node(String name, List<String> values)
	{
		this.name = name;
		this.values = values;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void addChild(Node child)
	{
		this.children.add(child);
	}
	
	public void addParent(Node parent)
	{
		this.parents.add(parent);
	}
	
	public String toString()
	{
		String output = this.name + " {";
		for (Node child : children)
			output += child.getName() + ",";
		output += "}";
		
		return output;
	}
}
