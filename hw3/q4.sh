# output should be System.out.printf("%d\t%f\n", numTopics, averageTestLikelihood);
#./compile
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 10 0.5 0.1 0.01 1100 1000 > question4/10-topics &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 20 0.5 0.1 0.01 1100 1000 > question4/20-topics &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 30 0.5 0.1 0.01 1100 1000 > question4/30-topics &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 40 0.5 0.1 0.01 1100 1000 > question4/40-topics &
./collapsed-sampler data/input-train.txt data/input-test.txt deleteme 50 0.5 0.1 0.01 1100 1000 > question4/50-topics &
