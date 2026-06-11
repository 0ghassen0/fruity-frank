package engine;

import engine.*;
import engine.brain.AttackBot;
import engine.brain.BasicStunt;
import engine.brain.FleeBot;
import engine.core.Game;
import engine.core.Model;
import game.*;
import game.entities.Ghost;
import game.entities.PacMan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stunt and Bot")
public class StuntBotTest {

	private Game game;
	private Model model;

	@BeforeEach
	void setUp() {
		game = new PacManGame(20, 20);
		model = new Model(game);
	}

	private PacMan makePlayer(int x, int y) {
		PacMan p = new PacMan();
		p.bind(game.grid(), game.isu());
		p.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
		p.setPosition(game.grid().new Position(x, y));
		model.add(p);
		return p;
	}

	private Ghost makeGhost(int x, int y) {
		Ghost g = new Ghost();
		g.bind(game.grid(), game.isu());
		g.setSize(game.isu().new Dimension(game.cmPerCell() / 2.0, game.cmPerCell() / 2.0));
		g.setPosition(game.grid().new Position(x, y));
		model.add(g);
		return g;
	}

	// =========================================================
	// == BasicStunt
	// =========================================================

	
	
	@Test
	@DisplayName("walk moves entity to adjacent cell center")
	void walk_movesToAdjacentCell() {
		PacMan p = makePlayer(5, 5);
		BasicStunt stunt = new BasicStunt(model, p);

		stunt.walk(0); // Est

		// tick jusqu'à destination
		for (int i = 0; i < 1000; i++) {
			if (!p.isMoving())
				break;
			model.tick(10);
		}

		assertFalse(p.isMoving());
		assertEquals(6, p.position().x());
		assertEquals(5, p.position().y());
	}

	@Test
	@DisplayName("walk snaps entity exactly to cell center")
	void walk_snapsToCenter() {
		PacMan p = makePlayer(5, 5);
		BasicStunt stunt = new BasicStunt(model, p);
		stunt.walk(90); // Sud

		for (int i = 0; i < 1000; i++) {
			if (!p.isMoving())
				break;
			model.tick(10);
		}

		double expected = (5 + 1 + 0.5) * game.cmPerCell();
		assertEquals(expected, p.center().y(), 1e-6);
	}

	@Test
	@DisplayName("walk north decrements y")
	void walk_north() {
		PacMan p = makePlayer(5, 5);
		BasicStunt stunt = new BasicStunt(model, p);
		stunt.walk(270); // Nord

		for (int i = 0; i < 1000; i++) {
			if (!p.isMoving())
				break;
			model.tick(10);
		}

		assertEquals(4, p.position().y());
	}

	@Test
	@DisplayName("entity stops after one cell walk")
	void walk_stopsAfterOneCell() {
		PacMan p = makePlayer(3, 3);
		BasicStunt stunt = new BasicStunt(model, p);
		stunt.walk(0);

		for (int i = 0; i < 1000; i++) {
			if (!p.isMoving())
				break;
			model.tick(10);
		}

		assertFalse(p.isMoving());
	}

	// =========================================================
	// == AttackBot
	// =========================================================

	@Test
	@DisplayName("attack bot moves toward target")
	void attackBot_movesTowardTarget() {
	    Ghost attacker = makeGhost(1, 5);
	    PacMan target = makePlayer(5, 5);

	    BasicStunt stunt = new BasicStunt(model, attacker);
	    AttackBot bot = new AttackBot(stunt, target);
	    bot.think();

	    System.out.println("Initial attacker pos: " + attacker.position().x() + "," + attacker.position().y());
	    System.out.println("Initial target pos: " + target.position().x() + "," + target.position().y());
	    System.out.println("Initial isMoving: " + attacker.isMoving());
	    System.out.println("Initial lSpeed: " + attacker.lSpeed());

	    for (int i = 0; i < 5000; i++) {
	        model.tick(10);
	        if (i % 500 == 0) {
	            System.out.println("tick " + i + " pos: " + attacker.position().x() + "," + attacker.position().y() + " moving: " + attacker.isMoving());
	        }
	        if (attacker.intersects(target)) break;
	    }

	    System.out.println("Final attacker pos: " + attacker.position().x() + "," + attacker.position().y());
	    assertTrue(attacker.intersects(target));
	    
	    bot.think();
	    System.out.println("bot in stunt: " + (stunt.bot() != null ? "OK" : "NULL"));
	}
	@Test
	@DisplayName("attack bot reduces target health points on contact")
	void attackBot_damagesTarget() {
		Ghost attacker = makeGhost(1, 5);
		PacMan target = makePlayer(5, 5);

		BasicStunt stunt = new BasicStunt(model, attacker);
		AttackBot bot = new AttackBot(stunt, target);
		bot.think();

		int initialHp = target.healthPoints();

		for (int i = 0; i < 5000; i++) {
			model.tick(10);
			if (target.healthPoints() < initialHp)
				break;
		}

		assertTrue(target.healthPoints() < initialHp);
	}

