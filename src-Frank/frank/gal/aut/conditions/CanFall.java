package frank.gal.aut.conditions;

import engine.entities.Entity;
import engine.gal.aut.iGALCondition;
import engine.geometry.Grid;
import frank.entities.Apple;
import frank.entities.Cherry;
import frank.entities.Fruit;
import frank.entities.GrassHigh;
import frank.game.FruityFrankModel;

// == CAN FALL ==

/**
 * Frank-specific GAL guard, wired to the spare {@code Struck} keyword in
 * {@code GalCompiler}
 *
 * <p>
 * True iff the apple may take a downward step this tick: nothing solid supports
 * it from below
 *
 * <p>
 * When supported it also halts the stunt and clears the falling flag, so a
 * landed apple comes to rest without relying on any other pass.
 */
public final class CanFall implements iGALCondition {

	/** Hang time, in AI ticks, between losing support and starting to fall. */
	private static final int HANG_TICKS = 6;

	@Override
	public boolean eval(Entity self) {
		if (!(self instanceof Apple apple))
			return false;
		if (!(self.model() instanceof FruityFrankModel model))
			return false;

		Grid grid = model.grid();
		Grid.Position pos = apple.position();
		Grid.Position south = grid.new Position(pos.x(), pos.y() + 1);

		boolean solidSupport = model.map().isWall(south.x(), south.y())
				|| cellHas(grid, south, GrassHigh.class)
				|| cellHasOtherApple(grid, south, apple)
				|| cellHas(grid, south, Fruit.class)
				|| cellHas(grid, south, Cherry.class);

		if (solidSupport) {
			apple.disarm();
			apple.setFalling(false);
			if (apple.stunt() != null)
				apple.stunt().drive(0, 0); // come to rest on the support
			return false;
		}

		if (apple.isFalling())
			return true;

		// Just lost its support: hang briefly, then start falling.
		if (!apple.isArmed()) {
			apple.arm(HANG_TICKS);
			return false;
		}
		apple.tickDelay();
		if (!apple.delayDone())
			return false;

		apple.setFalling(true);
		return true;
	}

	private static boolean cellHas(Grid grid, Grid.Position pos, Class<?> type) {
		for (Entity e : grid.cellAt(pos).entities())
			if (type.isInstance(e))
				return true;
		return false;
	}

	private static boolean cellHasOtherApple(Grid grid, Grid.Position pos, Apple self) {
		for (Entity e : grid.cellAt(pos).entities())
			if (e instanceof Apple a && a != self)
				return true;
		return false;
	}
}
