package engine.gal.arguments;

import java.util.HashMap;
import java.util.Map;

public class Direction {

    private static final Map<String, Direction> directions = new HashMap<>();

    public static final Direction B = new Direction("B");
    public static final Direction F = new Direction("F");
    public static final Direction H = new Direction("H");
    public static final Direction N = new Direction("N");
    public static final Direction S = new Direction("S");
    public static final Direction E = new Direction("E");
    public static final Direction W = new Direction("W");
    public static final Direction L = new Direction("L");
    public static final Direction R = new Direction("R");
    public static final Direction NE = new Direction("NE");
    public static final Direction NW = new Direction("NW");
    public static final Direction SE = new Direction("SE");
    public static final Direction SW = new Direction("SW");

    private final String name;

    private Direction(String name) {
        assert name != null;
        this.name = name;
        directions.put(name, this);
    }

    public static Direction canonical(String name) {
        assert name != null;
        Direction d = directions.get(name);
        assert d != null : "Unknown direction: " + name;
        return d;
    }

    public String name() { return name; }

    public boolean isAbsolute() {
        return this == N || this == S || this == E || this == W
            || this == NE || this == NW || this == SE || this == SW;
    }

    public boolean isRelative() {
        return this == F || this == B || this == L || this == R || this == H;
    }

    public int toAngle() {
        if (this == E)  return 0;
        if (this == S)  return 90;
        if (this == W)  return 180;
        if (this == N)  return 270;
        if (this == NE) return 45;
        if (this == SE) return 135;
        if (this == SW) return 225;
        if (this == NW) return 315;
        if (this == F)  return 0;
        if (this == R)  return 90;
        if (this == B)  return 180;
        if (this == L)  return 270;
        return 0;
    }

    @Override
    public String toString() { return name; }
}