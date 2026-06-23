package frank.ai;

import static engine.gal.aut.Gal.automaton;
import static engine.gal.aut.Gal.closest;
import static engine.gal.aut.Gal.move;
import static engine.gal.aut.Gal.on;
import static engine.gal.aut.Gal.opp;
import static engine.gal.aut.Gal.selectedDir;
import static engine.gal.aut.Gal.state;

import engine.entities.Entity;
import engine.gal.aut.Automaton;
import engine.gal.aut.Category;
import engine.gal.aut.DirArg;
import engine.gal.aut.GALBot;
import engine.gal.aut.State;
import engine.geometry.Grid;
import engine.geometry.ISU;

public final class FrankAutomata {

    private FrankAutomata() {}

    /** Poursuite de Frank avec contournement des obstacles (gazon, pommes). */
    public static Automaton enemyChase() {
        State s = state("Chase");
        return automaton("Chase", s,
            on(s, closest(Category.Adversary), move(chaseDir(false)), s));
    }

    /** Fuite de Frank, même contournement. */
    public static Automaton enemyFlee() {
        State s = state("Flee");
        return automaton("Flee", s,
            on(s, closest(Category.Adversary), move(chaseDir(true)), s));
    }

    /**
     * Direction vers (ou à l'opposé de) la cible sélectionnée, en ne choisissant
     * QUE parmi les directions dont la case voisine est franchissable (canOccupy).
   */
    private static DirArg chaseDir(boolean flee) {
        final int[] last = { 1, 0 };
        return self -> {
            if (!(self.bot() instanceof GALBot b) || b.selected() == null)
                return new int[] { 0, 0 };
            
            ISU.Coord selfcenter = self.center();
            ISU.Vector d = selfcenter.nearestDeltaTo(b.selected().center());
            double cm = selfcenter.getISU().getCmPerCell();
            int sign = flee ? -1 : 1;
            int towardX = (d.x() < 0 ? -1 : (d.x() > 0 ? 1 : 0)) * sign;
            int towardY = (d.y() < 0 ? -1 : (d.y() > 0 ? 1 : 0)) * sign;

            boolean xDominant = Math.abs(d.x()) >= Math.abs(d.y());
            int[][] prefs = xDominant
                ? new int[][] { {towardX,0}, {0,towardY}, {0,-towardY}, {-towardX,0} }
                : new int[][] { {0,towardY}, {towardX,0}, {-towardX,0}, {0,-towardY} };

            for (int[] p : prefs) {
                if (p[0] == 0 && p[1] == 0) continue;
                if (dirIsOpen(self, p[0], p[1], cm)) {
                    boolean changeAxe = (p[0] != 0 && last[0] == 0) || (p[1] != 0 && last[1] == 0);
                    if (changeAxe)
                        self.setCoord(self.position().toISUCoordCentered());
                    last[0] = p[0]; last[1] = p[1];
                    return new int[] { p[0], p[1] };
                }
            }
            return new int[] { last[0], last[1] };  // tout bloqué : on garde le cap
        };
    }

    /** La case voisine dans (dx,dy) est-elle franchissable pour self  */
    private static boolean dirIsOpen(Entity self, int dx, int dy, double cm) {
        if (self.model() == null) return false;
        ISU.Coord cc = self.position().toISUCoordCentered();
        ISU.Vector step = cc.getISU().new Vector(dx * cm, dy * cm);
        Grid.Position target = cc.mkTranslated(step).toGridPosition();
        return self.model().canOccupy(self, target);
    }
}