# output should be System.out.printf("%f\t%f\n", lambda, averageTestLikelihood);
#./compile
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.00 0.1 0.01 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.25 0.1 0.01 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.50 0.1 0.01 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 0.75 0.1 0.01 1100 1000  &
./collapsed-sampler data/input-train.txt data/input-test.txt question6/output 25 1.00 0.1 0.01 1100 1000  &
