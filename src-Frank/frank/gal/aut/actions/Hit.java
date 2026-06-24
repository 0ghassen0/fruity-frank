package frank.gal.aut.actions;

import engine.entities.Entity;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALAction;
import frank.entities.CherryProjectile;
import frank.entities.Enemy;
import frank.entities.Frank;
import frank.game.FruityFrankModel;
import frank.game.GameApp;
import frank.game.GameState;

// == HIT ==

/**
 * The Fruity Frank GAL {@code Hit} action: the aggressor running this automaton
 * (a cherry projectile, a falling apple, or an enemy) applies its effect to the
 * {@code $} target a guard such as {@code Got} just selected.
 *
 * <ul>
 * <li>target is {@code Frank} → {@link FruityFrankModel#killFrank} plus the
 * death / game-over sound.</li>
 * <li>target is an {@code Enemy} → remove it, award the kill, play the
 * explosion. The score is aggressor-specific (cherry = 300, falling apple =
 * 500), matching the old collision rules; a spent cherry projectile also removes
 * itself.</li>
 * </ul>
 *
 * <p>
 * Returns {@code false} (the transition does not fire) when there is no target
 * or it is neither Frank nor an enemy, so a paired {@code :()} destroy target is
 * only taken on a real hit. Wired to the spare {@code Hit} keyword in
 * {@code GalCompiler} (no parser change).
 */
public final class Hit implements iGALAction {

	/** The single shared instance — it is stateless. */
	public static final Hit INSTANCE = new Hit();

	private Hit() {
	}

	@Override
	public boolean exec(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;
		Entity target = bot.selected(); // the GAL $ selection
		if (target == null || !(self.model() instanceof FruityFrankModel model))
			return false;
		GameState state = model.state();

		if (target instanceof Frank) {
			model.killFrank();
			if (state.lives() <= 0)
				GameApp.audio.gameOver();
			else
				GameApp.audio.death();
			return true;
		}

		if (target instanceof Enemy) {
			model.remove(target);
			state.addScore(self instanceof CherryProjectile ? 300 : 500);
			GameApp.audio.explosion();
			if (self instanceof CherryProjectile)
				model.remove(self); // the shot is spent on impact
			return true;
		}

		return false;
	}
}
