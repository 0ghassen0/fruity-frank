package frank.game;

public class GameState {
    private int score;
    private int highScore;

    private boolean gameOver;
    private boolean frankDied;

    private int fruitsRemaining;
    private boolean victory;

    public int score() {
        return score;
    }

    public int highScore() {
        return highScore;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean frankDied() {
        return frankDied;
    }

    public boolean isVictory() {
        return victory;
    }

    public int fruitsRemaining() {
        return fruitsRemaining;
    }

    public void clearDeath() {
        frankDied = false;
    }

    public void addScore(int p) {
        score += p;
        if (score > highScore)
            highScore = score;
    }

    public void setGameOver() {
        gameOver = true;
    }

    public void killedByEnemy() {
        frankDied = true;
    }

    public void setFruitsRemaining(int n) {
        fruitsRemaining = Math.max(0, n);
        victory = fruitsRemaining == 0;
    }

    public void collectFruit() {
        if (gameOver || victory)
            return;

        fruitsRemaining--;

        if (fruitsRemaining <= 0) {
            fruitsRemaining = 0;
            victory = true;
        }
    }
}