package game.entities;

import engine.collision.Bounding;
import engine.collision.Circle;
import engine.collision.Rect;
import engine.collision.iShape;
import engine.entity.Entity;

public class Ghost extends Entity{

	public Ghost() {
		super("Ghost");
		
	}
	
	
	@Override 
	public void updateBounding() {

	    if (center() == null || size() == null) {
	        return;
	    }

	    Bounding b = new Bounding();

	    Circle c = new Circle(
	         isu().new Coord(
	            center().x(),
	            center().y() - size().y() * 0.2
	        ),
	        size().x() / 4.0
	    );

	    Rect r = new Rect(
	        center(),
	        isu().new Dimension(size().x(), size().y() * 0.6),
	        orientation(),
	        isu()
	    );

	    b.add(new iShape[]{c, r});

	    setBounding(b);
	}

}
