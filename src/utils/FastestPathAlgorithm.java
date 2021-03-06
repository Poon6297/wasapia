package utils;

import controllers.SimulatorController;
import models.Grid;
import models.MyRobot;
import views.CenterPanel;

import java.util.ArrayList;
import java.util.Stack;

import static models.Constants.*;

public class FastestPathAlgorithm {

    private MyRobot myRobot;
    private SimulatorController sim;

    // Grids that still need to be evaluated
    private ArrayList<Grid> openSet;
    // Grids that are already evaluated
    private ArrayList<Grid> closedSet;
    Stack<Grid> path = new Stack();
    private int i;
    private String[] waypoint;
    private String instruction = "";
    int numberOfForward=0;

    public FastestPathAlgorithm(MyRobot myRobot, SimulatorController sim) {
    	
        this.myRobot = myRobot;
        this.sim = sim;
        waypoint = parseInputToRowColArr(sim.getCenterPanel().getFields()[3].getText());
    }

    public void A_Star() throws Exception{
    	int col = Integer.parseInt(waypoint[1], 10);
    	int row = Integer.parseInt(waypoint[0], 10);
    	
    	buildTree(col, row);
    	path = getFastestPath(col ,row);
    	
//    	if(myRobot.isRealRun()) {
        	instruction(path, false);
//    	}
//    	else
//    		executeFastestPath(path);

    	myRobot.getArena().resetGridCost();
    	
    	buildTree(GOAL_ZONE_COL, GOAL_ZONE_ROW);
    	path = getFastestPath(GOAL_ZONE_COL, GOAL_ZONE_ROW);
    	
    	
//    	if(myRobot.isRealRun()) {
        	instruction(path, true);
        	System.out.println("The Instruction is :" + instruction);
        	
            myRobot.setCurCol(myRobot.getStartCol());
            myRobot.setCurRow(myRobot.getStartRow());
            myRobot.setCurOrientation(myRobot.getStartOrientation());
//    	}
//    	else
//    		executeFastestPath(path);
    }

    private void buildTree(int col , int row) {
    	Grid startingGrid = myRobot.getArena().getGrid(myRobot.getCurRow(), myRobot.getCurCol());
    	
        closedSet = new ArrayList<>();
        openSet = new ArrayList<>();
        openSet.add(startingGrid);

        startingGrid.setG(0);
        startingGrid.setH(calculateHeuristic(startingGrid, col, row));
        startingGrid.setO(myRobot.getCurOrientation());
        boolean searchCompletedFlag = false;
        Grid curGrid;
        ArrayList<Grid> curNeighbouringGridArrList;
        
        
        while (openSet.size() > 0 && !searchCompletedFlag) {
            int indexOfNodeWithLowestF;

            indexOfNodeWithLowestF = pickLowestF(openSet);

            curGrid = openSet.get(indexOfNodeWithLowestF);
            
            if(col != GOAL_ZONE_COL && row!=GOAL_ZONE_ROW){
	            if (isWayPoint(curGrid)) {
		                searchCompletedFlag = true;
		            }
            }
            else {
	            if (isGoalNode(curGrid)) {
	                searchCompletedFlag = true;
	            }
            }
            	

            openSet.remove(curGrid);
            closedSet.add(curGrid);

            curNeighbouringGridArrList = getNeighbouringGrids(curGrid.getRow(), curGrid.getCol());

            Grid curNeighbour;
            int tempG;
            Orientation tempO;
            for (i = 0; i < curNeighbouringGridArrList.size(); i++) {
                curNeighbour = curNeighbouringGridArrList.get(i);
                if (!closedSet.contains(curNeighbour)) {
                    tempO = getRespectiveOrientationToTarget(curGrid.getRow(), curGrid.getCol(),
                            curNeighbour.getRow(), curNeighbour.getCol());
                    tempG = curGrid.getG() + calculateG(curGrid.getO(), tempO);
                    if (openSet.contains(curNeighbour)) {
                        if (tempG < curNeighbour.getG()) {
                            curNeighbour.setG(tempG);
                            curNeighbour.setO(tempO);
                            curNeighbour.setH(calculateHeuristic(curNeighbour, col, row));
                            curNeighbour.setCameFrom(curGrid);
                        }
                    } else {
                        curNeighbour.setG(tempG);
                        curNeighbour.setO(tempO);
                        openSet.add(curNeighbour);
                        curNeighbour.setH(calculateHeuristic(curNeighbour, col, row));
                        curNeighbour.setCameFrom(curGrid);
                    }
                }
            }
        }
    }
    
    private int pickLowestF(ArrayList<Grid> openSet) {
        int temp = 0;
        for (i = 0; i < openSet.size(); i++) {
            if (openSet.get(i).getF() < openSet.get(temp).getF()) {
                temp = i;
            }
        }
        return temp;
    }

    private int calculateG(Orientation curOrientation, Orientation respectiveOrientation) {
            return MOVE_COST + getNumberOfTurnRequired(curOrientation, respectiveOrientation) * TURN_COST;
    }

    private int getNumberOfTurnRequired(Orientation curOrientation, Orientation respectiveOrientation) {
        int numOfTurn = Math.abs(curOrientation.ordinal() - respectiveOrientation.ordinal());
        return numOfTurn % 2;
    }

    private boolean isGoalNode(Grid grid) {
        return (grid.getRow() == GOAL_ZONE_ROW && grid.getCol() == GOAL_ZONE_COL);
    }

    private boolean isWayPoint(Grid grid) {
        return (grid.getRow() == Integer.parseInt(waypoint[0], 10) && grid.getCol() == Integer.parseInt(waypoint[1], 10));
    }
    
