package game;

import org.junit.jupiter.api.Test;

import game.entities.Ghost;
import game.entities.PacMan;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class GhostBoundingTest {

    private final TestGame game = new TestGame();

    @Test
    public void ghostCollidesWithPacManSameCell() {
        Ghost g = new Ghost();
        PacMan p = new PacMan();

        g.bind(game.grid(), game.isu());
        p.bind(game.grid(), game.isu());

        g.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));

        g.setPosition(game.grid().new Position(10, 10));
        p.setPosition(game.grid().new Position(10, 10));

        assertTrue(g.intersects(p));
    }
}