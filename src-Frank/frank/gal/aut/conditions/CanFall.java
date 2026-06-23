package frank.gal.aut.conditions;

import java.util.ArrayList;

import engine.entities.Entity;
import engine.gal.aut.iGALCondition;
import engine.geometry.Grid;
import frank.entities.Apple;
import frank.entities.Cherry;
import frank.entities.Fruit;
import frank.entities.GrassHigh;
import frank.entities.GrassLow;
import frank.game.FruityFrankModel;

// == CAN FALL ==

/**
 * Frank-specific GAL guard, wired to the spare {@code Struck} keyword in
 * {@code GalCompiler}.
 *
 * <p>
 * True iff the apple may take a downward step this tick. The apple free-falls
 * through anything non-solid; a thin floor ({@code GrassLow}) is NOT a support,
 * so the apple keeps falling into it. The brief pause on a thin floor is armed
 * by the {@code APPLE × GRASS_LOW} collision rule (which fires at the moment of
 * contact — i.e. at the correct resting height — and halts the apple); this
 * guard then counts the pause down and crushes the floor, after which the apple
 * resumes falling. Driving the pause off the contact event avoids any
 * cell-snapping or coast logic and so cannot leave the apple stuck.
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

		// (1) Paused on a thin floor (armed on contact by the crush rule): hold in
		// place, count the pause down, then crush the floor under us. Driven by the
		// lowDelay state alone, so it can never get stuck.
		if (apple.isLowArmed()) {
			if (apple.stunt() != null)
				apple.stunt().drive(0, 0);
			apple.tickLowDelay();
			if (apple.lowDelayDone()) {
				removeGrassLow(model, grid.cellAt(pos));
				apple.disarmLow();
			}
			return false;
		}

		// (2) Solid support directly below (wall / GrassHigh / apple / fruit / cherry):
		// come to rest on it.
		boolean solidSupport = model.map().isWall(south.x(), south.y())
				|| cellHas(grid, south, GrassHigh.class)
				|| cellHasOtherApple(grid, south, apple)
				|| cellHas(grid, south, Fruit.class)
				|| cellHas(grid, south, Cherry.class);

		if (solidSupport) {
			apple.disarm();
			apple.disarmLow();
			apple.setFalling(false);
			if (apple.stunt() != null)
				apple.stunt().drive(0, 0); // come to rest on the support
			return false;
		}

		// (3) Nothing solid below (open space or a thin floor): hang briefly, then
		// free-fall. GrassLow is not a support — the apple falls through it and the
		// contact rule arms the pause at the floor.
		if (apple.isFalling())
			return true;

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

	private static void removeGrassLow(FruityFrankModel model, Grid.Cell cell) {
		for (Entity e : new ArrayList<>(cell.entities()))
			if (e instanceof GrassLow)
				model.remove(e);
	}
}
