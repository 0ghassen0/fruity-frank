package game;

import org.junit.jupiter.api.Test;

import engine.core.Game;
import game.PacManGame;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Game.
 *
 * Game has two constructors:
 *   - Game(int w_ncell, int h_ncell)
 *   - Game(double w_cm, double h_cm)
 *
 * cmPerCell = 3.7 (constant), pixelPerCm = 2.
 * Both axes are on torus.
 */
@DisplayName("Game")
public class GameTest {

    private static final double EPS      = 1e-9;
    private static final double CM_CELL  = 3.7;

    // =========================================================
    // == Constructor (int, int)
    // =========================================================

    @Test
    @DisplayName("constructor(int,int) sets widthNcell and heightNcell")
    void constructorNcell_dimensions() {
        Game g = new PacManGame(10, 8);
        assertEquals(10, g.widthNcell());
        assertEquals(8,  g.heightNcell());
    }

    @Test
    @DisplayName("constructor(int,int) derives widthCm and heightCm correctly")
    void constructorNcell_cm() {
        Game g = new PacManGame(10, 8);
        assertEquals(10 * CM_CELL, g.widthCm(),  EPS);
        assertEquals(8  * CM_CELL, g.heightCm(), EPS);
    }

    @Test
    @DisplayName("constructor(int,int) initialises all subsystems")
    void constructorNcell_subsystems() {
        Game g = new PacManGame(5, 5);
        assertNotNull(g.grid());
        assertNotNull(g.isu());
    }

    // =========================================================
    // == Constructor (double, double)
    // =========================================================

    @Test
    @DisplayName("constructor(double,double) sets widthCm and heightCm")
    void constructorCm_cm() {
        double w = 10 * CM_CELL; // 37.0
        double h = 8  * CM_CELL; // 29.6
        Game g   = new PacManGame(w, h);
        assertEquals(w, g.widthCm(),  EPS);
        assertEquals(h, g.heightCm(), EPS);
    }

    @Test
    @DisplayName("constructor(double,double) derives ncell by rounding")
    void constructorCm_ncell() {
        Game g = new PacManGame(10 * CM_CELL, 8 * CM_CELL);
        assertEquals(10, g.widthNcell());
        assertEquals(8,  g.heightNcell());
    }

    @Test
    @DisplayName("constructor(double,double) initialises all subsystems")
    void constructorCm_subsystems() {
        Game g = new PacManGame(10 * CM_CELL, 5 * CM_CELL);
        assertNotNull(g.grid());
        assertNotNull(g.isu());
    }

    // =========================================================
    // == Constant getters
    // =========================================================

    @Test
    @DisplayName("cmPerCell returns 3.7")
    void cmPerCell() {
        Game g = new PacManGame(5, 5);
        assertEquals(3.7, g.cmPerCell(), EPS);
    }

    @Test
    @DisplayName("pixelPerCm returns 2")
    void pixelPerCm() {
        Game g = new PacManGame(5, 5);
        assertEquals(2, g.pixelPerCm());
    }

    @Test
    @DisplayName("torusOnXaxis returns true")
    void torusOnXaxis() {
        Game g = new PacManGame(5, 5);
        assertTrue(g.torusOnXaxis());
    }

    @Test
    @DisplayName("torusOnYaxis returns true")
    void torusOnYaxis() {
        Game g = new PacManGame(5, 5);
        assertTrue(g.torusOnYaxis());
    }

    // =========================================================
    // == Subsystem consistency
    // =========================================================

    @Test
    @DisplayName("grid dimensions match game ncell dimensions")
    void gridMatchesGame() {
        Game g = new PacManGame(7, 4);
        assertEquals(g.widthNcell(),  g.grid().width());
        assertEquals(g.heightNcell(), g.grid().height());
    }

    @Test
    @DisplayName("two Game instances are independent")
    void twoGamesAreIndependent() {
        Game g1 = new PacManGame(5, 5);
        Game g2 = new PacManGame(10, 20);
        assertNotSame(g1.grid(), g2.grid());
        assertEquals(5,  g1.widthNcell());
        assertEquals(10, g2.widthNcell());
    }
}