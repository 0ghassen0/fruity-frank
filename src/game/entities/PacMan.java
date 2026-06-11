package game.entities;

import engine.collision.Bounding;
import engine.collision.Circle;
import engine.entity.Entity;


public class PacMan extends Entity{

	
	public PacMan() {
		super("PacMan");
		
	}

	@Override
	protected void updateBounding() {
		
	    if (center() == null || size() == null) {
	        return;
	    }

		Bounding b = new Bounding();

		Circle c = new Circle(
		    center(),
		    size().x() / 2.0
		);

		b.add(c);

		setBounding(b);
	}
}
