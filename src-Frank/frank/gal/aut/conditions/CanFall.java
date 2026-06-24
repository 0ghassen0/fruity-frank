package frank.gal.aut.conditions;

import java.util.ArrayList;

import engine.entities.Entity;
import engine.entities.Stunt;
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
 * Frank-specific GAL guard, uses Struck token in GalCompiler
 *
 * True iff the apple can fall (no large block or fruit under it). Thin blocks
 * only stop the apple for a short pause.
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
		Stunt stunt = apple.stunt();

		if (apple.isLowArmed()) {
			if (stunt != null)
				stunt.drive(0, 0);
			apple.tickLowDelay();
			if (apple.lowDelayDone()) {
				removeGrassLow(model, grid.cellAt(pos));
				apple.disarmLow();
			}
			return false;
		}

		boolean solidSupport = model.map().isWall(south.x(), south.y()) || cellHas(grid, south, GrassHigh.class)
				|| cellHasOtherApple(grid, south, apple) || cellHas(grid, south, Fruit.class)
				|| cellHas(grid, south, Cherry.class);

		if (solidSupport) {
			if (apple.center().y() < pos.toISUCoordCentered().y())
				return true; // not yet at resting height: keep descending

			apple.disarm();
			apple.disarmLow();
			apple.setFalling(false);
			if (stunt != null)
				stunt.drive(0, 0); // settle: stop re-probing now that it rests on the support
			return false;
		}

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
