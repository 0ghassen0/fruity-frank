package engine.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.entity.Entity;

public class Grid {

	// FIELDS

	private final Game game;
	private final ISU isu;
	private final Axis xAxis, yAxis;

	private final int width_ncell, height_ncell;

	private final Cell[][] grid;

	// CONSTRUCTOR

	public Grid(Game game) {
		assert game != null;

		this.game = game;
		this.width_ncell = game.widthNcell();
		this.height_ncell = game.heightNcell();

		this.xAxis = new Axis(game.torusOnXaxis(), width_ncell);
		this.yAxis = new Axis(game.torusOnYaxis(), height_ncell);

		this.isu = game.isu();
		assert isu != null;

		this.grid = new Cell[width_ncell][height_ncell];

		init();
	}

	// INIT

	private void init() {
		for (int x = 0; x < width_ncell; x++) {
			for (int y = 0; y < height_ncell; y++) {
				grid[x][y] = new Cell(new Position(x, y));
			}
		}
	}

	// GETTERS

	public int width() {
		return width_ncell;
	}

	public int height() {
		return height_ncell;
	}

	public Game game() {
		return game;
	}

	public Cell cellAt(Grid.Position p) {
		assert p != null;

		Position copy = p.copy();
		copy.normalize();

		return grid[copy.x()][copy.y()];
	}

	public double cellSizeCm() {
		return game.cmPerCell();
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("Grid:");
		ps.println("  width_ncell = " + width_ncell);
		ps.println("  height_ncell = " + height_ncell);
		ps.println("  cellSizeCm = " + cellSizeCm());
	}

	// ============================================================
	// == DIMENSION (nb cell) ==
	// ============================================================

	public class Dimension {
		private int x_ncell, y_ncell;

		public Dimension(int x_ncell, int y_ncell) {
			assert x_ncell >= 0;
			assert y_ncell >= 0;

			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

		public void normalize() {
			// size is not wrapped; just kept as-is
			assert x_ncell >= 0;
			assert y_ncell >= 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Dimension)) {
				return false;
			}
			return equiv((Dimension) o);
		}

		public boolean equiv(Dimension d) {
			if (d == null) {
				return false;
			}
			return x_ncell == d.x_ncell && y_ncell == d.y_ncell;
		}

		public ISU.Dimension toISUDimension() {
			double factor = cellSizeCm();
			return isu.new Dimension(x_ncell * factor, y_ncell * factor);
		}

		public void show(PrintStream ps) {
			ps.println("Grid.Dimension(" + x_ncell + ", " + y_ncell + ")");
		}
	}

	// ============================================================
	// == VECTOR ==
	// ============================================================

	public class Vector {

		private int x_ncell, y_ncell;

		public Vector(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

		public void add(Vector v) {
			assert v != null;
			x_ncell += v.x_ncell;
			y_ncell += v.y_ncell;
		}

		public void show(PrintStream ps) {
			ps.println("Grid.Vector(" + x_ncell + ", " + y_ncell + ")");
		}
	}

	// ============================================================
	// == POINT ==
	// ============================================================

	public class Position {

		private int x_ncell, y_ncell;

		public Position(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
			normalize();
		}

		public Grid.Position copy() {
			return new Position(x_ncell, y_ncell);
		}

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Position)) {
				return false;
			}
			Position p = (Position) o;
			return x_ncell == p.x_ncell && y_ncell == p.y_ncell;
		}

		public void normalize() {
			if (xAxis.onTorus()) {
				x_ncell = xAxis.normalize(x_ncell);
			} else {
				assert x_ncell >= 0 && x_ncell < width_ncell;
			}

			if (yAxis.onTorus()) {
				y_ncell = yAxis.normalize(y_ncell);
			} else {
				assert y_ncell >= 0 && y_ncell < height_ncell;
			}
		}

		public void translate(Vector v) {
			assert v != null;
			x_ncell += v.x_ncell;
			y_ncell += v.y_ncell;
			normalize();
		}

		public void moveNorth(int n_ncell) {
			assert n_ncell >= 0;
			y_ncell -= n_ncell;
			normalize();
		}

		public void rotateAround(Grid.Position position, int angle_degree) {
			assert position != null;

			double dx = x_ncell - position.x_ncell;
			double dy = y_ncell - position.y_ncell;

			double rad = Math.toRadians(angle_degree);

			double newX = dx * Math.cos(rad) - dy * Math.sin(rad);
			double newY = dx * Math.sin(rad) + dy * Math.cos(rad);

			x_ncell = position.x_ncell + (int) Math.round(newX);
			y_ncell = position.y_ncell + (int) Math.round(newY);

			normalize();
		}

		public double distanceTo(Position p) {
			assert p != null;

			double dx = xAxis.distance(x_ncell, p.x_ncell);
			double dy = yAxis.distance(y_ncell, p.y_ncell);

			return Math.sqrt(dx * dx + dy * dy);
		}

		public ISU.Coord toISUCoord() {
			double factor = cellSizeCm();
			return isu.new Coord(
				x_ncell * factor - factor / 2.0,
				y_ncell * factor - factor / 2.0
			);
		}

		public ISU.Coord toISUCoordCentered() {
		    double factor = cellSizeCm();

		    return isu.new Coord(
		        (x_ncell + 0.5) * factor,
		        (y_ncell + 0.5) * factor
		    );
		}

		public void show(PrintStream ps) {
			ps.println("Grid.Position(" + x_ncell + ", " + y_ncell + ")");
		}
	}

	// ============================================================
	// == CELL ==
	// ============================================================

	public class Cell {

		private final Grid.Dimension size;
		private final Grid.Position position;
		private final List<Entity> entities;

		public Cell(Position p) {
			assert p != null;

			this.position = p.copy();
			this.size = new Dimension(1, 1);
			this.entities = new ArrayList<>();
		}

		public Grid.Dimension size() {
			return new Dimension(size.x(), size.y());
		}

		public Grid.Position position() {
			return position.copy();
		}

		public List<Entity> entities() {
			return Collections.unmodifiableList(entities);
		}

		public void add(Entity e) {
			assert e != null;
			if (!entities.contains(e)) {
				entities.add(e);
			}
		}

		public void remove(Entity e) {
			assert e != null;
			entities.remove(e);
		}

		public boolean contains(Entity e) {
			return entities.contains(e);
		}

		public void show(PrintStream ps) {
			ps.println("Grid.Cell at " + position.x() + ", " + position.y());
			ps.println("  entities = " + entities.size());
		}
	}
}