import numpy as np
import sys
import functools


# we store CPD of a factor in a numpy ndarray
class Factor:
  def __init__(self, rvs, cpt=None):
    rvs.sort()
    self.rvs = list(rvs)
    self.rvs.sort()
    if cpt == None:
      self.cpt = self._init_cpt(rvs)
    else:
      self.cpt = np.copy(cpt)
    
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
  def add_neighbor(self, neighbor):
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
  def __hash__(self):
    return hash(self.name)

def multiply_factors(phi1, phi2):
  rv_union = set(phi1.rvs).union(set(phi2.rvs))
  rv_union_list = list(rv_union)
  rv_union_list.sort()
  dims = []
  for r in rv_union_list:
    dims.append(len(r.values))
  shape = np.zeros(dims)
  product = np.zeros(dims)
  
  it = np.nditer(shape, flags=['multi_index'])
  while not it.finished:
    phi1_index = []
    for i, ix in zip(rv_union_list, range(len(rv_union_list))):
      if i in phi1.rvs:
        phi1_index.append(it.multi_index[ix])
    phi2_index = []
    for i, ix in zip(rv_union_list, range(len(rv_union_list))):
      if i in phi2.rvs:
        phi2_index.append(it.multi_index[ix])
    phi1_index = tuple(phi1_index)
    phi2_index = tuple(phi2_index)
    product[tuple(it.multi_index)] = phi1.cpt[phi1_index] * phi2.cpt[phi2_index]
    it.iternext()
  
  return Factor(rv_union_list, product)
    
def test_multiply_factors():
  rv1 = Node('a', [1, 2, 3])
  rv2 = Node('b', [1, 2])
  rv3 = Node('c', [1, 2])
  phi1 = Factor([rv1, rv2], cpt=None)
  phi2 = Factor([rv2, rv3], cpt=None)
  e1 = {'a': 1, 'b': 1} # 0.5
  phi1.update_cpt_entry(e1, 0.5)
  e2 = {'a': 1, 'b': 2} # 0.8
  phi1.update_cpt_entry(e2, 0.8)
  e3 = {'a': 2, 'b': 1} # 0.1
  phi1.update_cpt_entry(e3, 0.1)
  e4 = {'a': 3, 'b': 1} # 0.3)
  phi1.update_cpt_entry(e4, 0.3)
  e5 = {'a': 3, 'b': 2} # 0.9
  phi1.update_cpt_entry(e5, 0.9)

  f1 = {'b': 1, 'c': 1} # 0.5
  phi2.update_cpt_entry(f1, 0.5)
  f2 = {'b': 1, 'c': 2} # 0.7
  phi2.update_cpt_entry(f2, 0.7)
  f3 = {'b': 2, 'c': 1} # 0.1
  phi2.update_cpt_entry(f3, 0.1)
  f4 = {'b': 2, 'c': 2} # 0.2
  phi2.update_cpt_entry(f4, 0.2)
  result1 = multiply_factors(phi1, phi2)
  result2 = multiply_factors(phi2, phi1)
  print result1
  print '\n'
  print result2
  

# computes factor quotient of psi / phi
def divide_factors(psi, phi):
  psi_set = set(psi.rvs)
  phi_set = set(phi.rvs)
  if not phi_set <= psi_set:
    raise Exception('malformed denominator')
  dims = []
  for r in psi.rvs:
    dims.append(len(r.values))
  shape = np.zeros(dims)
  result = np.zeros(dims)
  
  it = np.nditer(shape, flags=['multi_index'])
  while not it.finished:
    phi_index = []
    for i, ix in zip(psi.rvs,  range(len(psi.rvs))):
      if i in phi.rvs:
        phi_index.append(it.multi_index[ix])
    phi_index = tuple(phi_index)
    full_ind = tuple(it.multi_index)
    if phi.cpt[phi_index] == 0:
      if psi.cpt[full_ind] != 0:
        raise Exception('division by zero')
    else:
      result[tuple(it.multi_index)] = psi.cpt[full_ind] / phi.cpt[phi_index]
    it.iternext()
  
  return Factor(phi.rvs, result)

def test_divide_factors():
  rv1 = Node('a', [1, 2, 3])
  rv2 = Node('b', [1, 2])
  psi = Factor([rv1, rv2], cpt=None)
  phi = Factor([rv1], cpt=None)
  e1 = {'a': 1, 'b': 1} # 0.5
  psi.update_cpt_entry(e1, 0.5)
  e2 = {'a': 1, 'b': 2} # 0.8
  psi.update_cpt_entry(e2, 0.2)
  e4 = {'a': 3, 'b': 1} # 0.3)
  psi.update_cpt_entry(e4, 0.3)
  e5 = {'a': 3, 'b': 2} # 0.9
  psi.update_cpt_entry(e5, 0.45)

  f1 = {'a': 1} # 0.5
  phi.update_cpt_entry(f1, 0.8)
  f3 = {'a': 3} # 0.5
  phi.update_cpt_entry(f3, 0.6)
  result1 = divide_factors(psi, phi)
  print result1


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

def add_entry_to_factor_test(factor, entry, value):
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
  for f, factor in factors.iteritems():
    if len(factor.rvs) == 1:
      # this factor has no parents
      node = factor.rvs[0]
      if len(node.parents) > 0:
        raise Exception('factor has one node that has parents')
      # fill in CPT with uniform distribution
      factor.cpt = np.ones(len(node.values))/np.float64(len(node.values))
      print factor

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
  #for f in factors:
    #print factors[f]
  # I now have all of my factors complete with cpds
  # time to put them into clique

########################################################
######################### TODO #########################
  # implement multiplication, division of factors
  # implement clique tree calibration
  # answer queries

########################################################



if __name__=='__main__':
  # print sys.argv
  # main(sys.argv[1], sys.argv[2])
  # test_multiply_factors()
  test_divide_factors()






















