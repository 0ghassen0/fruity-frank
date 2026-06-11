package engine.core;

import java.io.PrintStream;

public class ISU {

	// FIELDS

	private final Axis xAxis, yAxis;
	private Grid grid;
	private final Game game;

	// CONSTRUCTOR

	public ISU(Game game) {
		assert game != null;
		this.game = game;
		this.xAxis = new Axis(game.torusOnXaxis(), game.widthCm());
		this.yAxis = new Axis(game.torusOnYaxis(), game.heightCm());
	}

	// SETTER

	public void set(Grid grid) {
		assert grid != null;
		this.grid = grid;
	}

	// GETTERS

	public Axis xAxis() {
		return this.xAxis;
	}

	public Axis yAxis() {
		return this.yAxis;
	}

	public Grid grid() {
		return grid;
	}

	public Game game() {
		return game;
	}

	// == DIMENSION (cm) ==

	public class Dimension {

		private static final double EPS = 1e-9;
		private double x_cm, y_cm;

		public Dimension(double x_cm, double y_cm) {
			assert x_cm >= EPS;
			assert y_cm >= EPS;
			this.x_cm = x_cm;
			this.y_cm = y_cm;
		}

		public void normalize() {
		    assert x_cm > 0;
		    assert y_cm > 0;

		    assert x_cm < isu().xAxis().halfPerimeter();
		    assert y_cm < isu().yAxis().halfPerimeter();
		}

		void setxy(double x_cm, double y_cm) {
			assert x_cm >= EPS;
			assert y_cm >= EPS;

			this.x_cm = x_cm;
			this.y_cm = y_cm;
			normalize();
			checkInvariant();
		}

		public ISU isu() {
			return ISU.this;
		}

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Dimension)) return false;
			return equiv((Dimension) o);
		}

		boolean equiv(Dimension d) {
			if (d == null) return false;
			return Math.abs(x_cm - d.x_cm) < EPS
				&& Math.abs(y_cm - d.y_cm) < EPS;
		}

		public ISU.Vector mkScaledVector(double factor) {
			return new Vector(x_cm * factor, y_cm * factor);
		}

		public ISU.Vector mkScaledVector(double xFactor, double yFactor) {
			return new Vector(x_cm * xFactor, y_cm * yFactor);
		}

		public ISU.Vector mkVector() {
			return new Vector(x_cm, y_cm);
		}

		private void checkInvariant() {
			assert x_cm >= 0;
			assert y_cm >= 0;
			
			assert x_cm < isu().xAxis().halfPerimeter();
			assert y_cm < isu().yAxis().halfPerimeter();
		}

		public void show(PrintStream ps) {
			ps.println("ISU.Dimension(" + x_cm + ", " + y_cm + ")");
		}
	}

	// == POINT ==

	public class Coord {

		private static final double EPS = 1e-9;

		private double x_cm;
		private double y_cm;

		public Coord(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
			normalize();
			checkInvariant();
		}

		private void normalize() {
			x_cm = xAxis.normalize(x_cm);
			y_cm = yAxis.normalize(y_cm);
		}

		private void checkInvariant() {
			assert x_cm >= 0;
			assert x_cm < xAxis.perimeter();
			assert y_cm >= 0;
			assert y_cm < yAxis.perimeter();
		}

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

		public ISU isu() {
			return ISU.this;
		}

		public void show(PrintStream ps) {
			ps.println("ISU.Coord(" + x_cm + ", " + y_cm + ")");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Coord)) {
				return false;
			}
			Coord c = (Coord) o;
			return Math.abs(x_cm - c.x_cm) < EPS
				&& Math.abs(y_cm - c.y_cm) < EPS;
		}

		public ISU.Vector mkVectorToward(Coord target) {
			assert target != null;
			double dx = xAxis.distance(x_cm, target.x_cm);
			double dy = yAxis.distance(y_cm, target.y_cm);
			return new Vector(dx, dy);
		}

		public Grid.Position toGridPosition() {
		    double f = grid.cellSizeCm();

		    int x = (int) Math.floor((x_cm / f));
		    int y = (int) Math.floor((y_cm / f));

		    return grid.new Position(x, y);
		}

		public void translate(ISU.Vector v) {
			assert v != null;
			x_cm += v.x();
			y_cm += v.y();
			normalize();
			checkInvariant();
		}

		public ISU.Coord mkTranslated(ISU.Vector v) {
			assert v != null;
			Coord c = mkCopy();
			c.translate(v);
			return c;
		}

		public ISU.Coord mkCopy() {
			return new Coord(x_cm, y_cm);
		}

		public void rotation(int angle_degree) {
			double rad = Math.toRadians(angle_degree);
			double newX = x_cm * Math.cos(rad) - y_cm * Math.sin(rad);
			double newY = x_cm * Math.sin(rad) + y_cm * Math.cos(rad);
			x_cm = newX;
			y_cm = newY;
			normalize();
			checkInvariant();
		}

		public void rotateAround(Coord center, int angle_degree) {
			assert center != null;

			x_cm -= center.x_cm;
			y_cm -= center.y_cm;

			rotation(angle_degree);

			x_cm += center.x_cm;
			y_cm += center.y_cm;

			normalize();
			checkInvariant();
		}

		public double distanceTo(Coord pt) {
			assert pt != null;
			double dx = xAxis.distance(x_cm, pt.x_cm);
			double dy = yAxis.distance(y_cm, pt.y_cm);
			return Math.sqrt(dx * dx + dy * dy);
		}
	}

	// == VECTOR ==

	public class Vector {

		private static final double EPS = 1e-9;

		private double x_cm;
		private double y_cm;

		public Vector(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
		}

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

		public void add(Vector v) {
			assert v != null;
			x_cm += v.x_cm;
			y_cm += v.y_cm;
		}

		public void scale(double factor) {
			x_cm *= factor;
			y_cm *= factor;
		}

		public void scale(double xFactor, double yFactor) {
			x_cm *= xFactor;
			y_cm *= yFactor;
		}

		public double dot(ISU.Vector v) {
			assert v != null;
			return x_cm * v.x_cm + y_cm * v.y_cm;
		}

		public double norm() {
			return Math.sqrt(x_cm * x_cm + y_cm * y_cm);
		}

		public void unity() {
			double n = norm();
			assert n > EPS;
			x_cm /= n;
			y_cm /= n;
			assert Math.abs(norm() - 1.0) < 1e-6;
		}

		public void turn(int angle_degree) {
			double rad = Math.toRadians(angle_degree);
			double newX = x_cm * Math.cos(rad) - y_cm * Math.sin(rad);
			double newY = x_cm * Math.sin(rad) + y_cm * Math.cos(rad);
			x_cm = newX;
			y_cm = newY;
		}

		public void show(PrintStream ps) {
			ps.println("ISU.Vector(" + x_cm + ", " + y_cm + ")");
		}
	}
}