package engine.entity;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import engine.brain.Stunt;
import engine.collision.Bounding;
import engine.core.Grid;
import engine.core.ISU;
import engine.core.ISU.Coord;
import engine.view.Avatar;
import engine.core.ZLayer;

public class Entity {

	private static final double EPS = 1e-9;
	private double walkSpeed = 0.1; // cm/s

	// FIELDS

	private engine.gal.arguments.Category category;
	private engine.gal.Bot galBot;
	private ZLayer zlayer = ZLayer.REGULAR;
	private Avatar avatar;
	private Grid grid;
	private ISU isu;
	private String name;
	private Set<Grid.Cell> occupied = new HashSet<>();

	private ISU.Dimension size; // dimension de l'entité
	private ISU.Dimension step; // à garder si demandé par l'énoncé
	private Grid.Position position;
	private ISU.Coord center;
	protected Bounding bounding;
	private ISU.Vector lSpeed; // linear speed
	private double aSpeed; // anglular speed

	private int orientation_degree;

	private Stunt stunt;

	private int healthPoints = 100;

	// CONSTRUCTOR

	public Entity(String name) {
		assert name != null;
		this.name = name;
		this.orientation_degree = 0;
		this.bounding = new Bounding();
	}

	// BINDING

	public void bind(Grid grid, ISU isu) {
		assert grid != null;
		assert isu != null;
		this.grid = grid;
		this.isu = isu;
	}

	// SETTERS

	public void setBot(engine.gal.Bot b) { this.galBot = b; }


	public void setZLayer(ZLayer z){
		this.zlayer = z;
	}

	public void setAvatar(Avatar a) {
		this.avatar = a;
	}

	public void setHealthPoints(int hp) {
		this.healthPoints = hp;
	}

	public void setWalkSpeed(double s) {
		this.walkSpeed = s;
	}

	public void setPosition(Grid.Position position) {
		assert position != null;
		retract();
		this.position = position.copy();
		this.setCenter(this.position.toISUCoordCentered());
		updateBounding();
		deploy();
		checkInvariant();
	}

	public void setCoord(ISU.Coord center) {

		assert center != null;
		assert grid != null;

		retract();

		this.setCenter(center.mkCopy());
		this.position = this.center.toGridPosition();

		updateBounding();
		deploy();
		checkInvariant();
	}

	public void setSize(Grid.Dimension dimension) {
		assert dimension != null;
		this.size = dimension.toISUDimension();
		updateBounding();
		checkInvariant();
	}

	public void setSize(ISU.Dimension dimension) {
		assert dimension != null;
		this.size = dimension.isu().new Dimension(dimension.x(), dimension.y());
		updateBounding();
		checkInvariant();
	}

	public void setStunt(Stunt s) {
		this.stunt = s;
	}

	public void setASpeed(double deg) {
		this.aSpeed = deg;
	}

	public void setLSpeed(ISU.Vector v) {
		this.lSpeed = v;
	}

	/* bounding */
	protected void setBounding(Bounding b) {
		this.bounding = b;
	}

	protected void updateBounding() {
	}

	// GETTERS

	public void setCategory(engine.gal.arguments.Category c) { this.category = c; }

	
	public engine.gal.arguments.Category category() { return category; }


	public engine.gal.Bot bot() { return galBot; }

	public ZLayer getZLayer() {
		return zlayer;
	}

	public Avatar avatar() {
		return avatar;
	}

	public int healthPoints() {
		return healthPoints;
	}

	public double walkSpeed() {
		return this.walkSpeed;
	}

	public ISU.Dimension size() {
		return (size == null) ? null : size.isu().new Dimension(size.x(), size.y());
	}

	public ISU.Coord center() {
		return (center == null) ? null : center.mkCopy();
	}

	public Grid.Position position() {
		return (position == null) ? null : position.copy();
	}

	public int orientation() {
		return orientation_degree;
	}

	public Grid grid() {
		return this.grid;
	}

	public ISU isu() {
		return this.isu;
	}

	public String name() {
		return this.name;
	}

	public Bounding bounding() {
		return this.bounding;
	}

	public ISU.Vector lSpeed() {
		return lSpeed == null ? null : isu.new Vector(lSpeed.x(), lSpeed.y());
	}

	public double aSpeed() {
		return this.aSpeed;
	}

	public Stunt stunt() {
		return stunt;
	}

