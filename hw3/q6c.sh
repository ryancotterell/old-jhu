# output should be System.out.printf("%f\t%f\n", lambda, averageTestLikelihood);
#./compile
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.5 0.001 0.01 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.5 10.0 0.01 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.5 0.1 0.001 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.5 0.1 10.0 1100 1000  &
