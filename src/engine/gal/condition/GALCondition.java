package engine.gal.condition;

import engine.entity.Entity;

public class GALCondition {

    public static final iGALCondition TRUE = new iGALCondition() {
        public boolean eval(Entity e) { return true; }
    };

    public static iGALCondition and(iGALCondition a, iGALCondition b) {
        return e -> a.eval(e) && b.eval(e);
    }

    public static iGALCondition or(iGALCondition a, iGALCondition b) {
        return e -> a.eval(e) || b.eval(e);
    }

    public static iGALCondition not(iGALCondition a) {
        return e -> !a.eval(e);
    }
}