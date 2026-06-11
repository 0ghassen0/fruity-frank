package engine;

import engine.collision.Circle;
import engine.core.Game;
import engine.core.ISU;
import game.PacManGame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class CircleTest {


    // =========================================================
    // 1. INTERSECTION SIMPLE (overlap)
    // =========================================================
   
	@Test
	public void testIntersectionOverlap() {

	    Game game = new PacManGame(1000, 1000);
	    ISU isu = new ISU(game);

	    Circle c1 = new Circle(isu.new Coord(0, 0), 2);
	    Circle c2 = new Circle(isu.new Coord(1, 0), 2);

	    assertTrue(c1.intersects(c2));
	}

}