package engine.brain;

import engine.core.Grid;
import engine.core.ISU;
import engine.core.Model;
import engine.core.ISU.Coord;
import engine.entity.Entity;

public class BasicStunt extends Stunt {

	private ISU.Coord target;

	public BasicStunt(Model model, Entity entity) {
		super(model, entity);
	}
	
	// GETTERS
	
    public ISU.Coord target() { 
    	return target; 
    	}


	public void walk(int degree) {
		// 1 dir cardinale plus proche
		int cardinal = snapToCardinal(degree);
		entity.turn(cardinal - entity.orientation());

		// 2 calculer cellule cible
		Grid.Position current = entity.position();
		Grid.Vector dir = cardinalToVector(cardinal);
        Grid.Position next = current.copy();
		next.translate(dir);

		// 3 set la cible

		target = next.toISUCoordCentered();

		// 4 calculer le vecteur vitesse vers la cible
		double speed = entity.walkSpeed();
		ISU.Coord center = entity.center();

		double dx = entity.isu().xAxis().distance(center.x(), target.x());

		double dy = entity.isu().yAxis().distance(center.y(), target.y());

		ISU.Vector dir_v = entity.isu().new Vector(dx, dy);
		dir_v.unity();
		dir_v.scale(speed);
		entity.setLSpeed(dir_v);
		
	}
	
	
	private int snapToCardinal(int degree) {
        // Nord=270, Sud=90, Est=0, Ouest=180
        int[] cardinals = {0, 90, 180, 270};
        int best = cardinals[0];
        int bestDiff = Integer.MAX_VALUE;
        for (int c : cardinals) {
            int diff = Math.abs(degree - c);
            diff = Math.min(diff, 360 - diff);
            if (diff < bestDiff) { bestDiff = diff; best = c; }
        }
        return best;
    }

    private Grid.Vector cardinalToVector(int cardinal) {
        switch(cardinal) {
            case 0:   return entity.grid().new Vector(1, 0);
            case 90:  return entity.grid().new Vector(0, 1);
            case 180: return entity.grid().new Vector(-1, 0);
            case 270: return entity.grid().new Vector(0, -1);
            default:  return entity.grid().new Vector(0, 0);
        }
    }

    @Override
    public void set(double x, double y) {
        entity.stopMoving();
        super.set(x, y); // notifie le bot
    }

    public boolean hasReachedTarget(ISU.Coord current) {
        if (target == null) return false;
        return current.distanceTo(target) < entity.isu().game().cmPerCell() * 0.5;
    }

}

