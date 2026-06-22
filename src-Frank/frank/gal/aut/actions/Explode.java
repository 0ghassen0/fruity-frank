package frank.gal.aut.actions;

import engine.core.Model;
import engine.entities.Entity;
import engine.gal.aut.iGALAction;

// == EXPLODE ==

/**
 * The GAL {@code Explode} action read as self-destruction: the entity removes
 * itself from the world (and is detached for GC). This is the immediate-effect
 * counterpart to reaching the {@code ()} destroy state — useful when
 * destruction is the action itself rather than a transition target.
 * Game-specific blast effects (damaging neighbours, spawning debris) are
 * layered on top by the game's own verbs; this base action only removes the
 * actor.
 */
public final class Explode implements iGALAction {

	/** The single shared instance — it is stateless. */
	public static final Explode INSTANCE = new Explode();

	private Explode() {
	}

	@Override
	public boolean exec(Entity self) {
		Model model = self.model();
		if (model != null)
			model.remove(self);
		else
			self.destroy();
		return true;
	}
}
