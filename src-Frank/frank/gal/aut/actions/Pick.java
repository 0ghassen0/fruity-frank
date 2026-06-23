package frank.gal.aut.actions;

import engine.entities.Entity;
import engine.gal.aut.iGALAction;
import frank.game.FruityFrankModel;
import frank.game.GameApp;

// == PICK ==

/**
 * The Fruity Frank GAL {@code Pick} action: the pickup running this automaton
 * (a {@code Fruit} or {@code Cherry}) awards its value — score, fruit count, and
 * the eat sound — via {@link FruityFrankModel#awardPickup}. It does <i>not</i>
 * remove itself; the transition pairs {@code Pick} with the {@code ()} destroy
 * target, which removes the entity (e.g. {@code Idle | Got(@) ? Pick :()}).
 *
 * <p>
 * Mirrors the post-collect victory jingle of the imperative
 * {@code collectUnderFrank} so the two paths are interchangeable. Wired to the
 * spare {@code Pick} keyword in {@code GalCompiler} (no parser change).
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
