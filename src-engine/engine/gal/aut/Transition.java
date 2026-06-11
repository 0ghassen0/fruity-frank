package engine.gal.aut;

import engine.entities.Bot;
import engine.entities.Entity;

// == TRANSITION ==

/**
 * One GAL transition {@code (src): condition ? action :(tgt)}. It fires only
 * when the bot is in {@code src} and {@code condition} holds; firing runs
 * {@code action} (if any) and switches the bot to {@code tgt}.
 */
public class Transition {

	private final State src;
	private final iGALCondition condition;
	private final iGALAction action;
	private final State tgt;

	public Transition(State src, iGALCondition condition, iGALAction action, State tgt) {
		this.src = src;
		this.condition = condition;
		this.action = action;
		this.tgt = tgt;
	}

	public State src() {
		return src;
	}

	/**
	 * Try to fire this transition for {@code e}.
	 *
	 * @return {@code true} if it fired (regardless of whether the action
	 *         succeeded); {@code false} if the bot is not in {@code src} or the
	 *         condition is unsatisfied.
	 */
	public boolean exec(Entity e) {
		Bot bot = e.bot();
		if (bot != null && src.equals(bot.state()) && condition.eval(e) && action.exec(e)) {
			bot.state(tgt); // raw target; Automaton.step resolves the () / (_) sentinels
			return true;
		}
		return false;
	}

	/** @return the (possibly sentinel) target state, for the automaton to resolve. */
	public State tgt() {
		return tgt;
	}
}
