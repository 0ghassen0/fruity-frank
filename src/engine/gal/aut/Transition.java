package engine.gal.aut;

import engine.entity.Entity;
import engine.gal.State;
import engine.gal.action.iGALAction;
import engine.gal.condition.iGALCondition;

public class Transition {

    final State src;
    final iGALCondition condition;
    final iGALAction action;
    final State tgt;

    public Transition(State src, iGALCondition condition, iGALAction action, State tgt) {
        assert src != null;
        assert condition != null;
        assert tgt != null;
        this.src = src;
        this.condition = condition;
        this.action = action;
        this.tgt = tgt;
    }

    public boolean exec(Entity e) {
        assert e != null;
        if (!condition.eval(e)) return false;
        if (action != null) action.exec(e);
        e.bot().state(tgt);
        return true;
    }
}