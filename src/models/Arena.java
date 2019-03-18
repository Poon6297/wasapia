package models;



import static models.Constants.*;

public class Arena {
    private Grid[][] grids = new Grid[ARENA_HEIGHT][ARENA_WIDTH];


    public Arena() {
        for (int r = 0; r < ARENA_HEIGHT; r++) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                this.grids[r][c] = new Grid(r, c);
                if (isVirtualWall(r, c)) {
                    this.grids[r][c].setIsVirtualWall(true);
                }
                if (isGoalZone(r, c) || isStartZone(r, c)) {
                    this.grids[r][c].setHasObstacle(false);
                    this.grids[r][c].setHasBeenExplored(true);
                }
            }
        }
    }

    // ============================== utils

    public static int getRowFromActualRow(int actualRow) {
        return 19 - actualRow;
    }

    public static int getActualRowFromRow(int row) {
        return 19 - row;
    }

    public double getCoveragePercentage() {
        int exploredGrids = 0;
        int totalGrids = ARENA_HEIGHT * ARENA_WIDTH;
        for (int r = 0; r < ARENA_HEIGHT; r++) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                if (getGrid(r, c).hasBeenExplored()) {
                    exploredGrids++;
                }
            }

        }
        return (double) exploredGrids/totalGrids*100;
    }

    public void setHasExploredBasedOnOccupiedGrid(MyRobot myRobot) {
        int row = myRobot.getCurRow();
        int col = myRobot.getCurCol();

        for (int r = -1; r < 2; r++) {
            for (int c = -1; c < 2; c++) {
                this.grids[row + r][col + c].setHasBeenExplored(true);
            }
        }
    }

    public String generateMapDescriptorP1() {
        String txt = "";
        String fourChar = "11";

        for (int r = ARENA_HEIGHT - 1; r > -1; r--) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                if(getGrid(r, c).hasBeenExplored())
                    fourChar += 1;
                else
                    fourChar += 0;

                if(fourChar.length() == 4) {
                    txt += Integer.toHexString(Integer.parseInt(fourChar, 2));
                    fourChar = "";
                }
            }
        }
        if(fourChar.length() == 3) {
            fourChar += "1";
            txt += Integer.toHexString(Integer.parseInt(fourChar, 2));
            fourChar = "";
            txt += Integer.toHexString(Integer.parseInt(fourChar, 2)); }
        else {
            fourChar += "11";
            txt += Integer.toHexString(Integer.parseInt(fourChar, 2)); }
        return txt;
    }

    public String generateMapDescriptorP2() {
        String txt = "";
        String fourChar = "";

        for (int r = ARENA_HEIGHT - 1; r > -1; r--) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                if(getGrid(r, c).hasBeenExplored()) {
                    if(getGrid(r,c).hasObstacle())
                        fourChar += 1;
                    else
                        fourChar += 0;
                    if(fourChar.length() == 4) {
                        txt += Integer.toHexString(Integer.parseInt(fourChar, 2));
                        fourChar = "";
                    }
                }
            }
        }

        if(fourChar.length() > 0)
            txt += Integer.toHexString(Integer.parseInt(fourChar, 2));
        return txt;
    }

    // generate a binary representation of the obstacles
    public String obstacleToString() {
        String descriptor = "";
        for (int r = 19; r > -1; r--) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                if (this.grids[r][c].hasObstacle()) {
                    descriptor += "1";
                    continue;
                }
                descriptor += "0";
            }
        }
        return descriptor;
    }

    // for parsing the binary representation generated by the obstacleToString()
    public void binStringToArena(String arenaDescriptor) {
        int curIndex = 0;
        for (int r = ARENA_HEIGHT - 1; r > -1; r--) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                if (arenaDescriptor.charAt(curIndex) == '0') {
                    this.grids[r][c].setHasObstacle(false);
                } else if (arenaDescriptor.charAt(curIndex) == '1') {
                    this.grids[r][c].setHasObstacle(true);
                }
                curIndex++;
            }
        }
    }

    public void clearObstacle() {
        for (int r = 0; r < ARENA_HEIGHT; r++) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                this.grids[r][c].setHasObstacle(false);
            }
        }
    }

    public void reinitializeArena(MyRobot myRobot) {
        for (int r = 0; r < ARENA_HEIGHT; r++) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                this.grids[r][c].setHasObstacle(false);
                this.grids[r][c].setHasBeenExplored(false);

                if (isGoalZone(r, c) || isStartZone(r, c)) {
                    this.grids[r][c].setHasObstacle(false);
                    this.grids[r][c].setHasBeenExplored(true);
                }
                this.grids[r][c].resetGridCostAndCameFrom();
                this.grids[r][c].setU(false);
                this.grids[r][c].setD(false);
                this.grids[r][c].setL(false);
                this.grids[r][c].setR(false);
            }
        }
        setHasExploredBasedOnOccupiedGrid(myRobot);
        myRobot.resetPathTaken();
    }
    
    public void resetGridCostAndCameFrom() {
        for (int r = 0; r < ARENA_HEIGHT; r++) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                this.grids[r][c].resetGridCostAndCameFrom();
            }
        }
    }

    public static boolean isGoalZone(int row, int col) {
        return (row < ZONE_SIZE) && (col > ARENA_WIDTH - 1 - ZONE_SIZE);
    }

    public static boolean isStartZone(int row, int col) {
        return (row > ARENA_HEIGHT - 1 - ZONE_SIZE) && (col < ZONE_SIZE);
    }

    public static boolean isValidRowCol(int row, int col) {
        return row < ARENA_HEIGHT && row >= 0 && col < ARENA_WIDTH && col >= 0;
    }

    private boolean isVirtualWall(int row, int col) {
        return row == 0 || row == ARENA_HEIGHT - 1 || col == 0 || col == ARENA_WIDTH - 1;
    }

    // ============================== getters & setters
    public Grid getGrid(int row, int col) {
        if (!isValidRowCol(row, col)) {
            return null;
        }
        return this.grids[row][col];
    }


    /*
    public void debugArena() {
        System.out.println("$$$$$$$$$$$Debug Arena$$$$$$$$$$$$");
        for (int r = 0; r < ARENA_HEIGHT; r++) {
            for (int c = 0; c < ARENA_WIDTH; c++) {
                if (grids[r][c].hasBeenExplored()) {
                    System.out.print("1 ");
                } else {
                    System.out.print("0 ");
                }
            }
            System.out.println();
        }
    }
    */
}