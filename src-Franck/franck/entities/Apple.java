package franck.entities;

import engine.core.Game;
import engine.entities.Bounding;
import engine.entities.Entity;
import engine.shapes.Circle;
import franck.Categorized;
import franck.Category;

public class Apple extends Entity implements Categorized {

    private boolean falling = false;
    private int fallDelay = -1; // -1 = pas armé

    public Apple(Game game, int x, int y) {
        super("Apple@" + x + "," + y, game);
        setStep(isu.new Vector(1.0, 1.0));
        setSize(grid.new Dimension(1, 1).toISUDimension());
        setPosition(grid.new Position(x, y));
        setBounding(game);
    }

    @Override
    protected void setBounding(Game game) {
        Bounding b = new Bounding(center.mkCopy());
        b.add(new Circle(center.mkCopy(), 0.40 * game.getCmPerCell()));
        setBounding(b);
    }

    public boolean isFalling()        { return falling; }
    public void setFalling(boolean f) { falling = f; }

    public boolean isArmed()   { return fallDelay >= 0; }
    public void arm(int ticks) { if (fallDelay < 0) fallDelay = ticks; }
    public void disarm()       { fallDelay = -1; }
    public void tickDelay()    { if (fallDelay > 0) fallDelay--; }
    public boolean delayDone() { return fallDelay == 0; }
    public int fallDelay()     { return fallDelay; }

    @Override
    public Category category() { return Category.APPLE; }
}