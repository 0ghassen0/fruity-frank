package engine.brain;

import java.util.List;

import engine.core.ISU;
import engine.entity.Entity;

public class FleeBot extends Bot {

	private final List<Entity> enemies;

	public FleeBot(BasicStunt stunt, List<Entity> enemies) {
		super(stunt);
		assert enemies != null;
		this.enemies = enemies;
	}

	@Override
	public void think() {
		fleeClosest();
	}

	@Override
	public void done() {
		fleeClosest();
	}

	@Override
	public void collision(Entity other) {
		fleeClosest();
	}

	private void fleeClosest() {
		Entity closest = findClosest();
		if (closest == null)
			return;

		ISU.Coord me = stunt.entity.center();
		ISU.Coord them = closest.center();
		ISU isu = stunt.entity.isu();

		double dx = isu.xAxis().distance(me.x(), them.x());
		double dy = isu.yAxis().distance(me.y(), them.y());

		// Direction opposée
		int degree = (int) Math.toDegrees(Math.atan2(-dy, -dx));
		if (degree < 0)
			degree += 360;

		stunt.walk(degree);
	}

	private Entity findClosest() {
		Entity closest = null;
		double minDist = Double.MAX_VALUE;

		for (Entity e : enemies) {
			double d = stunt.entity.center().distanceTo(e.center());
			if (d < minDist) {
				minDist = d;
				closest = e;
			}
		}

		return closest;
	}

}
