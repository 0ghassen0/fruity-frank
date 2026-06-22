package frank.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import frank.game.Categorized;
import frank.game.Category;

public class Frank extends Entity implements Categorized {

    private static int lives = 3;
    private boolean hasCherry = false;
    private int facingDx = 1, facingDy = 0;

    public Frank(String name, Game game, int x, int y) {
        super(name, game);
        setStep(isu.new Vector(1.0, 1.0));
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.35 * game.getCmPerCell()));
        setBounding(b);
    }
    
    
    public int  lives()       { return lives; }
    public void loseLife()    { lives--; }
    public boolean isDead()   { return lives <= 0; }
    public static void resetLives() {lives = 3;}
    public boolean hasCherry(){ return hasCherry; }
    public void pickCherry()  { hasCherry = true; }
    public void useCherry()   { hasCherry = false; }
    public int  facingDx()    { return facingDx; }
    public int  facingDy()    { return facingDy; }
    public void setFacing(int dx, int dy) { if (dx != 0 || dy != 0) { facingDx = dx; facingDy = dy; } }

    @Override
    public Category category() { return Category.FRANK; }
}