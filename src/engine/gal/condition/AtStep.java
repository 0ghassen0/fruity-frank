package engine.gal.condition;

import engine.core.Grid;
import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;

public class AtStep implements iGALCondition {

    private final Direction direction;
    private final Category category;

    public AtStep(Direction direction, Category category) {
        assert direction != null;
        assert category != null;
        this.direction = direction;
        this.category = category;
    }

    @Override
    public boolean eval(Entity e) {
        assert e != null;
        Grid.Position target = resolvePosition(e);
        if (target == null) return false;

        for (engine.entity.Entity other : e.grid().cellAt(target).entities()) {
            if (other == e) continue;
            if (matchesCategory(other, e)) return true;
        }

        // Void = cellule vide
        if (category == Category.Void) {
            return e.grid().cellAt(target).entities().stream()
                .filter(en -> en != e).findAny().isEmpty();
        }

        return false;
    }

    private Grid.Position resolvePosition(Entity e) {
        Grid.Position pos = e.position().copy();
        int angle = resolveAngle(e);

        if (angle == 0)   pos.translate(e.grid().new Vector(1, 0));
        else if (angle == 90)  pos.translate(e.grid().new Vector(0, 1));
        else if (angle == 180) pos.translate(e.grid().new Vector(-1, 0));
        else if (angle == 270) pos.translate(e.grid().new Vector(0, -1));
        else return e.position().copy(); // H = Here

        return pos;
    }

    private int resolveAngle(Entity e) {
        if (direction.isAbsolute()) return direction.toAngle();
        // relatif : F = orientation actuelle
        int base = e.orientation();
        int delta = direction.toAngle();
        return (base + delta) % 360;
    }

    private boolean matchesCategory(Entity other, Entity self) {
        if (category == Category.Any) return true;
        if (category == Category.Adversary)
            return other.category() != null && other.category().interactsWith(self.category());
        return other.category() == category;
    }
}