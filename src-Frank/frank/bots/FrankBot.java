package frank.bots;

import engine.entities.Bot;
import engine.entities.Entity;
import engine.entities.Stunt;
import frank.entities.Frank;
import frank.game.FruityFrankModel;
import frank.game.GameApp;
import oop.graphics.VirtualKeyCodes;

public class FrankBot extends Bot {

	private final double speed;

	public FrankBot(Entity entity, double speed) {
		super(entity);
		this.speed = speed;
	}

	@Override
	public void pressed(int keyCode, char keyChar) {
		if (entity == null)
			return;

		FruityFrankModel m = (FruityFrankModel) entity.model();

		if (keyCode == VirtualKeyCodes.VK_F3) {
			if (!m.debugMode()) {
				GameApp.debug.setShowTickStats(true);
				GameApp.debug.setShowPaintStats(true);
				GameApp.debug.setShowFps(true);
				GameApp.debug.setShowHitboxes(true);
				GameApp.debug.setShowStates(true);
				// GameApp.debug.setShowEntityNames(true);
				GameApp.painter.setDebug(GameApp.debug); // also forwards to the view
			}
			if (m.debugMode()) {
				GameApp.debug.setShowTickStats(false);
				GameApp.debug.setShowPaintStats(false);
				GameApp.debug.setShowFps(false);
				GameApp.debug.setShowHitboxes(false);
				GameApp.debug.setShowStates(false);
				// GameApp.debug.setShowEntityNames(true);
				GameApp.painter.setDebug(null); // also forwards to the view
			}

			m.toggleDebug();
			return;
		}

		if (keyCode == VirtualKeyCodes.VK_SPACE) {
			if (entity instanceof Frank frank)
				if (frank.hasCherry())
					m.throwCherryInternal(frank);
			return;
		}

		Stunt stunt = stunt();
		if (stunt == null)
			return;

		double dvx = 0, dvy = 0;
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT -> dvx = speed;
		case VirtualKeyCodes.VK_LEFT -> dvx = -speed;
		case VirtualKeyCodes.VK_UP -> dvy = -speed;
		case VirtualKeyCodes.VK_DOWN -> dvy = speed;
		default -> {
			return;
		}
		}

		if (entity instanceof Frank frank)
			frank.setFacing(dvx > 0 ? 1 : dvx < 0 ? -1 : 0, dvy > 0 ? 1 : dvy < 0 ? -1 : 0);

		stunt.drive(dvx, dvy);
	}

	@Override
	public void released(int keyCode, char keyChar) {
		Stunt stunt = stunt();
		if (stunt == null)
			return;
		switch (keyCode) {
		case VirtualKeyCodes.VK_RIGHT, VirtualKeyCodes.VK_LEFT, VirtualKeyCodes.VK_UP, VirtualKeyCodes.VK_DOWN -> {
			entity.setCoord(entity.position().toISUCoordCentered());
			stunt.drive(0, 0);
		}
		}
	}
}