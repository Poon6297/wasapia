package models;

import java.awt.Color;

public class Constants {
    public static final int ARENA_WIDTH = 15;
    public static final int ARENA_HEIGHT = 20;
    public static final int ZONE_SIZE = 3;
    public static final int GRID_SIZE = 25;
    public static final Color OBSTACLE_COLOR = Color.black;
    public static final Color MAP_COLOR = Color.lightGray;
    public static final Color ARENA_DIVIDER_LINE_COLOR = Color.BLUE;
    public static final int ARENA_DIVIDER_LINE_THICKNESS = 4;
    public static final Color ARENA_GRID_LINE_COLOR = Color.darkGray;
    public static final Color GOAL_ZONE_COLOR = Color.MAGENTA;
    public static final Color START_ZONE_COLOR = Color.MAGENTA;
    public static final Color ORIENTATION_MARKER_COLOR = Color.MAGENTA;
    public static final String ARENA_DESCRIPTOR_PATH = System.getProperty("user.dir") + "/local_storage/mock_arena.txt";


    public enum My_Robot_Instruction {
        FORWARD,
        TURN_LEFT,
        TURN_RIGHT,
    };

    public enum Orientation {
        N,
        E,
        S,
        W,
    }
}