    private ArrayList<Grid> getNeighbouringGrids(int row, int col) {
        ArrayList<Grid> neighbourArrList = new ArrayList<>();
        // northNeighbour
        if (canBeVisited(row - 1, col)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row - 1, col));
        }
        // SouthNeighbour
        if (canBeVisited(row + 1, col)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row + 1, col));
        }
        // WestNeighbour
        if (canBeVisited(row, col - 1)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row, col - 1));
        }
        // EastNeighbour
        if (canBeVisited(row, col + 1)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row, col + 1));
        }
        return neighbourArrList;
    }

    /*
     * Used for checking whether the surround 3 by 3 grid can be visited.
     * It can be visited when all of the grid does not have obstacle and has been explored
    */
    private boolean canBeVisited(int centerR, int centerC) {
        Grid curGrid;
        for (int r = -1; r < 2; r++) {
            for (int c = -1; c < 2; c++) {
                curGrid = myRobot.getArena().getGrid(centerR + r, centerC + c);
                if (curGrid != null) {
                    if (!curGrid.hasBeenExplored() || curGrid.hasObstacle()) {
                        return false;
                    }
                    if (r == 0 && c == 0) {
                        if (curGrid.isVirtualWall()) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private int calculateHeuristic(Grid grid, int col, int row) {
    	int minNumOfGridAwayFromGoal;
    	
    	minNumOfGridAwayFromGoal = Math.abs(col - grid.getCol()) + Math.abs(row - grid.getRow());
    	
        if (gridNotInSameAxisAsGoal(grid, col ,row)) {
            return minNumOfGridAwayFromGoal * MOVE_COST + TURN_COST;
        }
        return minNumOfGridAwayFromGoal * MOVE_COST;
    }

    private boolean gridNotInSameAxisAsGoal(Grid grid, int col, int row) {
    		return col - grid.getCol() != 0 || row - grid.getRow() != 0;
    }

    private Stack<Grid> getFastestPath(int col, int row) {
    	Grid curGrid;
        Stack<Grid> path = new Stack();
       
        curGrid = myRobot.getArena().getGrid(row, col);
        
        Grid prevGrid;
        path.push(curGrid);
        while (curGrid.getCameFrom() != null) {
            prevGrid = curGrid.getCameFrom();
            path.push(prevGrid);
            curGrid = curGrid.getCameFrom();
        }
        // pop away the starting grid
        path.pop();
        return path;
    }

    private void executeFastestPath(Stack<Grid> s) throws  Exception{
        Grid targetGrid;
        Orientation orientationNeeded;
        while (!s.empty()) {
            targetGrid = s.pop();
            orientationNeeded = getRespectiveOrientationToTarget(myRobot.getCurRow(), myRobot.getCurCol(), targetGrid.getRow(), targetGrid.getCol());
            turnRobot(orientationNeeded);
            sim.forward();
        }
    }
    
    private void instruction(Stack<Grid> s , boolean stop) throws  Exception{
    	//int numberOfForward=0;
        int modulus=0;
        Grid targetGrid;
        Orientation orientationNeeded;
        
        while (!s.empty()) {
            targetGrid = s.pop();
            orientationNeeded = getRespectiveOrientationToTarget(myRobot.getCurRow(), myRobot.getCurCol(), targetGrid.getRow(), targetGrid.getCol());;
            modulus = calculateModulus(orientationNeeded);

            if (orientationNeeded == myRobot.getCurOrientation()) {
            	numberOfForward++;
            } else if (modulus == 1) {
            	sim.virtualRight();
            	instruction += numberOfForward + "R";
            	numberOfForward = 1;
            } else if (modulus == 3) {
            	sim.virtualLeft();
            	instruction += numberOfForward + "L";
            	numberOfForward = 1;
            } else {
            	sim.virtualRight();
            	sim.virtualRight();
            	instruction += numberOfForward + "2R";
            	numberOfForward = 1;
            }
            sim.virtualForward();
            if(numberOfForward == 9) {
	        	instruction += numberOfForward;
	        	numberOfForward = 0;
          }
        }
        if(stop && numberOfForward != 0)
        	instruction += numberOfForward;;
        
    }

    private Orientation getRespectiveOrientationToTarget(int curR, int curC, int targetR, int targetC) {
        if (curR == targetR && targetC > curC) {
            return Orientation.E;
        } else if (curR == targetR && targetC < curC) {
            return Orientation.W;
        } else if (curC == targetC && targetR > curR) {
            return Orientation.S;
        } else if (curC == targetC && targetR < curR) {
            return Orientation.N;
        }
        return null;
    }
    
    private int calculateModulus(Orientation targetOrientation) throws Exception {
        Orientation curOrientation = myRobot.getCurOrientation();

        int modulus;
        modulus = (targetOrientation.ordinal() - curOrientation.ordinal()) % 4;
        if (modulus < 0) {
            modulus += 4;
        }
        return modulus;
    }
    // turn myRobot to targetOrientation
    private void turnRobot(Orientation targetOrientation) throws Exception {
        Orientation curOrientation = myRobot.getCurOrientation();

        int modulus = calculateModulus(targetOrientation);

        if (targetOrientation == curOrientation) {
            return;
        } else if (modulus == 1) {
            sim.right();
        } else if (modulus == 3) {
            sim.left();
        } else {
            sim.right();
            sim.right();
        }
    }
    
    private String[] parseInputToRowColArr(String s) {
        return s.split("\\s*,\\s*");
    }
}
