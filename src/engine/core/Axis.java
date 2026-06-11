package engine.core;

public class Axis {

	private final boolean onTorus;
	private final double perimeter;
	private final double halfPerimeter;
	private final double EPS = 1e-9;
	
	public Axis(boolean onTorus, double perimeter) {
		assert perimeter > 0;
		this.onTorus = onTorus;
		this.perimeter = perimeter;
		this.halfPerimeter = perimeter / 2.0;
	}

	public boolean onTorus() {
		return onTorus;
	}

	public double perimeter() {
		return perimeter;
	}

	public double halfPerimeter() {
		return halfPerimeter;
	}

	public int normalize(int length) {
		if (!onTorus) {
			return length;
		}
		return modp(length, (int) Math.round(perimeter));
	}

	private int modp(int length, int perimeter) {
		assert perimeter > 0;
		return ((length % perimeter) + perimeter) % perimeter;
	}

	public double normalize(double length) {
		if (!onTorus) {
			return length;
		}
		double result = modp(length, perimeter);
		assert result >= 0.0 - EPS;
		assert result < perimeter + EPS;
		return result;
	}

	private double modp(double length, double perimeter) {
		assert perimeter > 0;
		return ((length % perimeter) + perimeter) % perimeter;
	}

	public double distance(double position1, double position2) {
	    if (!onTorus) return Math.abs(position2 - position1);
	    double d = modp(position2 - position1, perimeter);
	    if (d > halfPerimeter) d -= perimeter;
	    return d;
	}

	public double euclidean(double xmin, double xmax) {
		if (!onTorus) {
			return xmax;
		}
		if (xmax < xmin) {
			return xmax + perimeter;
		}
		return xmax;
	}
}