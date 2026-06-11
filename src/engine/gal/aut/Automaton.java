package engine.gal.aut;

import engine.entity.Entity;
import engine.gal.State;
import java.util.*;

public class Automaton {

    private final String name;
    public final State initial;
    private final Map<State, List<Transition>> transitions;

    public Automaton(String name, State initial) {
        assert name != null;
        assert initial != null;
        this.name = name;
        this.initial = initial;
        this.transitions = new LinkedHashMap<>();
    }

    public void add(Transition t) {
        assert t != null;
        transitions
            .computeIfAbsent(t.src, k -> new ArrayList<>())
            .add(t);
    }

    public boolean step(Entity e) {
        assert e != null;
        State current = e.bot().state();
        List<Transition> list = transitions.get(current);
        if (list == null) return false;
        for (Transition t : list) {
            if (t.exec(e)) return true;
        }
        return false;
    }
}