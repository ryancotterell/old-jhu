# Daniel Deutsch
# Dan Crankshaw
# Ryan Cotterell

import sys;

class Clique:
    
    def __init__(self, variables):
        self.variables = variables.split(",");
        self.neighbors = [];
        
    def __eq__(self, other):
        if self.variables == other.variables:
            return True;
        return False;
        
def main():
    
    # check for command line arguments
    if len(sys.argv) != 5:
        print "Please provide the network, cpd, cliquetree, and query file";
        exit();
    
    # read the clique    
    clique_lines = open(sys.argv[3]).read().splitlines();

    num_cliques = int(clique_lines[0]);
    cliques = [];
    for i in range(1, num_cliques + 1):
        cliques.append(Clique(clique_lines[i]));

    # add the neighbor associations
    for i in range(num_cliques + 1, len(clique_lines)):
        first,second = clique_lines[i].split(" -- ");
        
        index1 = cliques.index(Clique(first));
        index2 = cliques.index(Clique(second));
        cliques[index1].neighbors.append(cliques[index2]);
        cliques[index2].neighbors.append(cliques[index1]);



    print "done";


if __name__ == "__main__":
    main();