	@Test
	@DisplayName("attack bot kills target eventually")
	void attackBot_killsTarget() {
		Ghost attacker = makeGhost(1, 5);
		PacMan target = makePlayer(3, 5);

		BasicStunt stunt = new BasicStunt(model, attacker);
		AttackBot bot = new AttackBot(stunt, target);
		bot.think();

		for (int i = 0; i < 50000; i++) {
			model.tick(10);
			if (target.isDead())
				break;
		}

		assertTrue(target.isDead());
	}

	// =========================================================
	// == FleeBot
	// =========================================================

	@Test
	@DisplayName("flee bot moves away from enemy")
	void fleeBot_movesAway() {
		PacMan player = makePlayer(5, 5);
		Ghost enemy = makeGhost(5, 3);

		BasicStunt stunt = new BasicStunt(model, player);
		FleeBot bot = new FleeBot(stunt, List.of(enemy));
		bot.think();

		double initialDist = player.center().distanceTo(enemy.center());

		for (int i = 0; i < 2000; i++) {
			model.tick(10);
			if (!player.isMoving())
				break;
		}

		double finalDist = player.center().distanceTo(enemy.center());
		assertTrue(finalDist >= initialDist);
	}

	@Test
	@DisplayName("flee bot flees closest enemy among several")
	void fleeBot_fleesClosest() {
		PacMan player = makePlayer(5, 5);
		Ghost close = makeGhost(5, 4);
		Ghost far = makeGhost(5, 1);

		BasicStunt stunt = new BasicStunt(model, player);
		FleeBot bot = new FleeBot(stunt, List.of(close, far));
		bot.think();

		for (int i = 0; i < 2000; i++) {
			model.tick(10);
			if (!player.isMoving())
				break;
		}

		double distFromClose = player.center().distanceTo(close.center());
		double distFromFar = player.center().distanceTo(far.center());
		assertTrue(distFromClose > game.cmPerCell());
	}

	// =========================================================
	// == Health Points
	// =========================================================

	@Test
	@DisplayName("entity starts with 100 health points")
	void entity_startsWithFullHealth() {
		PacMan p = makePlayer(5, 5);
		assertEquals(100, p.healthPoints());
	}

	@Test
	@DisplayName("takeDamage reduces health points")
	void takeDamage_reducesHp() {
		PacMan p = makePlayer(5, 5);
		p.takeDamage(30);
		assertEquals(70, p.healthPoints());
	}

	@Test
	@DisplayName("health points cannot go below zero")
	void takeDamage_notBelowZero() {
		PacMan p = makePlayer(5, 5);
		p.takeDamage(200);
		assertEquals(0, p.healthPoints());
		assertTrue(p.isDead());
	}

	// =========================================================
	// == Collision
	// =========================================================

	@Test
	@DisplayName("collision stops entity movement")
	void collision_stopsMovement() {
		Ghost g = makeGhost(4, 5);
		PacMan p = makePlayer(5, 5);

		BasicStunt stunt = new BasicStunt(model, g);
		AttackBot bot = new AttackBot(stunt, p);
		bot.think();

		for (int i = 0; i < 5000; i++) {
			model.tick(10);
			if (!g.isMoving())
				break;
		}

		assertFalse(g.isMoving());
	}
}
