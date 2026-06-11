package engine;

import org.junit.jupiter.api.Test;

import engine.collision.Bounding;
import engine.collision.Circle;
import engine.collision.Rect;
import game.TestGame;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ShapeCollisionTest {
	private final TestGame game = new TestGame();

	@Test
	@DisplayName("circle circle overlap")
	public void circleCircleOverlap() {
		Circle c1 = new Circle(game.isu().new Coord(10, 10), 3);
		Circle c2 = new Circle(game.isu().new Coord(12, 10), 3);

		assertTrue(c1.intersects(c2));
		assertTrue(c2.intersects(c1));
	}

	@Test
	@DisplayName("circle circle no overlap")

	public void circleCircleNoOverlap() {
		Circle c1 = new Circle(game.isu().new Coord(0, 0), 1);
		Circle c2 = new Circle(game.isu().new Coord(10, 10), 1);

		assertFalse(c1.intersects(c2));
		assertFalse(c2.intersects(c1));

	}

	@Test
	@DisplayName("rectangle circle overlap")
	public void rectCircleOverlap() {
		Rect r = new Rect(game.isu().new Coord(10, 10), game.isu().new Dimension(6, 4), 0, game.isu());

		Circle c = new Circle(game.isu().new Coord(11, 10), 1);

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));

	}

	@Test
	@DisplayName("rectangle circle no overlap")
	public void rectCircleNoOverlap() {
		Rect r = new Rect(game.isu().new Coord(10, 10), game.isu().new Dimension(4, 4), 0, game.isu());

		Circle c = new Circle(game.isu().new Coord(20, 20), 1);

		assertFalse(r.intersects(c));
		assertFalse(c.intersects(r));

	}

	@Test
	@DisplayName("rectangle rectangle overlap")
	public void rectRectOverlap() {
		Rect r1 = new Rect(game.isu().new Coord(10, 10), game.isu().new Dimension(6, 4), 0, game.isu());
		Rect r2 = new Rect(game.isu().new Coord(12, 10), game.isu().new Dimension(6, 4), 0, game.isu());

		assertTrue(r1.intersects(r2));
		assertTrue(r2.intersects(r1));
	}

	@Test
	@DisplayName("rectangle rectangle no overlap")
	public void rectRectNoOverlap() {
		Rect r1 = new Rect(game.isu().new Coord(0, 0), game.isu().new Dimension(4, 4), 0, game.isu());
		Rect r2 = new Rect(game.isu().new Coord(20, 20), game.isu().new Dimension(4, 4), 0, game.isu());

		assertFalse(r1.intersects(r2));
		assertFalse(r2.intersects(r1));
	}
	
	@Test
	@DisplayName("testing collision between two boundings")
    public void boundingUnionWorks() {
        Bounding b1 = new Bounding();
        Bounding b2 = new Bounding();

        b1.add(new Circle(game.isu().new Coord(5, 5), 2));
        b1.add(new Rect(game.isu().new Coord(20, 20), game.isu().new Dimension(4, 4), 0, game.isu()));

        b2.add(new Circle(game.isu().new Coord(6, 5), 2));
        b2.add(new Rect(game.isu().new Coord(10, 20), game.isu().new Dimension(2, 4), 0, game.isu()));

        assertTrue(b1.intersects(b2));
        assertTrue(b2.intersects(b1));
    }

}