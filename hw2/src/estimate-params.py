# Daniel Deutsch
# Dan Crankshaw
# Ryan 'the Beast' Cotterell
#    "Two T's, two E's, two L's" - the family motto

import sys;

# This will figure out the dimensions of the grid
def get_dimensions_and_landmarks_and_trajectories(network_lines):
    
    row = -1;
    col = -1;
    landmarks = -1;
    trajectories = -1;
        
    for line in network_lines:
        if line.startswith("PositionRow_"):
            line_split = line.split(",");
            row = line_split[-1].rstrip("\n");
            break;
    
    for line in network_lines:
        if line.startswith("PositionCol_"):
            line_split = line.split(",");
            col = line_split[-1].rstrip("\n");
            break;
        
    for line in network_lines:
        if line.startswith("ObserveLandmark"):
            observation,_,_ = line.split(" ")[0].split("_");
            landmarks = max(landmarks, observation[-1]);
   
    for line in network_lines:
        if line.startswith("PositionRow_"):
            _,time = line.split(" ")[0].split("_");
            trajectories = max(trajectories, int(time));
   
    return int(row), int(col), int(landmarks), int(trajectories);
            

# will read in the PositionRow_t+1 | PositionRow_t, Action and
# PositionCol_t+1 | PositionCol_t, Action conditional probability tables.
def read_position_cpt(training_lines, rows, columns):
    
    # initialize the CPT
    row_cpt = {};
    col_cpt = {};
    actions = ["MoveNorth", "MoveEast", "MoveSouth", "MoveWest"];
    
    for action in actions:
        row_cpt["PositionRow_t+1=i|PositionRow_t=i-1,Action_t=" + action] = 1;
        row_cpt["PositionRow_t+1=i|PositionRow_t=i+1,Action_t=" + action] = 1;
        row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] = 1;
        col_cpt["PositionCol_t+1=j|PositionCol_t=j-1,Action_t=" + action] = 1;
        col_cpt["PositionCol_t+1=j|PositionCol_t=j+1,Action_t=" + action] = 1;
        col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] = 1;
        
    # indicate we're starting a new trajectory
    trajectory = -1;
    previousRow = -1;
    previousCol = -1;
    previousAction = "";
    
    # need to keep track of how many times each move happens
    moveCounters = {"MoveNorth" : 0, "MoveEast" : 0, "MoveSouth" : 0, "MoveWest" : 0};
    
    for line in training_lines:
        
        line_split = line.split(" ");

        # we're starting a new trajectory
        if line_split[0] != trajectory:
            trajectory = line_split[0];
            _,previousRow = line_split[2].rstrip("\n").split("=");
            _,previousCol = line_split[3].rstrip("\n").split("=");
            _,previousAction = line_split[4].rstrip("\n").split("=");
            continue;
              
        # get the current position and action
        _,row = line_split[2].rstrip("\n").split("=");
        _,col = line_split[3].rstrip("\n").split("=");
        _,action = line_split[4].rstrip("\n").split("=");
        
        # figure out the row change
        positionChange = "";
        if (row == previousRow):
            positionChange = "i";
        elif (int(row) + 1 == int(previousRow) or (int(row) == int(rows) and int(previousRow) == 1)):
            positionChange = "i+1";
        elif (int(row) == int(previousRow) + 1 or (int(row) == 1 and int(previousRow) == int(rows))):
            positionChange = "i-1";
        else:
            print "row hit else case";
        
        # update the row_cpt
        expression = "PositionRow_t+1=i|PositionRow_t=" + positionChange + ",Action_t=" + previousAction;        
        row_cpt[expression] = row_cpt[expression] + 1;

        # figure out the column change
        if (col == previousCol):
            positionChange = "j";
        elif (int(col) + 1 == int(previousCol) or (int(col) == int(columns) and int(previousCol) == 1)):
            positionChange = "j+1";
        elif (int(col) == int(previousCol) + 1 or (int(col) == 1 and int(previousCol) == int(columns))):
            positionChange = "j-1";
        else:
            print "col hit else case";

        # update the col_cpt
        expression = "PositionCol_t+1=j|PositionCol_t=" + positionChange + ",Action_t=" + previousAction;
        col_cpt[expression] = col_cpt[expression] + 1;
        
        # update move counter since we used that action
        moveCounters[previousAction] = moveCounters[previousAction] + 1;
        
        # update the previous values
        previousRow = row;
        previousCol = col;
        previousAction = action;
        
    # need to now make them probabilities
    # normalize by the number of times we saw that action + 3 for Laplacian smoothing
    
    # MoveNorth
    action = "MoveNorth";
    row_cpt["PositionRow_t+1=i|PositionRow_t=i-1,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i-1,Action_t=" + action] / float(2 + moveCounters[action]));
    row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] / float(2 + moveCounters[action]));
    col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] / float(1 + moveCounters[action]));
    
    # MoveSouth
    action = "MoveSouth";
    row_cpt["PositionRow_t+1=i|PositionRow_t=i+1,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i+1,Action_t=" + action] / float(2 + moveCounters[action]));
    row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] / float(2 + moveCounters[action]));
    col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] / float(1 + moveCounters[action]));

    # MoveEast
    action = "MoveEast";
    row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] / float(1 + moveCounters[action]));
    col_cpt["PositionCol_t+1=j|PositionCol_t=j-1,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j-1,Action_t=" + action] / float(2 + moveCounters[action]));
    col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] / float(2 + moveCounters[action]));

    # MoveWest
    action = "MoveWest";
    row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] / float(1 + moveCounters[action]));
    col_cpt["PositionCol_t+1=j|PositionCol_t=j+1,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j+1,Action_t=" + action] / float(2 + moveCounters[action]));
    col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] / float(2 + moveCounters[action]));

    # this section was correct before the correction on piazza
