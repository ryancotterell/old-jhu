#!/usr/bin/python
#
# Machine Learning in Complex Domains
# 600.674
# Homework 1 
#
# Ryan Cotterell, Dan Crankshaw, Dan Deutsch
# (c) 2013
#

import sys
import collections
import itertools

class factor:
    """
    This is a factor of a graph. 

    p(left | right)

    cpd is the conditional probability distribution hash table
    rv_values is a hash of random variables to every value they could take on

    """

    def __init__(self,left,right,cpd,rv_values):
        self.left = left
        self.right = right
        self.cpd = cpd
        self.rv_values = rv_values


    def prob(self,vals):
        """
        Returns a probability for a set of random variables

        IMPORANT!!!
        vals can contain any extraneous randomv variables, the
        probability will only be computed over the random variables
        input in the contructor, that is left and right. This was done
        intentionally to simplify other parts of the code
        
        """
       
        #in the case of a uniform prior
        if len(self.right) == 0:
            return 1.0/float(len(self.rv_values[self.left]))
        """
        ####
        # EARTH QUAKE EXAMPLE
        ###

        if self.left == "B" and len(self.right) == 0:
            if vals[self.left] == "t":
                return .001
            else:
                return .999
        if self.left == "E" and len(self.right) == 0:
            if vals[self.left] == "t":
                return .002
            else:
                return .998
        """

        #standard value
        query = "%s=%s|" % (self.left,vals[self.left])
        
        for i,right in enumerate(self.right):
            if i > 0:
                query += ","
                
            query += "%s=%s" % (right,vals[right])

        return self.cpd[query]
    
    def scope(self):
        """
        Returns the scope of the factor
        """
        return self.right + [self.left]

class tau:
    """
    tau from the variable elimination algorithm in 
    Probabilistic Graphical Models - Daphne Koller 2009

    Basically this is just a factor. Mathematically, it is a
    product of factors that have been marginalized
    """
    def __init__(self,factors,marginalized,rv_vals):
        self.factors = factors
        self.marginalized = marginalized
        self.rv_vals = rv_vals

        #gets the new scope for tau
        self.scope_vars = []
        for f in self.factors:
            for var in f.scope():
                if var not in marginalized:
                    self.scope_vars.append(var)
        

    def scope(self):
        """
        Returns the scope of the marginalized factors
        """
        return self.scope_vars
    
    def prob(self,vars):
        """
        Returns the probability, similar to the
        method in the factor class
        """
        sum = 0
        cart_product = itertools.product(*[ self.rv_vals[var] for var in self.marginalized])

        done = False
        for vals in cart_product:
            done = True

            for m,value in zip(self.marginalized,vals):
                vars[m] = value
                                
            prod = 1               

            for factor in self.factors:
                prod *= factor.prob(vars)
            
            sum += prod
           
        if not done:
            prod = 1
            for factor in self.factors:

                prod *= factor.prob(vars)

            sum = prod

        return sum


def eliminate(factors,z,rv_vals):
    """
    The elimination algorithm for 
    variable elimination
    """
    
    factors1 = []
    factors2 = []
    
    for f in factors:
        if z in f.scope():
            factors1.append(f)
        else:
            factors2.append(f)


    t = tau(factors1,[z],rv_vals)
   
    factors2.append(t)
    return factors2

def parse_input(left,right):
    """
    Parses the input of the query and observed variables
    Returns a pair of hashes
    """
    query_vars = {}
    observed_vars = {}
    if "," in left:
        for var in left.split(","):
            x,y = var.split("=")
            query_vars[x] = y
    elif left != "":
        x,y = left.split("=")
        query_vars[x] = y

    if "," in right:
        for var in right.split(","):
            x,y = var.split("=")
            observed_vars[x] = y
    elif right != "":
        x,y = right.split("=")
        observed_vars[x] = y

    return query_vars,observed_vars


def main():
    #read in the files
    network = open(sys.argv[1])
    network_lines = network.readlines()
    network.close()

    cpd = open(sys.argv[2])
    cpd_lines= cpd.readlines()
    cpd.close()

    num_random_variables = int(network_lines[0].rstrip("\n"))
    rv_vals = {}
    parents = collections.defaultdict(list)


    for i in range(1,num_random_variables + 1):
        line = network_lines[i].rstrip("\n")
        rv,values = line.split(" ")
        values = values.split(",")
        
        rv_vals[rv] = values

    for i in range(num_random_variables + 1, len(network_lines)):
        line = network_lines[i].rstrip("\n")
        
        parent,child = line.split(" -> ")
        parents[child].append(parent)

    
    
    cpds = {} #one hash table for all the cpds

    for line in cpd_lines:
        line = line.rstrip("\n")
        left,right,val = line.split(" ")
        right = right.split(",")
        val = float(val)

        query= left + "|" + ",".join(right)
        cpds[query] = val
    

    #READ IN THE FACTORS
    factors = []
    for rv in rv_vals.keys():
        if rv in parents:
            factors.append(factor(rv,parents[rv],cpds,rv_vals))
        else:
            factors.append(factor(rv,[],cpds,rv_vals))



    #parse the command line input
    left = ""
    right = ""
    if len(sys.argv) > 3:
        left = sys.argv[3]
    if len(sys.argv) > 4:
        right = sys.argv[4]
    query_var,observed_vars = parse_input(left,right)


    #calculate the unnormalized probability
    Z = filter(lambda x: x not in query_var.keys() and x not in observed_vars.keys(),rv_vals.keys())
    
    for z in Z:
        factors = eliminate(factors,z,rv_vals)
    
    query_prod = 1               
    vars = dict(query_var.items() + observed_vars.items())

    for f in factors:
        query_prod *= f.prob(vars)
        

    #calculate the normalization coeffiient
    sum = 0
    cart_product = itertools.product(*[ rv_vals[var] for var in query_var.keys()])
    
    done = False
    for vals in cart_product:
        done = True
        
        for name,val in zip(query_var.keys(),vals):
            vars[name] = val
    
        prod = 1
        for f in factors:
            prod *= f.prob(vars)

        sum += prod

    print query_prod / sum


if __name__ == "__main__":
    main()
