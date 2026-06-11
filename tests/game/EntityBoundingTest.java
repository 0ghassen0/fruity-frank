package game;

import org.junit.jupiter.api.Test;

import game.entities.Ghost;
import game.entities.Gum;
import game.entities.Obstacle;
import game.entities.PacMan;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class EntityBoundingTest {

    private final TestGame game = new TestGame();

    private void init(PacMan e, int x, int y, double sizeCm) {
        e.bind(game.grid(), game.isu());
        e.setSize(game.isu().new Dimension(sizeCm, sizeCm));
        e.setPosition(game.grid().new Position(x, y));
    }

    private void init(Obstacle e, int x, int y) {
        e.bind(game.grid(), game.isu());
        e.setSize(game.isu().new Dimension(game.cmPerCell(), game.cmPerCell()));
        e.setPosition(game.grid().new Position(x, y));
    }

    private void init(Gum e, int x, int y, double sizeCm) {
        e.bind(game.grid(), game.isu());
        e.setSize(game.isu().new Dimension(sizeCm, sizeCm));
        e.setPosition(game.grid().new Position(x, y));
    }

    private void init(Ghost e, int x, int y, double sizeCm) {
        e.bind(game.grid(), game.isu());
        e.setSize(game.isu().new Dimension(sizeCm, sizeCm));
        e.setPosition(game.grid().new Position(x, y));
    }

    @Test
    public void pacManCollidesWithItself() {
        PacMan p = new PacMan();
        init(p, 5, 5, game.cmPerCell() / 2.0);

        assertTrue(p.intersects(p));
    }

    @Test
    public void pacManCollidesWithObstacleSameCell() {
        PacMan p = new PacMan();
        Obstacle o = new Obstacle();

        init(p, 5, 5, game.cmPerCell() / 2.0);
        init(o, 5, 5);

        assertTrue(p.intersects(o));
        assertTrue(o.intersects(p));
    }

    @Test
    public void pacManDoesNotCollideFarObstacle() {
        PacMan p = new PacMan();
        Obstacle o = new Obstacle();

        init(p, 1, 1, game.cmPerCell() / 2.0);
        init(o, 20, 20);

        assertFalse(p.intersects(o));
    }

    @Test
    public void pacManCollidesWithGumSameCell() {
        PacMan p = new PacMan();
        Gum g = new Gum();

        init(p, 6, 6, game.cmPerCell() / 2.0);
        init(g, 6, 6, game.cmPerCell() / 4.0);

        assertTrue(p.intersects(g));
        assertTrue(g.intersects(p));
    }

    @Test
    public void pacManCollidesWithGhostSameCell() {
        PacMan p = new PacMan();
        Ghost g = new Ghost();

        init(p, 7, 7, game.cmPerCell() / 2.0);
        init(g, 7, 7, game.cmPerCell() / 2.0);

        assertTrue(p.intersects(g));
        assertTrue(g.intersects(p));
    }

    @Test
    public void gumDoesNotCollideFarAway() {
        Gum g = new Gum();
        PacMan p = new PacMan();

        init(g, 1, 1, game.cmPerCell() / 4.0);
        init(p, 25, 25, game.cmPerCell() / 2.0);

        assertFalse(g.intersects(p));
    }
}