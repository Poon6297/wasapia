package utils;

import controllers.SimulatorController;
import models.Grid;
import models.MyRobot;

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

    public FastestPathAlgorithm(MyRobot myRobot, SimulatorController sim) {
        this.myRobot = myRobot;
        this.sim = sim;
    }

    public void A_Star(int startRow, int startCol) throws Exception{
        Grid startingGrid = myRobot.getArena().getGrid(startRow, startCol);
        closedSet = new ArrayList<>();
        openSet = new ArrayList<>();
        openSet.add(startingGrid);

        startingGrid.setG(0);
        boolean searchCompletedFlag = false;
        Grid curGrid;
        ArrayList<Grid> curNeighbouringGridArrList;

        while (openSet.size() > 0 && !searchCompletedFlag) {
            int indexOfNodeWithLowestF = 0;

            for (i = 0; i < openSet.size(); i++) {
                if (openSet.get(i).getF() < openSet.get(indexOfNodeWithLowestF).getF()) {
                     indexOfNodeWithLowestF = i;
                }
            }

            curGrid = openSet.get(indexOfNodeWithLowestF);

            if (isGoalNode(curGrid)) {
                searchCompletedFlag = true;
            }

            openSet.remove(curGrid);
            closedSet.add(curGrid);

            curNeighbouringGridArrList = getNeighbouringGrids(curGrid.getRow(), curGrid.getCol());

            Grid curNeighbour;
            int tempG;
            for (i = 0; i < curNeighbouringGridArrList.size(); i++) {
                curNeighbour = curNeighbouringGridArrList.get(i);
                if (!closedSet.contains(curNeighbour)) {
                    tempG = curGrid.getG() + 1;
                    if (openSet.contains(curNeighbour)) {
                        if (tempG < curNeighbour.getG()) {
                            curNeighbour.setG(tempG);
                        }
                    } else {
                        curNeighbour.setG(tempG);
                        openSet.add(curNeighbour);
                    }
                    curNeighbour.setH(calculateHeuristic(curNeighbour));
                    curNeighbour.setCameFrom(curGrid);
                }
            }
        }
        path = getFastestPath();
        executeFastestPath(path);
    }


    private boolean isGoalNode(Grid grid) {
        return (grid.getRow() == GOAL_ZONE_ROW && grid.getCol() == GOAL_ZONE_COL);
    }

    private ArrayList<Grid> getNeighbouringGrids(int row, int col) {

        ArrayList<Grid> neighbourArrList = new ArrayList<>();
        if (canBeVisited(row - 1, col)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row - 1, col));
        }
        if (canBeVisited(row + 1, col)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row + 1, col));
        }
        if (canBeVisited(row, col - 1)) {
            neighbourArrList.add(myRobot.getArena().getGrid(row, col - 1));
        }
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

    private int calculateHeuristic(Grid grid) {
        int minNumOfGridAwayFromGoal = Math.abs(GOAL_ZONE_COL - grid.getCol()) + Math.abs(GOAL_ZONE_ROW - grid.getRow());
        if (gridNotInSameAxisAsGoal(grid)) {
            return minNumOfGridAwayFromGoal * MOVE_COST + TURN_COST;
        }
        return minNumOfGridAwayFromGoal * MOVE_COST;
    }

    private boolean gridNotInSameAxisAsGoal(Grid grid) {
        return GOAL_ZONE_COL - grid.getCol() != 0 || GOAL_ZONE_ROW - grid.getRow() != 0;
    }

    private Stack<Grid> getFastestPath() {
        Stack<Grid> path = new Stack();
        Grid curGrid = myRobot.getArena().getGrid(GOAL_ZONE_ROW, GOAL_ZONE_COL);
        Grid prevGrid;

        path.push(curGrid);
        while (curGrid.getCameFrom() != null) {
            prevGrid = curGrid.getCameFrom();
            path.push(prevGrid);
            curGrid = curGrid.getCameFrom();
        }
        return path;
    }

    private void executeFastestPath(Stack<Grid> s) throws  Exception{
        Grid curGrid;
        Orientation orientationNeeded;
        while (!s.empty()) {
            curGrid = s.pop();
            orientationNeeded = getTargetOrientationRespectiveToRobot(myRobot.getCurRow(), myRobot.getCurCol(), curGrid.getRow(), curGrid.getCol());
            setRobotOrientation(orientationNeeded);
            sim.forward();
        }
    }

    private Orientation getTargetOrientationRespectiveToRobot(int curR, int curC, int targetR, int targetC) {
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

    private void setRobotOrientation(Orientation o) throws Exception {
        Orientation curOrientation = myRobot.getCurOrientation();
        if (o == Orientation.N) {
            if (curOrientation == Orientation.N) {
            } else if (curOrientation == Orientation.E) {
                sim.left();
            } else if (curOrientation == Orientation.S) {
                System.out.println("THIS SHOULD'NT HAPPEN");
                sim.right();
                sim.right();
            } else if (curOrientation == Orientation.W) {
                sim.right();
            }
        } else if (o == Orientation.E) {
            if (curOrientation == Orientation.N) {
                sim.right();
            } else if (curOrientation == Orientation.E) {
            } else if (curOrientation == Orientation.S) {
                sim.left();
            } else if (curOrientation == Orientation.W) {
                System.out.println("THIS SHOULD'NT HAPPEN");
                sim.right();
                sim.right();
            }
        } else if (o == Orientation.S) {
            if (curOrientation == Orientation.N) {
                System.out.println("THIS SHOULD'NT HAPPEN");
                sim.right();
                sim.right();
            } else if (curOrientation == Orientation.E) {
                sim.right();
            } else if (curOrientation == Orientation.S) {
            } else if (curOrientation == Orientation.W) {
                sim.left();
            }
        } else if (o == Orientation.W) {
            if (curOrientation == Orientation.N) {
                sim.left();
            } else if (curOrientation == Orientation.E) {
                System.out.println("THIS SHOULD'NT HAPPEN");
                sim.right();
                sim.right();
            } else if (curOrientation == Orientation.S) {
                sim.right();
            } else if (curOrientation == Orientation.W) {
            }
        }
    }
}
