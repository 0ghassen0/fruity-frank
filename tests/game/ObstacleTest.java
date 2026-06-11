package game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import engine.*;
import engine.core.Game;
import engine.core.Grid;
import engine.core.ISU;
import game.entities.Obstacle;
import game.entities.PacMan;

public class ObstacleTest {

    private Game game = new TestGame();
    private ISU isu = game.isu();

    @Test
    public void testObstacleHasBounding() {
        Obstacle o = new Obstacle();
        o.bind(game.grid(), isu);
        o.setSize(isu.new Dimension(game.cmPerCell(), game.cmPerCell()));
        o.setPosition(game.grid().new Position(3, 3));
        assertNotNull(o.bounding());
    }

    @Test
    public void testObstacleIsSolid() {
        Obstacle o = new Obstacle();
        PacMan p = new PacMan();
        o.bind(game.grid(), isu);
        p.bind(game.grid(), isu);
        o.setSize(isu.new Dimension(game.cmPerCell(), game.cmPerCell()));
        p.setSize(isu.new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        Grid.Position pos = game.grid().new Position(7, 7);
        o.setPosition(pos);
        p.setPosition(pos);
        assertTrue(o.intersects(p));
    }
}