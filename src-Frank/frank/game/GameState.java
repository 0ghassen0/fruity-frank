package frank.game;

public class GameState {
    private int score;
    private int highScore;
    private boolean gameOver;
    private boolean frankDied;

    public int     score()      { return score; }
    public int     highScore()  { return highScore; }
    public boolean isGameOver() { return gameOver; }
    public boolean frankDied()  { return frankDied; }
    public void    clearDeath() { frankDied = false; }

    public void addScore(int p) { score += p; if (score > highScore) highScore = score; }
    public void setGameOver()   { gameOver = true; }
    public void killedByEnemy() { frankDied = true; }
}