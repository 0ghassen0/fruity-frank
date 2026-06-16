package franck.ai;

import static engine.gal.aut.Gal.automaton;
import static engine.gal.aut.Gal.closest;
import static engine.gal.aut.Gal.move;
import static engine.gal.aut.Gal.on;
import static engine.gal.aut.Gal.opp;
import static engine.gal.aut.Gal.selectedDir;
import static engine.gal.aut.Gal.state;

import engine.gal.aut.Automaton;
import engine.gal.aut.Category;
import engine.gal.aut.State;

public final class FranckAutomata {

    private FranckAutomata() {}

    /** Poursuite directe de Frank. */
    public static Automaton enemyChase() {
        State s = state("Chase");
        return automaton("Chase", s,
            on(s, closest(Category.Adversary), move(selectedDir()), s));
    }

    /** Fuite de Frank (trajectoire différente). */
    public static Automaton enemyFlee() {
        State s = state("Flee");
        return automaton("Flee", s,
            on(s, closest(Category.Adversary), move(opp(selectedDir())), s));
    }
}