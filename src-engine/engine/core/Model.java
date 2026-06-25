package engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import engine.entities.Entity;
import engine.entities.Stunt;
import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.graphics.Camera;

// == MODEL ==

/**
 * A {@code Model} owns and manages the game world: the {@link Entity entities}
 * that live in it, the {@link Grid} they move on, and the <i>physics</i> that
 * govern how they may move.
 *
 * <p>
 * It is the simulation itself. On each {@link #update() tick} every active
 * managed entity is given the chance to move — via its
 * {@code engine.entities.Stunt} — while the model enforces the world rules.
 *
 * <p>
 * Concrete subclasses specialise the world rules for a particular game.
 */
public abstract class Model implements Ticker.TickListener {

	// FIELDS

	/** The world the entities move on. */
	protected final Grid grid;

	/** The cm-based coordinate system paired with {@link #grid}. */
	protected final ISU isu;

	protected final Set<Entity> entities;

	/**
	 * Optional camera used to limit the simulated zone.
	 *
	 * <p>
	 * If null, all entities are simulated as before. If set, only entities visible
	 * in the camera plus a margin are simulated.
	 */
	private Camera simulationCamera = null;

	/**
	 * Extra margin around the camera viewport where entities are still simulated.
	 * This avoids freezing entities exactly at the border of the screen.
	 */
	private double simulationMarginCm = 0.0;

	// Per-tick scratch buffers

	/** Snapshot of entities for the current {@link #update()} tick. */
	private final ArrayList<Entity> tickBuffer = new ArrayList<>();

	/** Snapshot of entities for the current {@link #resolveCollisions()} pass. */
	private final ArrayList<Entity> collisionSnapshot = new ArrayList<>();

	/** Stable index of each entity within {@link #collisionSnapshot}. */
	private final Map<Entity, Integer> collisionIndex = new HashMap<>();

	/**
	 * Candidate colliding pairs, encoded as {@code (loIndex << 32) | hiIndex}. A
	 * long holds a pair of ints (32 bits * 2) -> indexes of collisionSnapshot.
	 */
	private final ArrayList<Long> candidatePairs = new ArrayList<>();

	/** De-duplicates pairs that surface via more than one shared cell. */
	private final HashSet<Long> seenPairs = new HashSet<>();

	// CONSTRUCTOR

	protected Model(Game game) {
		this.grid = game.getGrid();
		this.isu = game.getIsu();
		this.entities = new HashSet<>();
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

	// ACTIVE SIMULATION ZONE

	/**
	 * Enables simulation culling around a camera.
	 *
	 * @param camera   the camera defining the visible zone
	 * @param marginCm extra active margin around the camera, in cm
	 */
	public void setSimulationCamera(Camera camera, double marginCm) {
		this.simulationCamera = camera;
		this.simulationMarginCm = Math.max(0.0, marginCm);
	}

	/**
	 * Disables simulation culling. All entities are simulated again.
	 */
	public void clearSimulationCamera() {
		this.simulationCamera = null;
		this.simulationMarginCm = 0.0;
	}

	/**
	 * Returns whether an entity is inside the active simulation zone.
	 *
	 * <p>
	 * Default behaviour stays unchanged when no camera is configured.
	 */
	public boolean isActiveForSimulation(Entity e) {
		if (simulationCamera == null)
			return true;

		if (e == null || e.center() == null)
			return true;

		String className = e.getClass().getSimpleName();

		if (className.equals("Frank"))
			return true;

		if (className.equals("EnemySpawnerEntity"))
			return true;

		if (className.equals("Enemy"))
			return true;

		return simulationCamera.isVisible(e.center().x(), e.center().y(), simulationMarginCm);
	}

	// SIMULATION

	/**
	 * One motion tick, sub-stepped to keep fast entities honest. Each stunt does
	 * its once-per-tick prep (rotation, buffered turns) and reports how many
	 * micro-steps it needs so no micro exceeds half a cell; the model then advances
	 * every active stunt by the same number of micro-steps {@code n} in lockstep,
	 * resolving collisions after each one.
	 */
	public void update() {
		tickBuffer.clear();

		for (Entity e : entities) {
			if (isActiveForSimulation(e))
				tickBuffer.add(e);
		}

		int n = 1;
		for (Entity e : tickBuffer) {
			Stunt s = e.stunt();
			if (s != null) {
				s.beginTick(this);
				n = Math.max(n, s.substeps());
			}
		}

		for (int i = 0; i < n; i++) {
			for (Entity e : tickBuffer) {
				if (!entities.contains(e))
					continue;

				Stunt s = e.stunt();
				if (s != null)
					s.advance(this, i, n);
			}
			resolveCollisions();
		}

		tickBuffer.clear();
	}

	/**
	 * {@link Ticker.TickListener} entry point: one motion tick is one
	 * {@link #update()}. The {@code Ticker} owns the cadence.
	 */
	@Override
	public void onTick(double deltaTime) {
		update();
	}

	// COLLISIONS

	/**
	 * Detect every pair of currently intersecting active entities and hand each
	 * confirmed collision to {@link #onCollision(Entity, Entity)} for the game to
	 * resolve.
	 *
	 * <p>
	 * Broad phase: cell occupancy.
	 *
	 * <p>
	 * Narrow phase: reject or accept candidates.
	 */
	public void resolveCollisions() {
		collisionSnapshot.clear();
		collisionIndex.clear();

		for (Entity e : entities) {
			if (!isActiveForSimulation(e))
				continue;

			collisionIndex.put(e, collisionSnapshot.size());
			collisionSnapshot.add(e);
		}

		// --- Broad phase: candidate pairs are cell co-occupants ---
		candidatePairs.clear();
		seenPairs.clear();

		for (Entity a : collisionSnapshot) {
			int ia = collisionIndex.get(a);

			for (Grid.Cell cell : a.getHitbox().occupiedCells()) {
				for (Entity b : cell.entities()) {
					Integer ibObj = collisionIndex.get(b);
					if (ibObj == null)
						continue; // occupant not active in this snapshot

					int ib = ibObj;
					if (ia >= ib)
						continue; // emit each unordered pair once, as (lo, hi)

					long key = ((long) ia << 32) | (ib & 0xffffffffL);
					if (seenPairs.add(key))
						candidatePairs.add(key); // first time this pair surfaces
				}
			}
		}

		// --- Deterministic order: ascending (ia, ib) ---
		Collections.sort(candidatePairs);

		// --- Narrow phase + resolution ---
		for (long key : candidatePairs) {
			Entity a = collisionSnapshot.get((int) (key >>> 32));
			Entity b = collisionSnapshot.get((int) key);

			// Skip if a prior outcome this pass already removed either entity.
			if (!entities.contains(a) || !entities.contains(b))
				continue;

			if (a.intersects(b)) {
				onCollision(a, b); // game outcome first
				notifyCollision(a, b); // then each survivor's bot/stunt reacts
				notifyCollision(b, a);
			}
		}

		collisionSnapshot.clear();
		collisionIndex.clear();
		candidatePairs.clear();
		seenPairs.clear();
	}

	/**
	 * Tell {@code e}'s bot and stunt that it collided with {@code impactor}.
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
	 * and {@code b}. The pair is unordered.
	 */
	protected abstract void onCollision(Entity a, Entity b);

	// PHYSICS

	/**
	 * @return true if the move into {@code target} is allowed.
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
	 * @return an unmodifiable view of the entities currently in the world.
	 */
	public Set<Entity> entities() {
		return Collections.unmodifiableSet(entities);
	}
}