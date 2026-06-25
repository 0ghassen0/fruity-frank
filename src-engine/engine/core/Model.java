package engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import engine.entities.AlwaysSimulated;
import engine.entities.Entity;
import engine.entities.Stunt;
import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.geometry.ActiveRegion;

// == MODEL ==

/** Owns the game world — its entities, grid, and movement rules — and runs the simulation. */
public abstract class Model implements Ticker.TickListener {

	// FIELDS

	/** The world the entities move on. */
	protected final Grid grid;

	/** The cm-based coordinate system paired with {@link #grid}. */
	protected final ISU isu;

	protected final Set<Entity> entities;

	/** Optional zone limiting what gets simulated; null means simulate everything. */
	private ActiveRegion activeRegion = null;

	// Per-tick scratch buffers

	/** Snapshot of entities for the current {@link #update()} tick. */
	private final ArrayList<Entity> tickBuffer = new ArrayList<>();

	/** Snapshot of entities for the current {@link #resolveCollisions()} pass. */
	private final ArrayList<Entity> collisionSnapshot = new ArrayList<>();

	/** Stable index of each entity within {@link #collisionSnapshot}. */
	private final Map<Entity, Integer> collisionIndex = new HashMap<>();

	/** Candidate colliding pairs, encoded as {@code (loIndex << 32) | hiIndex}. */
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

	/** Restricts simulation to the given zone. */
	public void setActiveRegion(ActiveRegion region) {
		this.activeRegion = region;
	}

	/** Re-enables simulation of every entity. */
	public void clearActiveRegion() {
		this.activeRegion = null;
	}

	/** Tells whether an entity should be simulated this tick. */
	public boolean isActiveForSimulation(Entity e) {
		if (activeRegion == null)
			return true;

		if (e instanceof AlwaysSimulated)
			return true;

		ISU.Coord c = (e == null) ? null : e.center();
		if (c == null)
			return true;

		return activeRegion.contains(c.x(), c.y());
	}

	// SIMULATION

	/** Runs one motion tick over the active entities, resolving collisions after each sub-step. */
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

	/** Ticker entry point: runs one motion tick. */
	@Override
	public void onTick(double deltaTime) {
		update();
	}

	// COLLISIONS

	/** Detects intersecting active entities and resolves each collision. */
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

	/** Notifies {@code e}'s bot and stunt that it collided with {@code impactor}. */
	private void notifyCollision(Entity e, Entity impactor) {
		engine.entities.Bot bot = e.bot();
		if (bot != null)
			bot.collision(impactor, 0.0);

		Stunt stunt = e.stunt();
		if (stunt != null)
			stunt.collision(impactor);
	}

	/** Applies the game-specific outcome of a collision between two entities. */
	protected abstract void onCollision(Entity a, Entity b);

	// PHYSICS

	/** Tells whether {@code e} may move into {@code target}. */
	public abstract boolean canOccupy(Entity e, Grid.Position target);

	// GETTERS

	public Grid grid() {
		return grid;
	}

	public ISU isu() {
		return isu;
	}

	/** Unmodifiable view of the live entities. */
	public Set<Entity> entities() {
		return Collections.unmodifiableSet(entities);
	}
}
