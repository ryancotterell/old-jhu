import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Network
{
	private HashMap<String, Node> nodes = new HashMap<String, Node>();
	
	public Network()
	{
		
	}
	
	/**
	 * Adds a node to the network.
	 * @param line A String in the format "FluRate Elevated,NotElevated"
	 */
	public void addNode(String line)
	{
		Scanner scanner = new Scanner(line);
		String name = scanner.next();
		
		StringTokenizer tokenizer = new StringTokenizer(line.substring(line.indexOf(' ') + 1), ",");
		
		ArrayList<String> values = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
			values.add(tokenizer.nextToken());
		
		this.nodes.put(name, new Node(name, values));
	}
	
	/**
	 * Adds a connection between two nodes in the network.
	 * @param line A String in the format "FluRate -> MaryGetsFlu"
	 */
	public void addConnection(String line)
	{
		Scanner scanner = new Scanner(line);
		Node parent = this.nodes.get(scanner.next());
		scanner.next(); // ->
		Node child = this.nodes.get(scanner.next());
		
		parent.addChild(child);
		child.addParent(parent);
	}
	
	public String toString()
	{
		String output = "";
		for (String name : this.nodes.keySet())
			output += this.nodes.get(name).toString() + "\n";
		
		return output;
	}
}
