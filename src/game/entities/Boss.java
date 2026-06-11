package game.entities;

import engine.collision.Bounding;
import engine.collision.Rect;
import engine.core.ISU;
import engine.entity.Entity;

public class Boss extends Entity{

	public Boss(String name) {
		super(name);
	}
	
	@Override
	public void updateBounding() {
	    if (center() == null || size() == null) return;
	    
	    Bounding b = new Bounding();
	    double s = isu().game().cmPerCell();
	    double cx = center().x();
	    double cy = center().y();
	    ISU.Dimension cellSize = isu().new Dimension(s, s);
	    
	    // centre
	    b.add(new Rect(isu().new Coord(cx, cy), cellSize, 0, isu()));
	    // au dessus
	    b.add(new Rect(isu().new Coord(cx, cy - s), cellSize, 0, isu()));
	    // 2 en dessous
	    b.add(new Rect(isu().new Coord(cx, cy + s), cellSize, 0, isu()));
	    b.add(new Rect(isu().new Coord(cx, cy + 2*s), cellSize, 0, isu()));
	    // 2 à droite
	    b.add(new Rect(isu().new Coord(cx + s, cy), cellSize, 0, isu()));
	    b.add(new Rect(isu().new Coord(cx + 2*s, cy), cellSize, 0, isu()));
	    
	    setBounding(b);
	}

}
