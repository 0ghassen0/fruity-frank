package engine;

import engine.core.Game;
import engine.core.Grid;
import engine.core.Model;
import game.PacManGame;
import game.entities.Obstacle;
import game.entities.PacMan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model")
public class ModelTest {

    private Game game;
    private Model model;

    @BeforeEach
    void setUp() {
        game = new PacManGame(10, 10);
        model = new Model(game);
    }

    @Test
    @DisplayName("add places entity in model list")
    void add_entityInList() {
        PacMan p = new PacMan();
        p.bind(game.grid(), game.isu());
        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        p.setPosition(game.grid().new Position(3, 3));

        model.add(p);

        assertTrue(model.entities().contains(p));
    }

    @Test
    @DisplayName("add deploys entity into its cell")
    void add_entityDeployedInCell() {
        PacMan p = new PacMan();
        p.bind(game.grid(), game.isu());
        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        p.setPosition(game.grid().new Position(3, 3));

        model.add(p);

        Grid.Cell cell = game.grid().cellAt(game.grid().new Position(3, 3));
        assertTrue(cell.contains(p));
    }

    @Test
    @DisplayName("remove takes entity out of model list")
    void remove_entityRemovedFromList() {
        PacMan p = new PacMan();
        p.bind(game.grid(), game.isu());
        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        p.setPosition(game.grid().new Position(3, 3));

        model.add(p);
        model.remove(p);

        assertFalse(model.entities().contains(p));
    }

    @Test
    @DisplayName("remove retracts entity from its cell")
    void remove_entityRetractedFromCell() {
        PacMan p = new PacMan();
        p.bind(game.grid(), game.isu());
        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        p.setPosition(game.grid().new Position(3, 3));

        model.add(p);
        model.remove(p);

        Grid.Cell cell = game.grid().cellAt(game.grid().new Position(3, 3));
        assertFalse(cell.contains(p));
    }

    @Test
    @DisplayName("translate updates cell membership")
    void translate_updatesCellMembership() {
        PacMan p = new PacMan();
        p.bind(game.grid(), game.isu());
        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        p.setPosition(game.grid().new Position(3, 3));
        model.add(p);

        p.translate(game.grid().new Vector(1, 0));

        Grid.Cell oldCell = game.grid().cellAt(game.grid().new Position(3, 3));
        Grid.Cell newCell = game.grid().cellAt(game.grid().new Position(4, 3));

        assertFalse(oldCell.contains(p));
        assertTrue(newCell.contains(p));
    }

    @Test
    @DisplayName("two entities in same cell are both present")
    void twoEntities_sameCell() {
        PacMan p = new PacMan();
        Obstacle o = new Obstacle();

        p.bind(game.grid(), game.isu());
        o.bind(game.grid(), game.isu());

        p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
        o.setSize(game.isu().new Dimension(game.cmPerCell(), game.cmPerCell()));

        p.setPosition(game.grid().new Position(5, 5));
        o.setPosition(game.grid().new Position(5, 5));

        model.add(p);
        model.add(o);

        Grid.Cell cell = game.grid().cellAt(game.grid().new Position(5, 5));
        assertTrue(cell.contains(p));
        assertTrue(cell.contains(o));
    }

    @Test
    @DisplayName("model starts empty")
    void model_startsEmpty() {
        assertTrue(model.entities().isEmpty());
    }
}