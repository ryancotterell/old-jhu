import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class BayesQuery 
{
	public static void main(String[] args) throws IOException
	{
		Network network = BayesQuery.readNetwork(args[0]);
		System.out.println(network);
	}
	
	private static Network readNetwork(String filename) throws IOException
	{
		Network network = new Network();
		Scanner scanner = new Scanner(new FileReader(filename));
		
		// add Nodes and possible values
		int numValues = Integer.parseInt(scanner.nextLine());
		for (int i = 0; i < numValues; i++)
			network.addNode(scanner.nextLine());
		
		// add connections between Nodes
		while (scanner.hasNext())
			network.addConnection(scanner.nextLine());
		
		return network;
	}
}
