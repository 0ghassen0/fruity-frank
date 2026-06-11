package engine.gal;

import engine.entity.Entity;
import engine.gal.aut.Automaton;

public class GALBot extends Bot {

    private Automaton automaton;

    public GALBot(Entity e) {
        super(e);
    }

    public void set(Automaton automaton) {
        assert automaton != null;
        this.automaton = automaton;
        this.state = automaton.initial;
    }

    @Override
    public void tick(double elapsed_ms) {
        if (automaton == null) return;
        if (State.DEAD.equals(state)) {
            // entité à détruire
            return;
        }
        automaton.step(entity);
    }

    @Override
    public void collision(Entity impactor, double elapsed_ms) {
        entity.stopMoving();
        if (automaton != null)
            automaton.step(entity);
    }
}