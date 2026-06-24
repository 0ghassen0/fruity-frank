package frank.gal.aut.actions;

import engine.entities.Entity;
import engine.gal.aut.iGALAction;
import frank.game.FruityFrankModel;
import frank.game.GameApp;

// == PICK ==

/**
 * The Fruity Frank GAL {@code Pick} action: the pickup running this automaton
 * (a {@code Fruit} or {@code Cherry}) awards its value
 *
 */
public final class Pick implements iGALAction {

	/** The single shared instance — it is stateless. */
	public static final Pick INSTANCE = new Pick();

	private Pick() {
	}

	@Override
	public boolean exec(Entity self) {
		if (!(self.model() instanceof FruityFrankModel model))
			return false;
		model.awardPickup(self);
		if (model.state().isVictory()) {
			GameApp.audio.win();
			GameApp.audio.stopMusic();
		}
		return true;
	}
}