#    for action in actions:
#        row_cpt["PositionRow_t+1=i|PositionRow_t=i-1,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i-1,Action_t=" + action] / float(3 + moveCounters[action]));
#        row_cpt["PositionRow_t+1=i|PositionRow_t=i+1,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i+1,Action_t=" + action] / float(3 + moveCounters[action]));
#        row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] = float(row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action] / float(3 + moveCounters[action]));
#        col_cpt["PositionCol_t+1=j|PositionCol_t=j-1,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j-1,Action_t=" + action] / float(3 + moveCounters[action]));
#        col_cpt["PositionCol_t+1=j|PositionCol_t=j+1,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j+1,Action_t=" + action] / float(3 + moveCounters[action]));
#        col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] = float(col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action] / float(3 + moveCounters[action]));
    
    return row_cpt, col_cpt;
        
# This will compute the CPTs for Observation_?_T | PositionRow_t, PositionCol_t
def read_observation_cpt(training_lines, rows, columns, numLandmarks):

    # initialize the CPTs
    wall_cpt = {};
    land_cpt = {};
    position_counter = {};
    
    directions = ["N", "E", "S", "W"];
    
    for direction in directions:
        for row in range(1, rows + 1):
            for col in range(1, columns + 1):
                
                # start at 2 to account for +1 smoothing with two values: Yes, No
                position_counter[str(row) + "," + str(col)] = 0;
                
                # start at 1 to account for +1 smoothing with one value: Yes
                wall_cpt["ObserveWall_" + direction + "_t|PositionRow_t=" + str(row) + ",PositionCol_t=" + str(col)] = 1;
                
                for land in range(1, numLandmarks + 1):
                    land_cpt["ObserveLandmark" + str(land) + "_" + direction + "_t|PositionRow_t=" + str(row) + ",PositionCol_t=" + str(col)] = 1;
        
    for line in training_lines:
        line_split = line.split(" ");
        _,row = line_split[2].rstrip("\n").split("=");
        _,col = line_split[3].rstrip("\n").split("=");
        position_counter[row + "," + col] = position_counter[row + "," + col] + 1;
        
        # there could be many observations at one point
        for i in range(5, len(line_split)):
                
            # breaks up the line "ObserveLandmark2_N_0=True"
            observation,direction,_ = line_split[i].split("=")[0].split("_");
            
            if observation.startswith("ObserveWall"):
                expression = "ObserveWall_" + direction + "_t|PositionRow_t=" + row + ",PositionCol_t=" + col;
                wall_cpt[expression] = wall_cpt[expression] + 1;
            else:
                expression = observation + "_" + direction + "_t|PositionRow_t=" + row + ",PositionCol_t=" + col;
                land_cpt[expression] = land_cpt[expression] + 1;
                
