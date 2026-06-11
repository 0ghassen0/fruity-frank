package engine.core;

import java.util.ArrayList;
import java.util.List;

import engine.brain.BasicStunt;
import engine.entity.Entity;

public class Model {

	private final Grid grid;
	private final ISU isu;
	private final List<Entity> entities;

	public Model(Game game) {
		assert game != null;
		this.grid = game.grid();
		this.isu = game.isu();
		this.entities = new ArrayList<>();
	}

	public Grid grid() {
		return grid;
	}

	public ISU isu() {
		return isu;
	}

	public List<Entity> entities() {
		return java.util.Collections.unmodifiableList(entities);
	}

	public void add(Entity e) {
		assert e != null;
		entities.add(e);
		e.deploy();
	}

	public void remove(Entity e) {
		assert e != null;
		e.retract();
		entities.remove(e);
	}

	public void tick(double elapsed_ms) {
		assert elapsed_ms > 0;
		for (Entity e : new ArrayList<>(entities)) {
			if (e.isMoving())
				move(e, elapsed_ms);
		}
		detectCollisions();
	}

	private void move(Entity e, double elapsed_ms) {
	    ISU.Vector lSpeed = e.lSpeed();

	    if (lSpeed != null && lSpeed.norm() > 1e-9) {
	        if (e.stunt() instanceof BasicStunt) {
	            BasicStunt bs = (BasicStunt) e.stunt();
	            if (bs.target() != null) {
	                double distBefore = e.center().distanceTo(bs.target());
	                double stepSize = lSpeed.norm() * elapsed_ms;
	                if (stepSize >= distBefore) {
	                    e.snapToTarget(bs.target());
	                    e.stopMoving();
	                    bs.set(e.center().x(), e.center().y());
	                    return;
	                }
	            }
	        }

	        ISU.Vector delta = isu.new Vector(
	            lSpeed.x() * elapsed_ms,
	            lSpeed.y() * elapsed_ms
	        );
	        e.translate(delta);

	        if (e.stunt() instanceof BasicStunt) {
	            BasicStunt bs = (BasicStunt) e.stunt();
	            if (bs.hasReachedTarget(e.center())) {
	                e.snapToTarget(bs.target());
	                e.stopMoving();
	                bs.set(e.center().x(), e.center().y());
	            }
	        }
	    }

	    if (Math.abs(e.aSpeed()) > 1e-9) {
	        e.turn((int)(e.aSpeed() * elapsed_ms));
	    }
	}
	private void detectCollisions() {
		List<Entity> list = new ArrayList<>(entities);
		for (int i = 0; i < list.size(); i++) {
			for (int j = i + 1; j < list.size(); j++) {
				Entity a = list.get(i);
				Entity b = list.get(j);
				if (a.intersects(b)) {
					a.collision(b);
					b.collision(a);
				}
			}
		}
	}

	public void show(java.io.PrintStream ps) {
		ps.println("Model: " + entities.size() + " entities");
		for (Entity e : entities)
			e.show(ps);
	}
}