package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.core.Game;
import engine.core.Grid;
import engine.core.ISU;
import engine.entity.Entity;
import game.PacManGame;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Grid and its inner classes:
 *   Grid.Position, Grid.Vector, Grid.Dimension, Grid.Cell
 *
 * The Game is constructed with torusOnX=true, torusOnY=true, 10×10 cells.
 * cellSizeCm = 3.7  (from Game constant)
 */
@DisplayName("Grid")
public class GridTest {

    private static final double EPS = 1e-9;

    private Game game;
    private Grid grid;

    @BeforeEach
    void setUp() {
        // 10 × 10 torus grid, cmPerCell = 3.7
        game = new PacManGame(10, 10);
        grid = game.grid();
    }

    // =========================================================
    // == Grid (top-level)
    // =========================================================

    @Test
    @DisplayName("grid dimensions match constructor arguments")
    void grid_dimensions() {
        assertEquals(10, grid.width());
        assertEquals(10, grid.height());
    }

    @Test
    @DisplayName("cellAt returns non-null cell for every valid position")
    void grid_cellAt_valid() {
        for (int x = 0; x < grid.width(); x++) {
            for (int y = 0; y < grid.height(); y++) {
                assertNotNull(grid.cellAt(grid.new Position(x, y)));
            }
        }
    }

    @Test
    @DisplayName("cellAt wraps out-of-bounds position on torus")
    void grid_cellAt_torusWrap() {
        // Position(10,10) on a 10×10 torus wraps to (0,0)
        Grid.Cell wrapped = grid.cellAt(grid.new Position(10, 10));
        Grid.Cell origin  = grid.cellAt(grid.new Position(0, 0));
        assertEquals(origin.position(), wrapped.position());
    }

    // =========================================================
    // == Grid.Position
    // =========================================================

    @Test
    @DisplayName("Position constructor normalizes coordinates on torus")
    void position_constructor_normalizesTorus() {
        // (10,10) on a 10×10 grid wraps to (0,0)
        Grid.Position p = grid.new Position(10, 10);
        assertEquals(0, p.x());
        assertEquals(0, p.y());
    }

    @Test
    @DisplayName("Position copy is equal but not same reference")
    void position_copy() {
        Grid.Position p    = grid.new Position(3, 4);
        Grid.Position copy = p.copy();
        assertEquals(p, copy);
        assertNotSame(p, copy);
    }

