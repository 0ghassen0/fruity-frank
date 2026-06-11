package engine.gal.action;

import engine.brain.BasicStunt;
import engine.entity.Entity;
import engine.gal.arguments.Direction;

public class Move implements iGALAction {

    private final Direction direction;

    public Move() {
        this.direction = Direction.F;
    }

    public Move(Direction direction) {
        assert direction != null;
        this.direction = direction;
    }

    @Override
    public void exec(Entity e) {
        assert e != null;
        if (!(e.stunt() instanceof BasicStunt)) return;
        BasicStunt stunt = (BasicStunt) e.stunt();

        int angle = resolveAngle(e);
        stunt.walk(angle);
    }

    private int resolveAngle(Entity e) {
        if (direction.isAbsolute()) return direction.toAngle();
        int base = e.orientation();
        int delta = direction.toAngle();
        return (base + delta) % 360;
    }
}