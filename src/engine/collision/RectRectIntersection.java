package engine.collision;

import engine.core.Axis;
import engine.core.ISU;

class RectRectIntersection {

	private Rect a;
	private Rect b;
	private ISU isu;

	public RectRectIntersection(Rect a, Rect b, ISU isu) {
		this.a = a;
		this.b = b;
		this.isu = isu;
	}

	// 1. Récupéter les axes
	public ISU.Vector axis(Rect r, int i) {
		double angle = Math.toRadians(r.angle() + (i == 0 ? 0 : 90));

		return isu.new Vector(Math.cos(angle), Math.sin(angle));
	}

	// 2. projection
	public double[] project(Rect r, ISU.Vector axis) {
	    double[][] pts = getCorners(r);
	    double min = Double.POSITIVE_INFINITY;
	    double max = Double.NEGATIVE_INFINITY;

	    for (double[] p : pts) {
	        double proj = p[0] * axis.x() + p[1] * axis.y();
	        min = Math.min(min, proj);
	        max = Math.max(max, proj);
	    }
	    return new double[] { min, max };
	}
	
	// 3. coins du rectangle
	private double[][] getCorners(Rect r) {
	    double hw = r.halfW(), hh = r.halfH();
	    double rad = Math.toRadians(r.angle());
	    double cos = Math.cos(rad), sin = Math.sin(rad);

	    return new double[][] {
	        rotate(-hw, -hh, cos, sin),
	        rotate( hw, -hh, cos, sin),
	        rotate(-hw,  hh, cos, sin),
	        rotate( hw,  hh, cos, sin)
	    };
	}

	private double[] rotate(double lx, double ly, double cos, double sin) {
	    return new double[]{ lx*cos - ly*sin, lx*sin + ly*cos };
	}

	private boolean overlapOnAxis(ISU.Vector axis) {
	    double[] p1 = project(a, axis);
	    double[] p2 = project(b, axis);

	    Axis xAxis = isu.xAxis();
	    Axis yAxis = isu.yAxis();

	    double centerA = a.center().x() * axis.x() + a.center().y() * axis.y();
	    double dx = xAxis.distance(a.center().x(), b.center().x());
	    double dy = yAxis.distance(a.center().y(), b.center().y());
	    double centerB = centerA + dx * axis.x() + dy * axis.y();

	    double min1 = p1[0] + centerA, max1 = p1[1] + centerA;
	    double min2 = p2[0] + centerB, max2 = p2[1] + centerB;

	    return !(max1 < min2 || max2 < min1);
	}

	public boolean intersects() {

		return overlapOnAxis(axis(a, 0)) && overlapOnAxis(axis(a, 1)) && overlapOnAxis(axis(b, 0))
				&& overlapOnAxis(axis(b, 1));

	}

}
