package game.bots;

import engine.brain.BasicStunt;
import engine.brain.Bot;
import engine.entity.Entity;

public class PlayerBot extends Bot {

    private int pendingDegree = -1;  // commande mémorisée
    private boolean hasPending = false;

    public PlayerBot(BasicStunt stunt) {
        super(stunt);
    }

    // appelé par le Controller
    public void command(int degree) {
        this.pendingDegree = degree;
        this.hasPending = true;
        // si l'entité n'est pas en mouvement, appliquer immédiatement
        if (!stunt.entity().isMoving()) {
            applyPending();
        }
    }

    @Override
    public void think() {
        // pas utilisé pour le joueur
    }

    @Override
    public void done() {
        // arrivée au centre d'une cellule
        if (hasPending) {
            applyPending();
        }
    }

    @Override
    public void collision(Entity other) {
        stunt.entity().stopMoving();
        hasPending = false;
    }

    private void applyPending() {
        hasPending = false;
        stunt.walk(pendingDegree);
    }
}