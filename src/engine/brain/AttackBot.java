package engine.brain;

import engine.core.ISU;
import engine.entity.Entity;

public class AttackBot extends Bot {

	private final Entity target;
	
	public AttackBot(BasicStunt stunt, Entity target) {
		super(stunt);
		assert target != null;
		this.target = target;
	}

	@Override
	public void think() {
		moveTowardTarget();
	}

	@Override
	public void done() {
	    if (stunt.entity.intersects(target)) {
	        target.takeDamage(10);
	        if (!target.isDead()) moveTowardTarget();
	    } else {
	        moveTowardTarget();
	    }
	}


	@Override
	public void collision(Entity other) {
	    if (other == target) {
	        target.takeDamage(10);
	        if (!target.isDead()) moveTowardTarget();
	    } else {
	        moveTowardTarget();
	    }
	}
	
	private void moveTowardTarget() {
		ISU.Coord me = stunt.entity.center();
		ISU.Coord them = target.center();
		ISU isu = stunt.entity.isu();
		
		double dx = isu.xAxis().distance(me.x(), them.x());
		double dy = isu.yAxis().distance(me.y(), them.y());
		
		int degree = (int) Math.toDegrees(Math.atan2(dy, dx));
		if (degree < 0) degree += 360;
		
		stunt.walk(degree);
	}
}
