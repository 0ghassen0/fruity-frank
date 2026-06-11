package engine.collision;

import engine.core.ISU;

public class Rect extends Shape {
	
	private ISU.Dimension size;
	private int angle;
	private ISU isu;

	private double halfW;
	private double halfH;
	
	public Rect(
			ISU.Coord center,
			ISU.Dimension size, 
			int angle,
			ISU isu
			)
	{
		super(center);
		assert size != null;
		
		this.size = size;
		this.angle = angle;
		this.isu = isu;
		
		this.halfH = size.y() / 2.0;
		this.halfW = size.x() / 2.0;
		
		
	}
	

	@Override
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	@Override
	public boolean intersects(Circle c) {
		return new RectCircleIntersection(this, c).intersects();
	}

	@Override
	public boolean intersects(Rect r) {
		return new RectRectIntersection(this, r, this.isu).intersects();
	}


	
	// GETTERS 
	
	public ISU.Dimension size(){
		return this.size;
	}
	
	public int angle() {
		return this.angle;
	}
	
	public double halfW() {
		return this.halfW;
	}
	
	public double halfH() {
		return this.halfH;
	}
	
	public ISU isu() {
		return this.isu;
	}
	
}
