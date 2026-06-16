package frank.game;

import engine.core.Game;

public class FruityFrank extends Game {
	// torus X = true, Y = false ; cellule = 10 cm ; 4 px/cm => 40 px/cellule
	public FruityFrank(int cols, int rows) {
		super(cols, rows, true, false, 10.0, 4);
	}
}