package engine;

// test import
import org.junit.jupiter.api.Test;

import engine.core.Axis;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Axis class.
 *
 * Axis models an axis with optional torus topology.
 * - onTorus=true  : coordinates are wrapped in [-perimeter/2, perimeter/2[
 * - onTorus=false : coordinates are returned as-is
 */

@DisplayName("Axis")
public class AxisTest {

    // =========================================================
    // == Constructor & Getters
    // =========================================================

    @Test
    @DisplayName("constructor stores perimeter and halfPerimeter correctly")
    void constructor_storesFields() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(10.0, axis.perimeter(), 1e-9);
        assertEquals(5.0,  axis.halfPerimeter(), 1e-9);
        assertTrue(axis.onTorus());
    }

    @Test
    @DisplayName("constructor with onTorus=false stores flag correctly")
    void constructor_notTorus() {
        Axis axis = new Axis(false, 20.0);
        assertFalse(axis.onTorus());
        assertEquals(20.0, axis.perimeter(), 1e-9);
    }

    // =========================================================
    // == normalize(int) – onTorus=true
    // =========================================================

    @Test
    @DisplayName("normalize(int) returns 0 for 0 on torus")
    void normalizeInt_zero_onTorus() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(0, axis.normalize(0));
    }

    @Test
    @DisplayName("normalize(int) wraps positive overflow on torus")
    void normalizeInt_positiveOverflow_onTorus() {
        Axis axis = new Axis(true, 10.0);
        // 10 % 10 == 0
        assertEquals(0,  axis.normalize(10));
        // 13 % 10 == 3
        assertEquals(3,  axis.normalize(13));
        // 20 % 10 == 0
        assertEquals(0,  axis.normalize(20));
    }

    @Test
    @DisplayName("normalize(int) wraps negative values on torus")
    void normalizeInt_negative_onTorus() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(9, axis.normalize(-1));
        assertEquals(0, axis.normalize(-10));
        assertEquals(9, axis.normalize(-11));
    }

    @Test
    @DisplayName("normalize(int) is identity when NOT on torus")
    void normalizeInt_notTorus_identity() {
        Axis axis = new Axis(false, 10.0);
        assertEquals(15,  axis.normalize(15));
        assertEquals(-3,  axis.normalize(-3));
        assertEquals(0,   axis.normalize(0));
    }

    // =========================================================
    // == normalize(double) – onTorus=true
    // =========================================================

    @Test
    void normalizeDouble_inRange_onTorus() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(3.0, axis.normalize(3.0), 1e-9);
        assertEquals(7.0, axis.normalize(7.0), 1e-9);
    }

    @Test
    void normalizeDouble_positiveOverflow_onTorus() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(7.0, axis.normalize(7.0), 1e-9);
        assertEquals(0.0, axis.normalize(10.0), 1e-9);
        assertEquals(2.5, axis.normalize(12.5), 1e-9);
    }

    @Test
    void normalizeDouble_negativeOverflow_onTorus() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(3.0, axis.normalize(-7.0), 1e-9);
        assertEquals(0.0, axis.normalize(-10.0), 1e-9);
    }

    @Test
    void normalizeDouble_boundary_onTorus() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(5.0, axis.normalize(5.0), 1e-9);
        assertEquals(0.0, axis.normalize(10.0), 1e-9);
        assertEquals(0.0, axis.normalize(0.0), 1e-9);
    }

    @Test
    @DisplayName("normalize(double) is identity when NOT on torus")
    void normalizeDouble_notTorus_identity() {
        Axis axis = new Axis(false, 10.0);
        assertEquals(42.0,  axis.normalize(42.0), 1e-9);
        assertEquals(-42.0, axis.normalize(-42.0), 1e-9);
    }

    // =========================================================
    // == distance
    // =========================================================

    @Test
    @DisplayName("distance is absolute difference when NOT on torus")
    void distance_notTorus() {
        Axis axis = new Axis(false, 100.0);
        assertEquals(3.0, axis.distance(2.0, 5.0), 1e-9);
        assertEquals(3.0, axis.distance(5.0, 2.0), 1e-9);
    }

    @Test
    @DisplayName("distance on torus takes shortest path")
    void distance_onTorus_shortestPath() {
        Axis axis = new Axis(true, 10.0);
        assertEquals(1.0, Math.abs(axis.distance(-4.5, 4.5)), 1e-9);
        assertEquals(3.0, axis.distance(0.0, 3.0), 1e-9);
        assertEquals(-3.0, axis.distance(5.0, 2.0), 1e-9);
    }
    
    @Test
    @DisplayName("distance is 0 for identical positions")
    void distance_samePosition() {
        Axis axisTorus    = new Axis(true,  10.0);
        Axis axisNoTorus  = new Axis(false, 10.0);
        assertEquals(0.0, axisTorus.distance(3.0, 3.0), 1e-9);
        assertEquals(0.0, axisNoTorus.distance(3.0, 3.0), 1e-9);
    }
}