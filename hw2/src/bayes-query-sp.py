#!/usr/bin/python

# Daniel Deutsch
# Dan Crankshaw
# Ryan Cotterell

import sys;
from itertools import product

class Clique:
    
    def __init__(self, variables):
        self.variables = variables.split(",");
        self.neighbors = [];
        self.sep_sets = []
        self.factors = []
        

    def create_sep_sets(self):
        for n in self.neighbors:
            sep_set = []
            for v in n.variables:
                if v in self.variables:
                    sep_set.append(v)

            sep_sets.append(sep_set)
        
    def __eq__(self, other):
        if self.variables == other.variables:
            return True;
        return False;
    def __str__(self):
        return str(self.variables) + " - " + str(self.neighbors)


def key_maker(var,val,vars,vals):
    vs = sorted(zip(vars,vals),key=lambda x: x[0])
    return var + "=" + val + "|" + ",".join([v[0] + "=" + v[1] for v in vs])


#DATA STRUCTURE FOR A FACTOR
class factor:
        #must be sorted in alphabetical order
    def __init__(self,scope):
        self.cpd = {}
        self.scope = sorted(scope)
        
    def add(self,assignments,prob):
        """
        adds a new probability based on assignments
        """
        key = ",".join([ "%s=%s" % (x[0],x[1]) for x in zip(self.scope,assignments)])
        self.cpd[key] = prob
        
    def p(self,vals):
        """
        gets the probability for some value
        must be in order 
        """
        return self.cpd[",".join(map(lambda x: x[0] + '=' + x[1],zip(self.scope,vals)))]
    
    def p_hash(self,hash):
        """
        gets the probability for values in a hash
        this means we can have out of order values etc..
        """
        key = ""
        for var in self.scope:
            key += ("%s=%s," % (var,hash[var]))
            
            
        return self.cpd[key[:-1]]

    def __str__(self):
        return str(self.scope)
    
    def __repr__(self):
        return self.__str__()
    #the algoritm


def read_network(file_name):
    """
    Reads in an Network file
    """
    f = open(file_name,'r')
    lines = f.read().splitlines()
    f.close()
   
    num_vars = int(lines.pop(0))

    rv_to_vals = {}
   

    for i in range(0,num_vars):
        tmp = lines.pop(0)
        rv,val = tmp.split(" ")
        vals = val.split(",")
        rv_to_vals[rv] = vals

    return rv_to_vals
    

def read_cpd(file_name,rv_vars):
    """
    Returns initial factors
    """
    # read the clique
    f = open(file_name)
    cpd_lines = f.read().splitlines();
    f.close()

    #to enusre all rvs have factors
    rv_seen = set([])

    cur_factor = None
    cur_var = None

    factors = []

    for l in cpd_lines:

        left,right,prob = l.split(" ")
        var,val = left.split("=")
        vars = [ v.split("=")[0] for v in right.split(",") ]
        vals = [ v.split("=")[1] for v in right.split(",") ]

        if cur_var != var:
            cur_factor = factor(sorted([var] + vars))
            factors.append(cur_factor)
            cur_var = var
            rv_seen.add(var)
            

        #makes a key 
        key = ",".join([ "%s=%s" % (x[0],x[1]) for x in sorted(zip([var] + vars, [val] + vals),key=lambda x: x[0])])
        cur_factor.cpd[key] = float(prob)

    for rv in rv_vars:
        if rv not in rv_seen:
            tmp_factor = factor([rv])
            for val in rv_vars[rv]:
                tmp_factor.cpd["%s=%s" % (rv,val)] = 1/float(len(rv_vars[rv]))

            factors.append(tmp_factor)

    return factors

        
