# Daniel Deutsch
# create-clique.py

import sys;

def main():

	# check for command line arguments
	if (len(sys.argv) != 3):
		print "Please pass the time steps and landmarks as arguments";
		quit();

	steps = int(sys.argv[1]);
	landmarks = int(sys.argv[2]);

	# need to print the number of cliques
	cliques = (4 + 4 * landmarks + 2) * (steps - 1) + (4 + 4 * landmarks + 1);
	print cliques;

	directions = ["N", "E", "S", "W"];

	# the last time step is different
	for time in range(0, steps - 1):

		# Row_t, Col_t, Wall_t
		for direction in directions:
			print "PositionRow_{0},PositionCol_{0},ObserveWall_{1}_{0}".format(time, direction);
	
		# Row_t, Col_t, Land_t
		for direction in directions:
			for landmark in range(1, landmarks + 1):
				print "PositionRow_{0},PositionCol_{0},ObserveLandmark{1}_{2}_{0}".format(time, landmark, direction);

		# Row_t, Col_t, Action_t, Row_t+1
		print "PositionRow_{0},PositionCol_{0},Action_{0},PositionRow_{1}".format(time, time + 1);

		# Col_t, Action_t, Row_t+1, Col_t+1
		print "PositionCol_{0},Action_{0},PositionRow_{1},PositionCol_{1}".format(time, time + 1);

	# do the unqiue last time step
	# Row_t, Col_t, Wall_t
	for direction in directions:
		print "PositionRow_{0},PositionCol_{0},ObserveWall_{1}_{0}".format(steps - 1, direction);

	# Row_t, Col_t, Land_t
	for direction in directions:
		for landmark in range(1, landmarks + 1):
			print "PositionRow_{0},PositionCol_{0},ObserveLandmark{1}_{2}_{0}".format(steps - 1, landmark, direction);

	# Row_t, Col_t, Action_t (there is no Row_t+1)
	print "PositionRow_{0},PositionCol_{0},Action_{0}".format(steps - 1);




	# now print the connections
	for time in range(0, steps - 2):

		for direction in directions:

			# Wall -> Parent
			print "PositionRow_{0},PositionCol_{0},ObserveWall_{1}_{0} -- PositionRow_{0},PositionCol_{0},Action_{0},PositionRow_{2}".format(time, direction, time + 1);

			# Land -> Parent
			for landmark in range(1, landmarks + 1):
				print "PositionRow_{0},PositionCol_{0},ObserveLandmark{1}_{2}_{0} -- PositionRow_{0},PositionCol_{0},Action_{0},PositionRow_{3}".format(time, landmark, direction, time + 1);
		
		# Row_t, Col_t, Action_t, Row_t+1 -> Col_t, Action_t, Row_t+1, Col_t+1
		print "PositionRow_{0},PositionCol_{0},Action_{0},PositionRow_{1} -- PositionCol_{0},Action_{0},PositionRow_{1},PositionCol_{1}".format(time, time + 1);

		# Col_t, Action_t, Row_t+1, Col_t+1 -> Row_t+1, Col_t+1, Action_t+1, Row_t+2
		print "PositionCol_{0},Action_{0},PositionRow_{1},PositionCol_{1} -- PositionRow_{1},PositionCol_{1},Action_{1},PositionRow_{2}".format(time, time + 1, time + 2);

	
	# last time step is different
	for direction in directions:
		
		# Wall -> Parent
		print "PositionRow_{0},PositionCol_{0},ObserveWall_{1}_{0} -- PositionRow_{0},PositionCol_{0},Action_{0}".format(steps - 2, direction);
		print "PositionRow_{0},PositionCol_{0},ObserveWall_{1}_{0} -- PositionRow_{0},PositionCol_{0},Action_{0}".format(steps - 1, direction);

		# Land -> Parent
		for landmark in range(1, landmarks + 1):
			print "PositionRow_{0},PositionCol_{0},ObserveLandmark{1}_{2}_{0} -- PositionRow_{0},PositionCol_{0},Action_{0}".format(steps - 2, landmark, direction);
			print "PositionRow_{0},PositionCol_{0},ObserveLandmark{1}_{2}_{0} -- PositionRow_{0},PositionCol_{0},Action_{0}".format(steps - 1, landmark, direction);

	# Row_t, Col_t, Action_t -> Col_t-1, Action_t-1, Row_t, Col_t
	print "PositionRow_{0},PositionCol_{0},Action_{0} -- PositionCol_{1},Action_{1},PositionRow_{0},PositionCol_{0}".format(steps - 1, steps - 2);
		
		













if __name__ == "__main__":
	main();