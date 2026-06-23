package frank.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class FruityFrankMap {

	public record Cell(int col, int row) {
	}

	private final int cols, rows;
	private final boolean[][] wall;

	private final List<Cell> grassCells = new ArrayList<>();
	private final List<Cell> fruitCells = new ArrayList<>();
	private final List<Cell> appleCells = new ArrayList<>();
	private final List<Cell> cherryCells = new ArrayList<>();
	private final List<Cell> enemySpawns = new ArrayList<>();
	private final Cell frankSpawn;

	public FruityFrankMap(GameConfig cfg) {
		this.cols = cfg.getInt("cols", 21);
		this.rows = cfg.getInt("rows", 15);
		this.wall = new boolean[cols][rows];

		// Murs haut et bas
		for (int c = 0; c < cols; c++) {
			wall[c][0] = true;
			wall[c][rows - 1] = true;
		}

		frankSpawn = new Cell(cols / 2, rows / 2);
		enemySpawns.add(new Cell(cfg.getInt("spawnerx", 2), cfg.getInt("spawnerx", 2)));
		enemySpawns.add(new Cell(cols - 3, 2));
		enemySpawns.add(new Cell(2, rows - 3));
		enemySpawns.add(new Cell(cols - 3, rows - 3));

		Set<Cell> reserved = new HashSet<>(enemySpawns);
		reserved.add(frankSpawn);

		for (int c = 0; c < cols; c++)
			for (int r = 1; r < rows - 1; r++)
				if (!wall[c][r] && !reserved.contains(new Cell(c, r)))
					grassCells.add(new Cell(c, r));

		Set<Cell> grassSet = new HashSet<>(grassCells);

		// Tous les ratios viennent de la config
		int fruitRatio = cfg.getInt("fruit_ratio", 5);
		int appleRatio = cfg.getInt("apple_ratio", 25);
		int cherryRatio = cfg.getInt("cherry_ratio", 18);

		// Fruits
		Random rf = new Random(42L);
		Set<Cell> used = new HashSet<>();
		int targetFruit = grassCells.size() / fruitRatio;
		for (int a = 0; fruitCells.size() < targetFruit && a < 100000; a++) {
			Cell c = grassCells.get(rf.nextInt(grassCells.size()));
			if (used.add(c))
				fruitCells.add(c);
		}

		// Pommes : case du dessous doit être du gazon
		Random ra = new Random(999L);
		int targetApple = Math.max(2, grassCells.size() / appleRatio);
		for (int a = 0; appleCells.size() < targetApple && a < 100000; a++) {
			Cell c = grassCells.get(ra.nextInt(grassCells.size()));
			Cell below = new Cell(c.col(), c.row() + 1);
			if (grassSet.contains(below) && used.add(c))
				appleCells.add(c);
		}

		// Cerises
		Random rc = new Random(777L);
		int targetCherry = Math.max(3, grassCells.size() / cherryRatio);
		for (int a = 0; cherryCells.size() < targetCherry && a < 100000; a++) {
			Cell c = grassCells.get(rc.nextInt(grassCells.size()));
			if (used.add(c))
				cherryCells.add(c);
		}

		System.out.println("[Map] grass=" + grassCells.size() + " fruit=" + fruitCells.size() + " apple="
				+ appleCells.size() + " cherry=" + cherryCells.size());
	}

	public boolean isWall(int col, int row) {
		int r = Math.floorMod(row, rows);
		int c = Math.floorMod(col, cols);
		return wall[c][r];
	}

	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public List<Cell> grassCells() {
		return grassCells;
	}

	public List<Cell> fruitCells() {
		return fruitCells;
	}

	public List<Cell> appleCells() {
		return appleCells;
	}

	public List<Cell> cherryCells() {
		return cherryCells;
	}

	public List<Cell> enemySpawns() {
		return enemySpawns;
	}

	public Cell frankSpawn() {
		return frankSpawn;
	}
}