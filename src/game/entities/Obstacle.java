package game.entities;

import engine.collision.Bounding;
import engine.collision.Rect;
import engine.entity.Entity;

public class Obstacle extends Entity{

	public Obstacle() {
		super("Obstacle");
		
	}

	
	@Override
	public void updateBounding() {
		
	    if (center() == null || size() == null) {
	        return;
	    }

		Bounding b = new Bounding();
		
	    Rect r = new Rect(
		        center(),
		        size(),
		        0,
		        isu()
		    );
	    
	    b.add(r);
	    
	    setBounding(b);

	}
}