#    print "(1,1) ObserveWall_N = " + str(wall_cpt["ObserveWall_N_t|PositionRow_t=1,PositionCol_t=1"]);
#    print "(1,1) ObserveLandmark1_N = " + str(land_cpt["ObserveLandmark1_N_t|PositionRow_t=1,PositionCol_t=1"]);
#    print "(1,1) PositionCounter = " + str(position_counter["1,1"]);
    
    # need to make them into probabilities
    for row in range(1, rows + 1):
        for col in range(1, columns + 1):
            for direction in directions:
                
                wall_cpt["ObserveWall_" + direction + "_t|PositionRow_t=" + str(row) + ",PositionCol_t=" + str(col)] = float(wall_cpt["ObserveWall_" + direction + "_t|PositionRow_t=" + str(row) + ",PositionCol_t=" + str(col)]) / float(2 + position_counter[str(row) + "," + str(col)]);
                
                for land in range(1, numLandmarks + 1):
                    land_cpt["ObserveLandmark" + str(land) + "_" + direction + "_t|PositionRow_t=" + str(row) + ",PositionCol_t=" + str(col)] = float(land_cpt["ObserveLandmark" + str(land) + "_" + direction + "_t|PositionRow_t=" + str(row) + ",PositionCol_t=" + str(col)]) / (float(2 + position_counter[str(row) + "," + str(col)]));
    
        
    return wall_cpt, land_cpt;
    

def print_row_cpt(row_cpt, rows, trajectories, output_file):
    
    actions = ["MoveNorth", "MoveEast", "MoveSouth", "MoveWest"];

    for time in range(1, trajectories + 1):
        for row in range(1, rows + 1):
            for action in actions:
                
                # i == i - 1
                prob = row_cpt["PositionRow_t+1=i|PositionRow_t=i-1,Action_t=" + action];
                otherPosition = row - 1;
                
                # do we need to wrap around?
                if otherPosition == 0:
                    otherPosition = rows;
                
                if (action == "MoveNorth"):
                    output_file.write("PositionRow_{0}={1} PositionRow_{2}={3},Action_{2}={4} {5}\n".format(time, row, time - 1, otherPosition, action, prob));
                
                #i == i
                prob = row_cpt["PositionRow_t+1=i|PositionRow_t=i,Action_t=" + action];
                otherPosition = row;
                
                output_file.write("PositionRow_{0}={1} PositionRow_{2}={3},Action_{2}={4} {5}\n".format(time, row, time -1, otherPosition, action, prob));
                
                #i == i + 1
                prob = row_cpt["PositionRow_t+1=i|PositionRow_t=i+1,Action_t=" + action];
                otherPosition = row + 1;
                
                # do we need to wrap around?
                if otherPosition == rows + 1:
                    otherPosition = 1;
                
                if (action == "MoveSouth"):
                    output_file.write("PositionRow_{0}={1} PositionRow_{2}={3},Action_{2}={4} {5}\n".format(time, row, time - 1, otherPosition, action, prob));

