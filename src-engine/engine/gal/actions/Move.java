package engine.gal.actions;

import engine.entities.Entity;
import engine.entities.Stunt;
import engine.gal.aut.DirArg;
import engine.gal.aut.Direction;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALAction;

// == MOVE ==

/**
 * The GAL {@code Move(direction)} action: resolves its {@link DirArg} to a unit
 * step, reorients the entity to face that direction (so subsequent relative
 * directions and the avatar track its heading), then pushes a velocity command
 * at the bot's speed onto the entity's stunt. Motion itself is actuated later
 * by the stunt on the motion tick (push model). Returns {@code false} (action
 * not performed) if there is no stunt to drive.
 */
public final class Move implements iGALAction {

	private final DirArg dir;

	public Move(DirArg dir) {
		this.dir = dir;
	}

	@Override
	public boolean exec(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;
		Stunt stunt = bot.stunt();
		if (stunt == null || stunt.busy())
			return false; // no actuator, or a timed action is in progress (can't redirect)
		int[] d = dir.resolve(self);
		if (d[0] != 0 || d[1] != 0)
			self.face(Direction.cardinal(d[0], d[1]).toAngle()); // heading tracks motion
		double speed = bot.speed();
		stunt.drive(d[0] * speed, d[1] * speed);
		return true;
	}
}
