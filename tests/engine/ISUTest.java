package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.core.Game;
import engine.core.Grid;
import engine.core.ISU;
import game.PacManGame;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ISU and its inner classes:
 *   ISU.Coord, ISU.Dimension, ISU.Vector
 *
 * The Game is 10×10 cells with cmPerCell = 3.7,
 * so widthCm = heightCm = 37.0, both axes on torus.
 */
@DisplayName("ISU")
public class ISUTest {

    private static final double EPS = 1e-9;

    private Game game;
    private ISU  isu;

    @BeforeEach
    void setUp() {
        game = new PacManGame(10, 10);
        isu  = game.isu();
    }

    // =========================================================
    // == ISU.Dimension
    // =========================================================

    @Test
    @DisplayName("Dimension stores x and y correctly")
    void dimension_getters() {
        ISU.Dimension d = isu.new Dimension(2.0, 3.0);
        assertEquals(2.0, d.x(), EPS);
        assertEquals(3.0, d.y(), EPS);
    }

    @Test
    @DisplayName("Dimension equals for same values")
    void dimension_equals_same() {
        ISU.Dimension a = isu.new Dimension(1.5, 2.5);
        ISU.Dimension b = isu.new Dimension(1.5, 2.5);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Dimension not equal for different values")
    void dimension_notEqual() {
        ISU.Dimension a = isu.new Dimension(1.5, 2.5);
        ISU.Dimension b = isu.new Dimension(1.5, 3.0);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Dimension mkScaledVector(factor) scales both components")
    void dimension_mkScaledVector_uniform() {
        ISU.Dimension d = isu.new Dimension(2.0, 4.0);
        ISU.Vector    v = d.mkScaledVector(3.0);
        assertEquals(6.0, v.x(), EPS);
        assertEquals(12.0, v.y(), EPS);
    }

    @Test
    @DisplayName("Dimension mkScaledVector(xF, yF) scales each component independently")
    void dimension_mkScaledVector_xy() {
        ISU.Dimension d = isu.new Dimension(2.0, 4.0);
        ISU.Vector    v = d.mkScaledVector(2.0, 0.5);
        assertEquals(4.0, v.x(), EPS);
        assertEquals(2.0, v.y(), EPS);
    }

    @Test
    @DisplayName("Dimension mkVector returns vector with same components")
    void dimension_mkVector() {
        ISU.Dimension d = isu.new Dimension(3.0, 5.0);
        ISU.Vector    v = d.mkVector();
        assertEquals(3.0, v.x(), EPS);
        assertEquals(5.0, v.y(), EPS);
    }

    // =========================================================
    // == ISU.Vector
    // =========================================================

    @Test
    @DisplayName("Vector stores x and y correctly")
    void vector_getters() {
        ISU.Vector v = isu.new Vector(1.5, -2.5);
        assertEquals(1.5,  v.x(), EPS);
        assertEquals(-2.5, v.y(), EPS);
    }

    @Test
    @DisplayName("Vector add accumulates components")
    void vector_add() {
        ISU.Vector a = isu.new Vector(1.0, 2.0);
        ISU.Vector b = isu.new Vector(3.0, 4.0);
        a.add(b);
        assertEquals(4.0, a.x(), EPS);
        assertEquals(6.0, a.y(), EPS);
    }

    @Test
    @DisplayName("Vector scale(factor) scales both components")
    void vector_scale_uniform() {
        ISU.Vector v = isu.new Vector(3.0, 4.0);
        v.scale(2.0);
        assertEquals(6.0, v.x(), EPS);
        assertEquals(8.0, v.y(), EPS);
    }

    @Test
    @DisplayName("Vector scale(xF, yF) scales each component independently")
    void vector_scale_xy() {
        ISU.Vector v = isu.new Vector(3.0, 4.0);
        v.scale(2.0, 0.5);
        assertEquals(6.0, v.x(), EPS);
        assertEquals(2.0, v.y(), EPS);
    }

    @Test
    @DisplayName("Vector dot product is correct")
    void vector_dot() {
        ISU.Vector a = isu.new Vector(1.0, 2.0);
        ISU.Vector b = isu.new Vector(3.0, 4.0);
        assertEquals(11.0, a.dot(b), EPS); // 1*3 + 2*4 = 11
    }

    @Test
    @DisplayName("Vector norm of (3,4) is 5")
    void vector_norm() {
        ISU.Vector v = isu.new Vector(3.0, 4.0);
        assertEquals(5.0, v.norm(), EPS);
    }

    @Test
    @DisplayName("Vector unity makes norm equal to 1")
    void vector_unity() {
        ISU.Vector v = isu.new Vector(3.0, 4.0);
        v.unity();
        assertEquals(1.0, v.norm(), 1e-6);
    }

    @Test
    @DisplayName("Vector turn by 90° rotates correctly")
    void vector_turn_90() {
        ISU.Vector v = isu.new Vector(1.0, 0.0);
        v.turn(90);
        assertEquals(0.0, v.x(), 1e-9);
        assertEquals(1.0, v.y(), 1e-9);
    }

    @Test
    @DisplayName("Vector turn by 0° is identity")
    void vector_turn_0() {
        ISU.Vector v = isu.new Vector(2.0, 3.0);
        v.turn(0);
        assertEquals(2.0, v.x(), EPS);
        assertEquals(3.0, v.y(), EPS);
    }

    @Test
    @DisplayName("Vector turn by 360° is identity")
    void vector_turn_360() {
        ISU.Vector v = isu.new Vector(2.0, 3.0);
        v.turn(360);
        assertEquals(2.0, v.x(), 1e-9);
        assertEquals(3.0, v.y(), 1e-9);
    }

    // =========================================================
    // == ISU.Coord
    // =========================================================

    @Test
    @DisplayName("Coord stores x and y correctly (in-range)")
    void coord_getters() {
        ISU.Coord c = isu.new Coord(1.0, 2.0);
        assertEquals(1.0, c.x(), EPS);
        assertEquals(2.0, c.y(), EPS);
    }

    @Test
    @DisplayName("Coord equals for same values")
    void coord_equals() {
        ISU.Coord a = isu.new Coord(2.0, 3.0);
        ISU.Coord b = isu.new Coord(2.0, 3.0);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Coord mkCopy is equal but different reference")
    void coord_mkCopy() {
        ISU.Coord c    = isu.new Coord(1.0, 2.0);
        ISU.Coord copy = c.mkCopy();
        assertEquals(c, copy);
        assertNotSame(c, copy);
    }

    @Test
    @DisplayName("Coord translate moves by vector")
    void coord_translate() {
        ISU.Coord  c = isu.new Coord(1.0, 2.0);
        ISU.Vector v = isu.new Vector(0.5, 1.5);
        c.translate(v);
        assertEquals(1.5, c.x(), EPS);
        assertEquals(3.5, c.y(), EPS);
    }

    @Test
    @DisplayName("Coord mkTranslated does not modify original")
    void coord_mkTranslated_doesNotModifyOriginal() {
        ISU.Coord  c = isu.new Coord(1.0, 2.0);
        ISU.Vector v = isu.new Vector(1.0, 1.0);
        ISU.Coord  t = c.mkTranslated(v);
        assertEquals(1.0, c.x(), EPS); // original unchanged
        assertEquals(2.0, c.x() + 1.0, EPS);
        assertEquals(2.0, t.x(), EPS);
        assertEquals(3.0, t.y(), EPS);
    }

    @Test
    @DisplayName("Coord distanceTo same point is 0")
    void coord_distanceTo_zero() {
        ISU.Coord c = isu.new Coord(3.0, 4.0);
        assertEquals(0.0, c.distanceTo(isu.new Coord(3.0, 4.0)), EPS);
    }

    @Test
    @DisplayName("Coord distanceTo (3,4) to origin is 5")
    void coord_distanceTo_pythagorean() {
        ISU.Coord origin = isu.new Coord(0.0, 0.0);
        ISU.Coord target = isu.new Coord(3.0, 4.0);
        assertEquals(5.0, origin.distanceTo(target), EPS);
    }

    @Test
    @DisplayName("Coord mkVectorToward points in correct direction")
    void coord_mkVectorToward() {
        ISU.Coord from   = isu.new Coord(0.0, 0.0);
        ISU.Coord target = isu.new Coord(3.0, 0.0);
        ISU.Vector v     = from.mkVectorToward(target);
        assertEquals(3.0, v.x(), EPS);
        assertEquals(0.0, v.y(), EPS);
    }

    @Test
    @DisplayName("Coord rotation by 90° around origin gives correct result")
    void coord_rotation_90() {
        ISU.Coord c = isu.new Coord(3.0, 0.0);
        c.rotation(90);
        assertEquals(0.0, c.x(), 1e-9);
        assertEquals(3.0, c.y(), 1e-9);
    }

    @Test
    @DisplayName("Coord rotateAround center by 90°")
    void coord_rotateAround_90() {
        ISU.Coord center = isu.new Coord(1.0, 1.0);
        ISU.Coord c      = isu.new Coord(4.0, 1.0); // 3 units right of center
        c.rotateAround(center, 90);
        assertEquals(1.0, c.x(), 1e-9);
        assertEquals(4.0, c.y(), 1e-9);
    }

    @Test
    @DisplayName("Coord toGridPosition converts cm to nearest cell")
    void coord_toGridPosition() {
        double factor = game.cmPerCell();
        ISU.Coord c = isu.new Coord((2 + 0.5) * factor, (3 + 0.5) * factor);
        Grid.Position pos = c.toGridPosition();
        assertEquals(2, pos.x());
        assertEquals(3, pos.y());
    }
}