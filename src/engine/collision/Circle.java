package engine.collision;

import engine.core.ISU.Coord;

public class Circle extends Shape {
	
	// FIELDS

	private double radius; 
    
    public Circle(Coord center, double radius) {
		super(center);
		
		assert radius > 0;
		
		this.radius = radius;
	}


    // GETTERS 


    public double radius() {
        return radius;
    }

    @Override
    public boolean intersects(iShape shape) {
        return shape.intersects(this);
    }

    @Override
    public boolean intersects(Circle c) {
        double d = center().distanceTo(c.center());
        return d <= (radius + c.radius);
    }

    @Override
    public boolean intersects(Rect r) {
        return r.intersects(this);
    }
}