    @Test
    @DisplayName("Position equals reflexive, symmetric")
    void position_equals() {
        Grid.Position a = grid.new Position(2, 5);
        Grid.Position b = grid.new Position(2, 5);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    @DisplayName("Position not equal for different coordinates")
    void position_notEqual() {
        Grid.Position a = grid.new Position(1, 2);
        Grid.Position b = grid.new Position(1, 3);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Position translate moves by vector and wraps on torus")
    void position_translate() {
        Grid.Position p = grid.new Position(8, 8);
        p.translate(grid.new Vector(3, 3)); 
        assertEquals(1, p.x());
        assertEquals(1, p.y());
    }

    @Test
    @DisplayName("Position moveNorth decrements y and wraps on torus")
    void position_moveNorth() {
        Grid.Position p = grid.new Position(5, 0);
        p.moveNorth(1); 
        assertEquals(9, p.y());
    }

    @Test
    @DisplayName("Position rotateAround 90° gives correct result")
    void position_rotateAround_90() {
        // Rotate (3,0) around (0,0) by 90°: result should be (0,3)
        Grid.Position center = grid.new Position(0, 0);
        Grid.Position p      = grid.new Position(3, 0);
        p.rotateAround(center, 90);
        assertEquals(0, p.x());
        assertEquals(3, p.y());
    }

    @Test
    @DisplayName("Position rotateAround 0° is identity")
    void position_rotateAround_0() {
        Grid.Position center = grid.new Position(1, 1);
        Grid.Position p      = grid.new Position(4, 2);
        p.rotateAround(center, 0);
        assertEquals(4, p.x());
        assertEquals(2, p.y());
    }

    @Test
    @DisplayName("Position distanceTo same point is 0")
    void position_distanceTo_zero() {
        Grid.Position p = grid.new Position(3, 3);
        assertEquals(0.0, p.distanceTo(grid.new Position(3, 3)), EPS);
    }

    @Test
    @DisplayName("Position distanceTo adjacent cell is 1")
    void position_distanceTo_adjacent() {
        Grid.Position p = grid.new Position(0, 0);
        Grid.Position q = grid.new Position(1, 0);
        assertEquals(1.0, p.distanceTo(q), EPS);
    }

    @Test
    @DisplayName("Position distanceTo uses torus shortest path")
    void position_distanceTo_torusShortestPath() {
        // On 10x10 torus: distance from (0,0) to (9,0)
        // Direct = 9, short path = 1
        Grid.Position p = grid.new Position(0, 0);
        Grid.Position q = grid.new Position(9, 0);
        assertEquals(1.0, p.distanceTo(q), EPS);
    }

    @Test
    @DisplayName("toISUCoord returns top-left corner of cell in cm")
    void position_toISUCoord() {
        Grid.Position p   = grid.new Position(1, 1);
        ISU.Coord     isu = p.toISUCoord();
        double factor     = game.cmPerCell(); // 3.7
        // top-left = x*factor - factor/2
        assertEquals(factor - factor / 2.0, isu.x(), EPS);
        assertEquals(factor - factor / 2.0, isu.y(), EPS);
    }

    @Test
    @DisplayName("toISUCoordCentered returns center of cell in cm")
    void position_toISUCoordCentered() {
        Grid.Position p = grid.new Position(2, 3);
        ISU.Coord c = p.toISUCoordCentered();
        assertEquals((2 + 0.5) * game.cmPerCell(), c.x(), 1e-9);
        assertEquals((3 + 0.5) * game.cmPerCell(), c.y(), 1e-9);
    }

    // =========================================================
    // == Grid.Vector
    // =========================================================

    @Test
    @DisplayName("Vector getters return constructed values")
    void vector_getters() {
        Grid.Vector v = grid.new Vector(3, -2);
        assertEquals(3,  v.x());
        assertEquals(-2, v.y());
    }

    @Test
    @DisplayName("Vector add accumulates components")
    void vector_add() {
        Grid.Vector a = grid.new Vector(1, 2);
        Grid.Vector b = grid.new Vector(3, 4);
        a.add(b);
        assertEquals(4, a.x());
        assertEquals(6, a.y());
    }

    // =========================================================
    // == Grid.Dimension
    // =========================================================

    @Test
    @DisplayName("Dimension getters return constructed values (in-range)")
    void dimension_getters() {
        Grid.Dimension d = grid.new Dimension(3, 4);
        assertEquals(3, d.x());
        assertEquals(4, d.y());
    }

    @Test
    @DisplayName("Dimension equals is true for same values")
    void dimension_equals() {
        Grid.Dimension a = grid.new Dimension(2, 3);
        Grid.Dimension b = grid.new Dimension(2, 3);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Dimension toISUDimension scales by cellSizeCm")
    void dimension_toISUDimension() {
        Grid.Dimension d   = grid.new Dimension(2, 3);
        ISU.Dimension  isu = d.toISUDimension();
        double factor      = game.cmPerCell(); // 3.7
        assertEquals(2 * factor, isu.x(), EPS);
        assertEquals(3 * factor, isu.y(), EPS);
    }

    // =========================================================
    // == Grid.Cell
    // =========================================================

    @Test
    @DisplayName("new Cell starts with no entities")
    void cell_startsEmpty() {
        Grid.Cell cell = grid.cellAt(grid.new Position(0, 0));
        assertTrue(cell.entities().isEmpty());
    }

    @Test
    @DisplayName("Cell size is always 1x1")
    void cell_sizeIsOne() {
        Grid.Cell cell = grid.cellAt(grid.new Position(0, 0));
        assertEquals(1, cell.size().x());
        assertEquals(1, cell.size().y());
    }

    @Test
    @DisplayName("Cell position matches the requested position")
    void cell_position() {
        Grid.Position p    = grid.new Position(3, 7);
        Grid.Cell     cell = grid.cellAt(p);
        assertEquals(p, cell.position());
    }

    @Test
    @DisplayName("Cell add / contains / remove entity lifecycle")
    void cell_entityLifecycle() {
        Grid.Cell  cell   = grid.cellAt(grid.new Position(1, 1));
        Entity e = new Entity("test");

        cell.add(e);
        assertTrue(cell.contains(e));
        assertEquals(1, cell.entities().size());

        // Adding twice does not duplicate
        cell.add(e);
        assertEquals(1, cell.entities().size());

        cell.remove(e);
        assertFalse(cell.contains(e));
        assertTrue(cell.entities().isEmpty());
    }
    
    @Test
    @DisplayName("Conversion from GRID to ISU")
    void testToISUCoordCentered() {

    	Game game = new PacManGame(10, 10);
    	Grid grid = game.grid();

    	Grid.Position p = grid.new Position(2, 3);

    	ISU.Coord c = p.toISUCoordCentered();

    	assertEquals((2 + 0.5) * game.cmPerCell(), c.x(), 1e-9);
    	assertEquals((3 + 0.5) * game.cmPerCell(), c.y(), 1e-9);
    }
    
    @Test
    @DisplayName("Conversion from ISU to GRID")
    void testToGridPosition() {
        Game game = new PacManGame(10, 10);
        ISU isu = game.isu();
        double s = game.cmPerCell();
        ISU.Coord c = isu.new Coord(2.5 * s, 3.5 * s);
        Grid.Position p = c.toGridPosition();
        assertEquals(2, p.x());
        assertEquals(3, p.y());
    }
    
    @Test
    @DisplayName("Test Conversion/Reconversion")
    void testRoundTripConversion() {

    	Game game = new PacManGame(10, 10);
    	Grid grid = game.grid();

    	Grid.Position p1 = grid.new Position(4, 7);

    	ISU.Coord c = p1.toISUCoordCentered();

    	Grid.Position p2 = c.toGridPosition();

    	assertEquals(p1, p2);
    }
}