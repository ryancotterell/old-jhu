import numpy as np
import sys
import functools


# we store CPD of a factor in a numpy ndarray
class Factor:
  def __init__(self, rvs):
    self.rvs = rvs
    self.rvs.sort()
    self.cpt = self._init_cpt(rvs)
    
  def _init_cpt(self, rvs):
    dims_list = []
    for r in rvs:
      dims_list.append(len(r.values))
    dims = tuple(dims_list)
    return np.zeros(dims, np.float64)

  # from http://stackoverflow.com/questions/3076967/python-is-this-an-ok-way-of-overriding-eq-and-hash
  def __eq__(self, other):
    if not isinstance(other, Factor):
      return False
    return (set(self.rvs) == set(other.rvs))

  def __hash__(self):
    return hash(frozenset(self.rvs))

  def update_cpt_entry(self, entry, value):
    if (len(entry) != len(self.rvs)):
      raise Exception('length mismatch') 
    # order of indices is lexicographical order of RV names in factor
    indices = []
    for rv_name, rv in zip(sorted(entry), sorted(self.rvs)):
      if rv_name != rv.name:
        print entry
        print self.rvs
        raise Exception((rv_name + ' ' +  rv.name))
      # find the index that corresponds to this setting of the RV
      indices.append(rv.get_index(entry[rv_name]))
    self.cpt[tuple(indices)] = value

  def __str__(self):
    return (str(self.rvs) + "\n" + str(self.cpt) + '\n')
  
  def __repr__(self):
    return str(self)
      
      
    

class Clique:
  def __init__(self, factors, nodes):
    self.factors = factors
    self.nodes = nodes
    self.neighbors = []
  def add_neighbor(self, neighbor)
    self.neighbors.append(neighbor)

class SepSet:
  def __init__(self, c1, c2):
    self.i = c1
    self.j = c2
  def __eq__(self, other):
    if (self.i == other.i and self.j == other.j):
      return True
    elif (self.j == other.i and self.i == other.j):
        return True
    else:
        return False

@functools.total_ordering
class Node:
  def __init__(self, nm, vals):
    self.values = vals
    # sort values so they will be in alphabetical order
    self.values.sort()
    self.name = nm
    self.children = []
    self.parents = []

  def add_child(self, child):
    self.children.append(child)

  def add_parent(self, par):
    self.parents.append(par)

  # maps string setting value onto a numerical index
  def get_index(self, setting):
    return self.values.index(setting)
  def __str__(self):
    return ('name: ' + self.name + ' values: ' + str(self.values) + ' parents: ' + str(self.parents) + '\n')
  def __repr__(self):
    return str(self)
  
  def __eq__(self, other):
    if not isinstance(other, Node):
      return False
    return self.name == other.name
  def __lt__(self, other):
    return self.name < other.name

def multiply_factors(phi1, phi2):
  return 0

def divide_factors(phi1, phi2):
  return 0


def parse_network(netfile):
  net = open(netfile, 'r')
  # need file error handling
  num_vars = int(net.readline())
  nodes = {}
  for i in range(num_vars):
    line = net.readline()
    line = line.strip()
    halves = line.split(' ')
    name = halves[0]
    vals = halves[1].split(',')
    cur_node = Node(name, vals)
    nodes[name] = cur_node

  for line in net:
    line = line.strip()
    comps = line.split(' ')
    nodes[comps[0]].add_child(comps[2])
    nodes[comps[2]].add_parent(comps[0])
  net.close() 
  return nodes

def create_factors(nodes):
  # now create factors
  # TODO(crankshaw) might be good to add factors to cliques here as well
  factors = {}
  for name in nodes:
    nodes_in_factor = [nodes[n] for n in nodes[name].parents]
    node_names = list(nodes[name].parents)
    node_names.append(name)
    # nodes_in_factor = list(nodes[name].parents)
    nodes_in_factor.append(nodes[name])
    # print nodes_in_factor
    factors[frozenset(node_names)]= Factor(nodes_in_factor)
  return factors

  # maybe name factor based on a hash of all nodes in the factor

  # when reading in clique tree ensure that it contains correct factors

def parse_cpd(nodes, factors, cpd_file): 
  cpd = open(cpd_file, 'r')
  for line in cpd:
    line = line.strip()
    # print line
    halves = line.split(' ')
    # print halves
    value = float(halves[2])
    variables = []
    variables.append(halves[0])
    variables = variables + halves[1].split(',')
    # print variables
    entry = {}
    for var in variables:
      sp = var.split('=')
      name = sp[0]
      setting = sp[1]
      entry[name] = setting
    add_entry_to_factor(factors, entry, value)
  cpd.close()

# entry is a dictionary containg member names as keys and
# their settings as values.
# value is the value of the CPT with these settings
def add_entry_to_factor(factors, entry, value):
  # key is an immutable set of the names of all RVs in the factor
  # keyset = set()
  # for name in entry:
  #     keyset.add(name)
  key = frozenset(iter(entry))
  factor = factors[key]
  factor.update_cpt_entry(entry, value)
  
  

# TODO(crankshaw) update all factors with no parents
# so that the entries reflect the uniform distribution
def update_cpds_for_single_node_factors(factors):
  for factor in factors:
    if len(factor.rvs) == 1:
      # this factor has no parents
      node = factor.rvs[0]
      if len(node.parents) > 0:
        raise Exception('factor has one node that has parents')
      # fill in CPT with uniform distribution
      factor.cpt = np.ones(len(node.values))/np.float64(len(node.values))

def create_cliques(clique_file, factors):
  with open(clique_file, 'r') as read_cliques:
    cliques = {}
    num_cliques = int(read_cliques.readline())
    for i in range(num_cliques):
      line = read_cliques.readline().strip()
      comps = line.split(',')
      current_clique = frozenset(comps)
      factors_in_clique = {}
      for i, f in factors.iteritems():
        # check if factor is a subset of this clique
        if i <= current_clique:
          factors_in_clique[i] = f
      cliques[current_clique] = Clique(factors_in_clique, current_clique)
    sepsets = []
    for line in read_cliques:
      parts = line.strip().split(' ')
      if len(parts) != 3:
        raise Exception('malformed clique tree file')
      first_clique_set = frozenset(parts[0].split(','))
      second_clique_set = frozenset(parts[2].split(','))
      first_clique = cliques[first_clique_set]
      second_clique = cliques[second_clique_set]
      sepsets.append(SepSet(first_clique, second_clique))
      first_clique.add_neighbor(second_clique)
      second_clique.add_neighbor(first_clique)
  return (cliques, sepsets)
      
    

def main(netfile, cpdfile):
  nodes = parse_network(netfile)
  factors = create_factors(nodes)
  parse_cpd(nodes, factors, cpdfile) 
  update_cpds_for_single_node_factors(factors)
  for f in factors:
    print factors[f]
  # I now have all of my factors complete with cpds
  # time to put them into clique

  # TODO I think the alphabetical ordering of my RVs within
  # the factors isn't working. My lookup scheme for the factors
  # (creating the dummy factors) also isn't working.




if __name__=='__main__':
  # print sys.argv
  main(sys.argv[1], sys.argv[2])






