def main():

    cpd = {}
    
    # check for command line arguments
    if len(sys.argv) != 5:
        print "Please provide the network, cpd, cliquetree, and query file";
        exit();
    


    rv_to_vals = read_network(sys.argv[1])
    factors = read_cpd(sys.argv[2],rv_to_vals)
   

    
    clique_lines = open(sys.argv[3]).read().splitlines()

    num_cliques = int(clique_lines.pop(0));
    cliques = {};
    clique_to_id = {}
    
    for i in range(0, num_cliques):
        cliques[str(i)] = Clique(clique_lines[i]);
        clique_to_id[clique_lines[i]] = i

        

    # add the neighbor associations
    for i in range(num_cliques, len(clique_lines)):
        first,second = clique_lines[i].split(" -- ");
        cliques[str(clique_to_id[first])].neighbors.append(clique_to_id[second])
        cliques[str(clique_to_id[second])].neighbors.append(clique_to_id[first])
        



    #DATA

    #need to generalized
    def mult(phi1,phi2):
        scope_union = sorted(phi1.scope + [x for x in phi2.scope if x not in phi1.scope])
        psi = factor(scope_union)
        
        for assignment in product(*map(lambda z: ['Yes','No'],scope_union)):
            zipped = dict(zip(scope_union,assignment))
        
            psi.add(assignment,phi1.p_hash(zipped)*phi2.p_hash(zipped))

        
        return psi

    
    def divide(phi1,phi2):
        scope_union = sorted(phi1.scope + [x for x in phi2.scope if x not in phi1.scope])
        psi = factor(scope_union)
        
        for assignment in product(*map(lambda z: ['Yes','No'],scope_union)):
            zipped = dict(zip(scope_union,assignment))
        
            if phi1.p_hash(zipped) == 0.0:
                psi.add(assignment,0.0)
            else:
                psi.add(assignment,phi1.p_hash(zipped)/phi2.p_hash(zipped))

        return psi

    def sum_out(phi1,var):
        cpd = {}
        
        for assignment in product(*map(lambda z: ['Yes','No'],phi1.scope)):
            key = ",".join([ "%s=%s" % (x[0],x[1]) for x in zip(phi1.scope,assignment)])
            key_new = ",".join(["%s=%s" % (x[0],x[1]) for x in zip(phi1.scope,assignment) if x[0] != var])
            if key_new not in cpd:
                cpd[key_new] = 0.0
            cpd[key_new] += phi1.cpd[key]
        

        psi = factor([x for x in phi1.scope if x != var])
        psi.cpd = cpd
        return psi
    


    def all_in_clique(clique,factor):
        for var in factor.scope:
            if var not in clique.variables:
                return False
        return True

    for f in factors:
        #print "Factor %s" % str(f)
        for i in range(0,num_cliques):
            clique = cliques[str(i)]
            if all_in_clique(clique,f):
                clique.factors.append(f)
 
                break


    #init c_tree
    beta = {}
    for i in range(0,num_cliques):
        clique = cliques[str(i)]
        psi = clique.factors[0]
        for j,f in enumerate(clique.factors):
            if j == 0:
                continue
            psi = mult(psi,f)

        beta[str(i)] = psi

       
    mu = {}
    sep_sets = {}
    for i in range(0,num_cliques):
        key = str(i)
        clique = cliques[key]
        if key not in mu:
            mu[key] = {}
            sep_sets[key] = {}
        for n in clique.neighbors:
            mu[key][str(n)] = 1.0
            sep_sets[key][str(n)] = list(set(clique.variables) - set(cliques[str(n)].variables))
            
    def bu_update(i,j):
        mu_i = str(i)
        mu_j = str(j)
        if j < i:
            mu_i = str(j)
            mu_j = str(i)

        i = str(i)
        j = str(j)
        sigma = beta[i]
        for var in sep_sets[i][j]:
            sigma = sum_out(sigma,var)

        
        tmp = sigma
        if mu[mu_i][mu_j] != 1:
            tmp = divide(sigma,mu[mu_i][mu_j])
        beta[j] = mult(beta[j],tmp)

        mu[mu_i][mu_j] = sigma


    def test1(one):
        for i in range(0,num_cliques):
            for j in range(0,num_cliques):
                i = str(i)
                j = str(j)
                if i in mu and j in mu[i]:
                    valid_order = True
                    for n in mu[i]:
                        if n != j and mu[n][i] == 1:
                            valid_order = False

                    if valid_order and mu[i][j] == 1:
                       
                        print mu
                        #if mu[j][i] != 1.0:
                        #    return
                        if one:
                            return one
                        bu_update(i,j)
                      
                        print "%s -> %s" % (i,j)
                        
                        if mu[j][i] != 1.0:
                     
                            return True
                                                    
                       

    def test2():
        for i in range(0,num_cliques):
            for j in range(0,num_cliques):
                i = str(i)
                j = str(j)
                

                if i in mu and j in mu[i]:
                    
                    bu_update(i,j)


    i = 3
    j = 2
    beta_i = str(i)
    beta_j = str(j)
    mu_i = str(i)
    mu_j = str(j)

    print "BETA %s Orig" % beta_i
    for var in beta[beta_i].cpd:
        print "%s : %s" % (var,beta[beta_i].cpd[var])


    print "BETA %s Orig Sum Out D" % beta_i
    tmp = sum_out(beta[beta_i],'D')
    for var in tmp.cpd:
        print "%s : %s" % (var,tmp.cpd[var])

    print
    print "BETA %s Orig" % beta_j
    for var in beta[beta_j].cpd:
        print "%s : %s" % (var,beta[beta_j].cpd[var])


    one = False
    for i in range(0,5):
        test2()
        #one = test1(one)

  


    result = sum_out(beta['4'],'S')
    result = sum_out(result,'L')
    norm =  result.p(['Yes']) + result.p(['No'])
    print result.p(['Yes'])
    print result.p(['No'])
    print norm
    print result.p(['Yes']) / norm


#    print beta
    
    """
    print "CLIQUES"
    for i in range(0,num_cliques):
        print i
        print cliques[str(i)]
        print cliques[str(i)].factors
        print
    """

    """
    print "MU"
    for i in range(0,num_cliques):
        for j in range(0,num_cliques):
            i = str(i)
            j = str(j)
            if j in mu[i]:
                print "%s -> %s" % (i,j)
                print mu[i][j]
                if mu[i][j] != 1:
                    print mu[i][j].cpd
                print

    """

    """
    print 
    """
    
    """
    print "MU %s %s" % (mu_i,mu_j)
    for var in mu[mu_i][mu_j].cpd:
        print "%s -> %s" % (var,mu[mu_i][mu_j].cpd[var])
    
    
    print "BETA %s" % beta_i
    for var in beta[beta_i].cpd:
        print "%s -> %s" % (var,beta[beta_i].cpd[var])
    

    print "BETA %s" % beta_j
    for var in beta[beta_j].cpd:
        print "%s -> %s" % (var,beta[beta_j].cpd[var])

    print sum_out(beta['3'],'L')
    print sum_out(beta['3'],'L').cpd
    """
if __name__ == "__main__":
    main();
