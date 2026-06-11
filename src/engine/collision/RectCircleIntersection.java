package engine.collision;

import engine.core.Axis;

public class RectCircleIntersection {

	// FIELDS
	
	private Rect rect;
	private Circle circle;
	
	
	public RectCircleIntersection(Rect r, Circle c) {
		// assertions
		assert r != null;
		assert c != null;
		
		// assignments
		this.rect = r;
		this.circle = c;
	}
	
	public boolean intersects() {
	    Axis xAxis = rect.isu().xAxis();
	    Axis yAxis = rect.isu().yAxis();

	    double cx = xAxis.distance(rect.center().x(), circle.center().x());
	    double cy = yAxis.distance(rect.center().y(), circle.center().y());

	    double rad = Math.toRadians(-rect.angle());
	    double localX = cx * Math.cos(rad) - cy * Math.sin(rad);
	    double localY = cx * Math.sin(rad) + cy * Math.cos(rad);

	    double closestX = clamp(localX, -rect.halfW(), rect.halfW());
	    double closestY = clamp(localY, -rect.halfH(), rect.halfH());

	    double dx = localX - closestX;
	    double dy = localY - closestY;

	    return (dx * dx + dy * dy) <= circle.radius() * circle.radius();
	}
	
	 private double clamp(double v, double min, double max) {
	        return Math.max(min, Math.min(max, v));
	    }
}
