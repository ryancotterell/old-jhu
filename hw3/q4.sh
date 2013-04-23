# output should be System.out.printf("%d\t%f\n", numTopics, averageTestLikelihood);
#./compile
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 10 0.5 0.1 0.01 1100 1000 &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 20 0.5 0.1 0.01 1100 1000 &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 30 0.5 0.1 0.01 1100 1000 &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 40 0.5 0.1 0.01 1100 1000 &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 50 0.5 0.1 0.01 1100 1000 &
