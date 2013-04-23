import numpy as np
import matplotlib
matplotlib.use('Agg')
import pylab

def plot_topics():
  topics, lls = np.loadtxt('question4/all-topics', unpack=True)
  pylab.clf()
  pylab.plot(topics, lls)
  pylab.xlabel('Number of topics')
  pylab.ylabel('Test Log Likelihood')
  pylab.title('Likelihood vs Number of Topics')
  pylab.subplots_adjust(left=0.15)
  pylab.savefig('topics_plot.png')


def plot_lambdas():
  lambdas, lls = np.loadtxt('question5/all_lambdas', unpack=True)
  pylab.clf()
  pylab.plot(lambdas, lls)
  pylab.xlabel('Lambda')
  pylab.ylabel('Test Log Likelihood')
  pylab.title('Likelihood vs Lambda')
  pylab.subplots_adjust(left=0.15)
  pylab.savefig('lambda_plot.png')

def plot_runtimes():
  br, blls = np.loadtxt('question3/blocked-runtimes', unpack=True)
  cr, clls = np.loadtxt('question3/collapsed-runtimes', unpack=True)
  pylab.clf()
  pylab.plot(br, blls, c='b', label='Blocked Sampler')
  pylab.plot(cr, clls, c='r', label='Collapsed Sampler')
  pylab.xlabel('Runtime (ms)')
  pylab.ylabel('Training Log Likelihood')
  pylab.legend(loc=4)
  pylab.title('Likelihood vs Runtime')
  pylab.subplots_adjust(left=0.15)
  pylab.savefig('runtime_plot.png')


def plot_block_v_collapse():
  blls = np.loadtxt('question1/blocked-25-0.5-0.1-trainll.txt', unpack=True)
  clls = np.loadtxt('question1/output1-25-0.5-0.1-trainll.txt', unpack=True)
  br = np.arange(1, len(blls) + 1, 1)
  cr = np.arange(1, len(clls) + 1, 1)
  pylab.clf()
  pylab.plot(br, blls, c='b', label='Blocked Sampler')
  pylab.plot(cr, clls, c='r', label='Collapsed Sampler')
  pylab.xlabel('Iteration Number')
  pylab.ylabel('Training Log Likelihood')
  pylab.legend(loc=4)
  pylab.title('Likelihood vs Iteration Number')
  pylab.subplots_adjust(left=0.15)
  pylab.savefig('block_v_collapse_plot.png')

def plot_question1():
  for i in range(1, 4):
    pylab.clf()
    trainfname = 'question1/output' + str(i) + '-25-0.5-0.1-trainll.txt'
    testfname = 'question1/output' + str(i) + '-25-0.5-0.1-testll.txt'
    trainlls = np.loadtxt(trainfname, unpack=True)
    testlls = np.loadtxt(testfname, unpack=True)
    iters = np.arange(1, len(trainlls) + 1, 1)
    norm_testlls = testlls / 30372.0
    norm_trainlls = trainlls / 120114.0
    pylab.plot(iters, norm_trainlls, c='r', label='Training Likelihood')
    pylab.plot(iters, norm_testlls, c='b', label='Test Likelihood')
    pylab.xlabel('Iteration Number')
    pylab.ylabel('Training Log Likelihood')
    pylab.legend(loc=4)
    pylab.title('Likelihood vs Iteration Number')
    pylab.subplots_adjust(left=0.15)
    outname = 'train_test_' + str(i) + '.png'
    pylab.savefig(outname)

    

if __name__=='__main__':
  #plot_lambdas()
  #plot_topics()
  #plot_runtimes()
  #plot_block_v_collapse()
  plot_question1()
