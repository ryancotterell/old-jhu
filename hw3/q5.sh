# output should be System.out.printf("%f\t%f\n", lambda, averageTestLikelihood);
#./compile
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 25 0.0 0.1 0.01 1100 1000 > question5/00lambda &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 25 0.25 0.1 0.01 1100 1000 > question5/25lambda &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 25 0.50 0.1 0.01 1100 1000 > question5/50lambda &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 25 0.75 0.1 0.01 1100 1000 > question5/75lambda &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 25 1.00 0.1 0.01 1100 1000 > question5/100lambda &
