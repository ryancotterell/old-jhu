import numpy as np
import sys
import matplotlib
matplotlib.use('Agg')
import pylab


def main(infile, outfile):
    lls = np.loadtxt(infile, unpack=True)
    lls = lls[:900]
    xs = np.arange(1, len(lls) + 1, 1)

    pylab.plot(xs, lls, c='b')
    pylab.xlabel('Iterations')
    pylab.ylabel('Log likelihood')
    pylab.savefig(outfile)



if __name__=='__main__':
    main(sys.argv[1], sys.argv[2])
