package engine.gal;

import engine.brain.BasicStunt;
import engine.entity.Entity;

public abstract class Bot {

    protected final Entity entity;
    protected State state;
    protected int healthPercent = 100;

    public Bot(Entity entity) {
        assert entity != null;
        this.entity = entity;
        entity.setBot(this);
    }

    public State state() { return state; }
    public void state(State s) { this.state = s; }
    public Entity entity() { return entity; }

    public BasicStunt stunt() {
        assert entity.stunt() instanceof BasicStunt;
        return (BasicStunt) entity.stunt();
    }

    public abstract void tick(double elapsed_ms);
    public abstract void collision(Entity impactor, double elapsed_ms);
}