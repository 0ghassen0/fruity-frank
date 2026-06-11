package game.entities;

import engine.collision.Bounding;
import engine.collision.Circle;
import engine.entity.Entity;

public class Gum extends Entity{

	public Gum() {
		super("Gum");
	}
	
	@Override
	public void updateBounding() {
		
	    if (center() == null || size() == null) {
	        return;
	    }

		Bounding b = new Bounding();
		

		Circle c = new Circle(
		    center(),
		    Math.min(size().x(), size().y()) / 4.0
		    );

		b.add(c);

		setBounding(b);
	}

}