def print_col_cpt(col_cpt, columns, trajectories, output_file):
    
    actions = ["MoveNorth", "MoveEast", "MoveSouth", "MoveWest"];

    for time in range(1, trajectories + 1):
        for col in range(1, columns + 1):
            for action in actions:
                
                # j == j - 1
                prob = col_cpt["PositionCol_t+1=j|PositionCol_t=j-1,Action_t=" + action];
                otherPosition = col - 1;
                
                # do we need to wrap around?
                if otherPosition == 0:
                    otherPosition = columns;
                
                if (action == "MoveEast"):
                    output_file.write("PositionCol_{0}={1} PositionCol_{2}={3},Action_{2}={4} {5}\n".format(time, col, time - 1, otherPosition, action, prob));
                
                #i == i
                prob = col_cpt["PositionCol_t+1=j|PositionCol_t=j,Action_t=" + action];
                otherPosition = col;
                
                output_file.write("PositionCol_{0}={1} PositionCol_{2}={3},Action_{2}={4} {5}\n".format(time, col, time - 1, otherPosition, action, prob));
                
                #i == i + 1
                prob = col_cpt["PositionCol_t+1=j|PositionCol_t=j+1,Action_t=" + action];
                otherPosition = col + 1;
                
                # do we need to wrap around?
                if otherPosition == columns + 1:
                    otherPosition = 1;
                
                if (action == "MoveWest"):
                    output_file.write("PositionCol_{0}={1} PositionCol_{2}={3},Action_{2}={4} {5}\n".format(time, col, time - 1, otherPosition, action, prob));


def print_observation_cpt(wall_cpt, land_cpt, rows, columns, landmarks, trajectories, output_file):

    directions = ["N", "E", "S", "W"];
    
    for time in range(0, trajectories + 1):
        for row in range(1, rows + 1):
            for col in range(1, columns + 1):
                for direction in directions:
                    
                    prob = wall_cpt["ObserveWall_{0}_t|PositionRow_t={1},PositionCol_t={2}".format(direction, row, col)];
                    
                    output_file.write("ObserveWall_{0}_{1}=Yes PositionRow_{1}={2},PositionCol_{1}={3} {4}\n".format(direction, time, row, col, prob));
                    output_file.write("ObserveWall_{0}_{1}=No PositionRow_{1}={2},PositionCol_{1}={3} {4}\n".format(direction, time, row, col, 1 - prob));
                    
                    for land in range(1, landmarks + 1):
                        prob = land_cpt["ObserveLandmark{0}_{1}_t|PositionRow_t={2},PositionCol_t={3}".format(land, direction, row, col)];
                        
                        output_file.write("ObserveLandmark{0}_{1}_{2}=Yes PositionRow_{2}={3},PositionCol_{2}={4} {5}\n".format(land, direction, time, row, col, prob));
                        output_file.write("ObserveLandmark{0}_{1}_{2}=No PositionRow_{2}={3},PositionCol_{2}={4} {5}\n".format(land, direction, time, row, col, 1 - prob));

def print_full_cpt(row_cpt, col_cpt, wall_cpt, land_cpt, rows, columns, landmarks, trajectories, output_file):

    print_row_cpt(row_cpt, rows, trajectories, output_file);
    print_col_cpt(col_cpt, columns, trajectories, output_file);
    print_observation_cpt(wall_cpt, land_cpt, rows, columns, landmarks, trajectories, output_file);
    
def main():
    
    # check for command line args
    if (len(sys.argv) != 4):
        print "Please provide the network, training, and cpd-output files";
        exit();

    # read the network file
    network_lines = open(sys.argv[1]).readlines();
    num_variables = int(network_lines[0].rstrip("\n"));
    
    # get the dimensions of the grid
    rows,columns,landmarks,trajectories = get_dimensions_and_landmarks_and_trajectories(network_lines);
    
    # read in the possible values for each variable
    possible_values = {};
    for i in range(1, num_variables + 1):
        
        # break up the variable and the values
        line = network_lines[i].rstrip("\n");
        variable,values = line.split(" ");
        values = values.split(",");
    
        # place the values in the hashmap
        possible_values[variable] = values;
        
    # open the training data
    training_lines = open(sys.argv[2]).readlines();

    positionRow_cpt, positionCol_cpt = read_position_cpt(training_lines, rows, columns);

    wall_cpt, land_cpt = read_observation_cpt(training_lines, rows, columns, landmarks);

    # I have confirmed with a regex that the number of times Action=MoveNorth 
    # happens and we use it is 22,783. We add 3 for smoothing

    output_file = open(sys.argv[3], "w+");
    print_full_cpt(positionRow_cpt, positionCol_cpt, wall_cpt, land_cpt, rows, columns, landmarks, trajectories, output_file);
    output_file.close();
    

if __name__ == "__main__":
    main();
    
    
    
    
    
    
    
