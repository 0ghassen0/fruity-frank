package engine.brain;

import engine.entity.Entity;

public abstract class Bot {

    protected final BasicStunt stunt;

    public Bot(BasicStunt stunt) {
        assert stunt != null;
        this.stunt = stunt;
        stunt.setBot(this);
    }

    public abstract void think();
    public abstract void done();
    public abstract void collision(Entity other);
}