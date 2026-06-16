package engine.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import engine.entities.Entity;
import engine.entities.Stunt;
import engine.geometry.Grid;
import engine.geometry.ISU;

// == MODEL ==

/**
 * A {@code Model} owns and manages the game world: the {@link Entity entities}
 * that live in it, the {@link Grid} they move on, and the <i>physics</i> that
 * govern how they may move
 *
 * <p>
 * It is the simulation itself. On each {@link #update() tick} every managed
 * entity is given the chance to decide and move — via its
 * {@code engine.entities.Stunt} — while the model enforces the world rules
 *
 * <p>
 * Concrete subclasses specialise the world rules for a particular game
 *
 */
public abstract class Model implements Ticker.TickListener {

	// FIELDS

	/** The world the entities move on. */
	protected final Grid grid;
	/** The cm-based coordinate system paired with {@link #grid}. */
	protected final ISU isu;

	protected final Set<Entity> entities;

	// CONSTRUCTOR

	protected Model(Game game) {
		this.grid = game.getGrid();
		this.isu = game.getIsu();
		this.entities = ConcurrentHashMap.newKeySet();
	}

	// ENTITIES

	public void add(Entity e) {
		entities.add(e);
		e.setModel(this); // so the entity (and its bot's GAL conditions) can reach the world
	}

	/**
	 * Remove {@code e} from the simulation and detach it from the world so it can
	 * be garbage collected: drops it from {@link #entities} and calls
	 * {@link Entity#destroy()} to release the grid cells it still occupies.
	 */
	public void remove(Entity e) {
		entities.remove(e);
		e.destroy();
	}

	// SIMULATION

	/**
	 * One motion tick, sub-stepped to keep fast entities honest. Each stunt does
	 * its once-per-tick prep (rotation, buffered turns) and reports how many
	 * micro-steps it needs so no micro exceeds half a cell; the model then advances
	 * <i>every</i> stunt by the same number of micro-steps {@code n} (the global
	 * maximum) in lockstep, resolving collisions after each one. This kills
	 * tunnelling two ways: a fast entity can't skip a wall between leading-edge
	 * probes (Step A), and two fast entities can't cross between ticks without
	 * sharing an overlap test (Step B). A world of slow entities yields
	 * {@code n == 1} — a single full step plus one collision pass, identical to a
	 * non-sub-stepped tick.
	 */
	public void update() {
		Set<Entity> snapshot = new HashSet<>(entities);

		int n = 1;
		for (Entity e : snapshot) {
			Stunt s = e.stunt();
			if (s != null) {
				s.beginTick(this);
				n = Math.max(n, s.substeps());
			}
		}

		for (int i = 0; i < n; i++) {
			for (Entity e : snapshot) {
				Stunt s = e.stunt();
				if (s != null)
					s.advance(this, i, n);
			}
			resolveCollisions();
		}
	}

	/**
	 * {@link Ticker.TickListener} entry point: one motion tick is one
	 * {@link #update()}. The {@code Ticker} owns the cadence
	 */
	@Override
	public void onTick(double deltaTime) {
		update();
	}

	// COLLISIONS

	/**
	 * Detect every pair of currently intersecting entities and hand each confirmed
	 * collision to {@link #onCollision(Entity, Entity)} for the game to resolve.
	 *
	 */
	public void resolveCollisions() {
		Entity[] snapshot = entities.toArray(new Entity[0]);
		for (int i = 0; i < snapshot.length; i++) {
			for (int j = i + 1; j < snapshot.length; j++) {
				Entity a = snapshot[i];
				Entity b = snapshot[j];
				if (a.intersects(b)) {
					onCollision(a, b); // game outcome first (may remove/destroy a or b)
					notifyCollision(a, b); // then each survivor's bot/stunt reacts
					notifyCollision(b, a);
				}
			}
		}
	}

	/**
	 * Tell {@code e}'s bot (so it can interrupt its action and re-decide) and its
	 * stunt (so it can react at the motion level) that it collided with
	 * {@code impactor}. Skipped when {@code e} was destroyed by the outcome — its
	 * bot/stunt are then already detached (null), so a victim is never notified.
	 */
	private void notifyCollision(Entity e, Entity impactor) {
		engine.entities.Bot bot = e.bot();
		if (bot != null)
			bot.collision(impactor, 0.0);
		Stunt stunt = e.stunt();
		if (stunt != null)
			stunt.collision(impactor);
	}

	/**
	 * Apply the game-specific outcome of a confirmed collision between {@code a}
	 * and {@code b}. The pair is unordered
	 */
	protected abstract void onCollision(Entity a, Entity b);

	// PHYSICS

	/**
	 * @return {true} if the move into {target} is allowed.
	 */
	public abstract boolean canOccupy(Entity e, Grid.Position target);

	// GETTERS

	public Grid grid() {
		return grid;
	}

	public ISU isu() {
		return isu;
	}

	/**
	 * @return an unmodifiable view of the entities currently in the world
	 */
	public Set<Entity> entities() {
		return Collections.unmodifiableSet(entities);
	}
}
