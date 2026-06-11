package game;

import engine.core.Game;

public class PacManGame extends Game{

	public PacManGame(double w_cm, double h_cm) {
        super(w_cm, h_cm); // Game(double,double) n'a pas de nom ni torus params
    }

	public PacManGame(int w, int h) {
	    super("PacMan", w, h, true, true);
	}
	
}