	public boolean isMoving() {
		if (lSpeed == null)
			return false;
		return lSpeed.norm() > EPS || Math.abs(aSpeed) > EPS;
	}

	public void stopMoving() {
		this.lSpeed = null;
		this.aSpeed = 0;
	}

	public void setLSpeed(double x, double y) {
		this.lSpeed = isu.new Vector(x, y);
	}
	// INTERSECTION

	public boolean intersects(Entity e) {
		assert e != null;
		assert bounding != null;
		assert e.bounding != null;
		return bounding.intersects(e.bounding);
	}

	// TRANSLATION
	public void translate(Grid.Vector v) {
		retract();
		position.translate(v);
		setCenter(position.toISUCoordCentered());
		updateBounding();
		deploy();
		checkInvariant();
	}

	public void translate(ISU.Vector v) {
		retract();
		center.translate(v);
		position = center.toGridPosition();
		updateBounding();
		deploy();
		checkInvariant();
	}

	public void turn(int angle_degree) {
		orientation_degree = normalizeAngle(orientation_degree + angle_degree);
		updateBounding();
		checkInvariant();
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("Entity:");
		ps.println("  name = " + name);
		ps.println("  orientation_degree = " + orientation_degree);

		ps.print("  position = ");
		if (position == null) {
			ps.println("null");
		} else {
			position.show(ps);
		}

		ps.print("  center = ");
		if (center() == null) {
			ps.println("null");
		} else {
			center.show(ps);
		}

		ps.print("  size = ");
		if (size == null) {
			ps.println("null");
		} else {
			size.show(ps);
		}

		ps.print("  step = ");
		if (step == null) {
			ps.println("null");
		} else {
			step.show(ps);
		}
	}

	// === MOVE ===

	/*
	 * public void moveNorth(int nStep) { assert nStep >= 0; assert grid != null;
	 * translate(grid.new Vector(0, -nStep)); }
	 * 
	 * public void moveSouth(int nStep) { assert nStep >= 0; assert grid != null;
	 * translate(grid.new Vector(0, nStep)); }
	 * 
	 * public void moveEast(double length_cm) { assert length_cm >= 0; assert isu !=
	 * null; translate(isu.new Vector(length_cm, 0.0)); }
	 * 
	 * public void moveWest(double length_cm) { assert length_cm >= 0; assert isu !=
	 * null; translate(isu.new Vector(-length_cm, 0.0)); }
	 */

	// HELPERS

	private void checkInvariant() {
		assert orientation_degree >= 0;
		assert orientation_degree < 360;

		if (position != null) {
			assert center() != null;
		}
		if (center() != null) {
			assert position != null;
		}
	}

	private int normalizeAngle(int angle_degree) {
		int a = angle_degree % 360;
		if (a < 0) {
			a += 360;
		}
		return a;
	}

	protected void setCenter(ISU.Coord center) {
		this.center = center;
	}

	// deploy / rectract

	public Set<Grid.Cell> computeOccupied() {
		Set<Grid.Cell> cells = new HashSet<>();
		double s = grid().cellSizeCm();

		double hw = size().x() / 2.0;
		double hh = size().y() / 2.0;

		int x0 = (int) Math.floor((center().x() - hw) / s);
		int x1 = (int) Math.floor((center().x() + hw) / s);
		int y0 = (int) Math.floor((center().y() - hh) / s);
		int y1 = (int) Math.floor((center().y() + hh) / s);

		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				cells.add(grid().cellAt(grid().new Position(x, y)));
			}
		}

		return cells;
	}

	
	// no assert
	public void deploy() {
	    if (grid == null || size == null || center == null) return;
	    occupied = computeOccupied();
	    for (Grid.Cell c : occupied) {
	        c.add(this);
	    }
	}

	public void retract() {
		for (Grid.Cell c : occupied) {
			c.remove(this);
		}

		occupied.clear();
	}

	public void occupy(Grid.Position position) {
		Grid.Cell c = grid().cellAt(position);
		occupied.add(c);
		c.add(this);
	}

	public void collision(Entity other) {
		if (stunt != null)
			stunt.collision(other);
	}

	public void snapToTarget(Coord c) {
		setCoord(c);
	}

	public void takeDamage(int dmg) {
		assert dmg >= 0;
		healthPoints = Math.max(0, healthPoints - dmg);
	}

	public boolean isDead() {
		return healthPoints <= 0;
	}

}