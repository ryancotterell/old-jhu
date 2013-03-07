import numpy
import sys


class Factor:
	def __init__(self):
		print 'constructed Factor'


class Clique:
	def __init__(self):
		print 'constructed Factor'

class SepSet:
	i = None
	j = None
	def __init__(self, Clique c1, Clique c2):
		i = c1
		j = c2
	def __eq__(self, other):
		if (self.i == other.i and self.j == other.j):
			return True
		elif (self.j == other.i and self.i == other.j):
			return True
		else:
			return False
		
		


def multiply_factors(Factor phi1, Factor phi2):
	return 0

def divide_factors(Factor phi1, Factor phi2):
	return 0



def main():
	print 'hello'


if __name__=='__main__':
	main()
