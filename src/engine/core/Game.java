package engine.core;

import java.io.PrintStream;

public abstract class Game {


	// CONSTANTS

	private static final double EPS = 1e-9;
	private final double cmPerCell = 3.7;
	private final int pixelPerCm = 2;

	// FIELDS
	
	private boolean torusOnXaxis = true;
	private boolean torusOnYaxis = true;

	private int width_ncell;
	private int height_ncell;
	private String name = "Game";

	private double width_cm;
	private double height_cm;

	private Grid grid;
	private ISU isu;

	// CONSTRUCTORS

	public Game(String name,int w_ncell, int h_ncell, boolean torusOnXaxis, boolean torusOnYaxis) {
		assert w_ncell > 0;
		assert h_ncell > 0;
		assert cmPerCell > 0;
		assert pixelPerCm > 0;
		assert name != null && name != "";
		
	
		this.width_ncell = w_ncell;
		this.height_ncell = h_ncell;
		this.width_cm = width_ncell * cmPerCell;
		this.height_cm = height_ncell * cmPerCell;
		this.torusOnXaxis = torusOnXaxis;
		this.torusOnYaxis = torusOnYaxis;
		this.name = name;

		checkInvariant();
		initSubsystems();
	}

	public Game(double w_cm, double h_cm) {
	    this("Game", 
	         (int) Math.round(w_cm / 3.7), 
	         (int) Math.round(h_cm / 3.7), 
	         true, true);
	}

	private void initSubsystems() {
		this.isu = new ISU(this);
		this.grid = new Grid(this);
		this.isu.set(this.grid);
	}

	private void checkInvariant() {
		assert width_ncell > 0;
		assert height_ncell > 0;
		assert cmPerCell > 0;
		assert pixelPerCm > 0;

		assert Math.abs(width_cm - width_ncell * cmPerCell) < EPS;
		assert Math.abs(height_cm - height_ncell * cmPerCell) < EPS;
	}
	
	// SETTERS
	
	public void setTorusX() {
		this.torusOnXaxis = true;
	}
	
	public void unsetTorusX() {
		this.torusOnXaxis = false;
	}
	
	public void setTorusY() {
		this.torusOnYaxis = true;
	}
	
	public void unsetTorusY() {
		this.torusOnYaxis = false;
	}

	// GETTERS

	public boolean torusOnXaxis() {
		return torusOnXaxis;
	}

	public boolean torusOnYaxis() {
		return torusOnYaxis;
	}

	public double cmPerCell() {
		return cmPerCell;
	}

	public int pixelPerCm() {
		return pixelPerCm;
	}

	public int widthNcell() {
		return width_ncell;
	}

	public int heightNcell() {
		return height_ncell;
	}

	public double widthCm() {
		return width_cm;
	}

	public double heightCm() {
		return height_cm;
	}

	public Grid grid() {
		return grid;
	}

	public ISU isu() {
		return isu;
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.println("Game:");
		ps.println("  width_ncell = " + width_ncell);
		ps.println("  height_ncell = " + height_ncell);
		ps.println("  width_cm = " + width_cm);
		ps.println("  height_cm = " + height_cm);
		ps.println("  cmPerCell = " + cmPerCell);
		ps.println("  pixelPerCm = " + pixelPerCm);
		ps.println("  torusX = " + torusOnXaxis);
		ps.println("  torusY = " + torusOnYaxis);
	}
}