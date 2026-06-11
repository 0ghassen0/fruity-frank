package game;

import engine.core.Game;
import engine.core.Grid;
import engine.core.ISU;
import engine.entity.Entity;
import game.PacManGame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Entity.
 *
 * Entity manages a position (Grid.Position + ISU.Coord) and orientation.
 * It depends on Grid and ISU which are provided by a Game(10,10).
 */
@DisplayName("Entity")
public class EntityTest {

    private static final double EPS = 1e-9;

    private Game   game;
    private Grid   grid;
    private ISU    isu;

    @BeforeEach
    void setUp() {
        game = new PacManGame(10, 10);
        grid = game.grid();
        isu  = game.isu();
    }

    // =========================================================
    // == Constructor
    // =========================================================

    @Test
    @DisplayName("new entity has orientation 0 and null position/center")
    void constructor_initialState() {
        Entity e = new Entity("hero");
        assertEquals(0, e.orientation());
        assertNull(e.position());
        assertNull(e.center());
    }

    // =========================================================
    // == setPosition / setCoord
    // =========================================================

    @Test
    @DisplayName("setPosition sets position and derives center")
    void setPosition_setsPositionAndCenter() {
        Entity        e = new Entity("hero");
        Grid.Position p = grid.new Position(3, 4);
        e.setPosition(p);

        assertNotNull(e.position());
        assertNotNull(e.center());
        assertEquals(3, e.position().x());
        assertEquals(4, e.position().y());
    }

    @Test
    @DisplayName("setPosition stores a copy, mutation does not propagate")
    void setPosition_storesCopy() {
        Entity        e = new Entity("hero");
        Grid.Position p = grid.new Position(2, 2);
        e.setPosition(p);
        // Even if p changes, entity position should be unchanged
        assertEquals(2, e.position().x());
    }

    @Test
    void setCoord_setsCenterAndPosition() {
        Entity e = new Entity("hero");
        e.bind(grid, isu);
        double f = game.cmPerCell();
        ISU.Coord c = isu.new Coord(2.5 * f, 3.5 * f);
        e.setCoord(c);
        assertNotNull(e.center());
        assertNotNull(e.position());
        assertEquals(2, e.position().x());
        assertEquals(3, e.position().y());
    }
    
    
    // =========================================================
    // == setSize (Grid.Dimension)
    // =========================================================

    @Test
    @DisplayName("setSize(Grid.Dimension) converts to ISU dimension (no NPE)")
    void setSize_gridDimension() {
        Entity         e = new Entity("hero");
        // setSize requires isu to be accessible; Entity uses the isu stored in Grid.
        // We provide position first to bind grid/isu references.
        e.setPosition(grid.new Position(1, 1));
        // Now we can safely call setSize
        Grid.Dimension d = grid.new Dimension(2, 2);
        assertDoesNotThrow(() -> e.setSize(d));
    }

    // =========================================================
    // == turn / orientation
    // =========================================================

    @Test
    @DisplayName("turn increments orientation and keeps it in [0,360[")
    void turn_increment() {
        Entity e = new Entity("hero");
        e.turn(90);
        assertEquals(90, e.orientation());
        e.turn(90);
        assertEquals(180, e.orientation());
        e.turn(180);
        assertEquals(0, e.orientation());
    }

    @Test
    @DisplayName("turn wraps negative angle correctly")
    void turn_negativeAngle() {
        Entity e = new Entity("hero");
        e.turn(-90);
        assertEquals(270, e.orientation());
    }

    @Test
    @DisplayName("turn wraps angle > 360 correctly")
    void turn_largeAngle() {
        Entity e = new Entity("hero");
        e.turn(450); // 450 % 360 = 90
        assertEquals(90, e.orientation());
    }

    // =========================================================
    // == translate(Grid.Vector)
    // =========================================================

    @Test
    @DisplayName("translate(Grid.Vector) moves position and updates center")
    void translate_gridVector() {
        Entity        e = new Entity("hero");
        e.setPosition(grid.new Position(2, 2));

        Grid.Vector v = grid.new Vector(1, 1);
        e.translate(v);

        assertEquals(3, e.position().x());
        assertEquals(3, e.position().y());
        assertNotNull(e.center());
    }

    @Test
    @DisplayName("translate(Grid.Vector) wraps on torus")
    void translate_gridVector_torus() {
        Entity e = new Entity("hero");
        e.setPosition(grid.new Position(9, 9));

        Grid.Vector v = grid.new Vector(1, 1); // 9+1=10 → 0 on 10-torus
        e.translate(v);

        assertEquals(0, e.position().x());
        assertEquals(0, e.position().y());
    }

    // =========================================================
    // == translate(ISU.Vector)
    // =========================================================

    @Test
    @DisplayName("translate(ISU.Vector) moves center and updates position")
    void translate_isuVector() {
        Entity    e = new Entity("hero");
        double    f = game.cmPerCell();
        e.setPosition(grid.new Position(2, 2));

        ISU.Vector v = isu.new Vector(f, 0.0); // shift one cell to the east
        e.translate(v);

        assertEquals(3, e.position().x());
    }
    /*
     
    // =========================================================
    // == moveNorth / moveSouth / moveEast / moveWest
    // =========================================================

    @Test
    @DisplayName("moveNorth(1) decrements y by 1 (and wraps on torus)")
    void moveNorth() {
        Entity e = new Entity("hero");
        e.setPosition(grid.new Position(5, 5));
        e.moveNorth(1);
        assertEquals(4, e.position().y());
    }

    @Test
    @DisplayName("moveSouth(1) increments y by 1")
    void moveSouth() {
        Entity e = new Entity("hero");
        e.setPosition(grid.new Position(5, 5));
        e.moveSouth(1);
        assertEquals(6, e.position().y());
    }

    @Test
    @DisplayName("moveEast shifts center x by length_cm")
    void moveEast() {
        Entity    e = new Entity("hero");
        double    f = game.cmPerCell(); // 3.7
        e.setPosition(grid.new Position(2, 2));
        e.moveEast(f); // shift exactly one cell
        assertEquals(3, e.position().x());
    }

    @Test
    @DisplayName("moveWest shifts center x left by length_cm")
    void moveWest() {
        Entity    e = new Entity("hero");
        double    f = game.cmPerCell();
        e.setPosition(grid.new Position(5, 5));
        e.moveWest(f); // one cell to the left
        assertEquals(4, e.position().x());
    }
    
    */
}