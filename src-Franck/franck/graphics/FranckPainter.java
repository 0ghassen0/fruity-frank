package franck.graphics;

import engine.entities.Entity;
import engine.graphics.Camera;
import engine.graphics.Painter;
import franck.Category;
import franck.FruityFrankMap;
import franck.FruityFrankModel;
import franck.entities.Frank;
import oop.graphics.Canvas;
import oop.graphics.Graphics;

public class FranckPainter extends Painter {

	private final FranckView view;
	private final FruityFrankMap map;
	private final FruityFrankModel model;
	private final int cellPx;
	private final double cmPerCell;

	public FranckPainter(FranckView view, FruityFrankMap map, FruityFrankModel model, int cellPx, double cmPerCell) {
		super(view);
		this.view = view;
		this.map = map;
		this.model = model;
		this.cellPx = cellPx;
		this.cmPerCell = cmPerCell;
	}

	@Override
	protected void paintBackground(Canvas canvas, Graphics g) {
		g.setColor(g.getColor(255, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		// murs
		Camera cam = view.camera();
		g.setColor(g.getColor(255, 40, 40, 40));
		for (int c = 0; c < map.cols(); c++)
			for (int r = 0; r < map.rows(); r++) {
				if (!map.isWall(c, r))
					continue;
				double wx = (c + 0.5) * cmPerCell, wy = (r + 0.5) * cmPerCell;
				if (cam != null) {
					if (!cam.isVisible(wx, wy, cmPerCell))
						continue;
					int[] s = cam.worldToScreen(wx, wy);
					g.fillRect(s[0] - cellPx / 2, s[1] - cellPx / 2, cellPx, cellPx);
				} else
					g.fillRect(c * cellPx, r * cellPx, cellPx, cellPx);
			}
	}

	@Override
	protected void paintForeground(Canvas canvas, Graphics g) {
		// HUD
		g.setColor(g.getColor(255, 0, 0, 0));
		g.fillRect(0, 0, canvas.getWidth(), 24);
		g.setColor(g.getColor(255, 255, 220, 0));
		g.drawString("SCORE " + String.format("%05d", model.state().score()), 8, 16);
		g.drawString("HI " + String.format("%05d", model.state().highScore()), canvas.getWidth() / 2 - 30, 16);

		Frank frank = model.player();
		if (frank != null) {
			g.setColor(g.getColor(255, 255, 120, 0));
			for (int i = 0; i < frank.lives(); i++)
				g.fillOval(canvas.getWidth() - 24 - i * 16, 6, 12, 12);
		}

		if (model.debugMode())
			debugOverlay(g);
	}

	private void debugOverlay(Graphics g) {
		Camera cam = view.camera();
		g.setColor(g.getColor(220, 255, 255, 0));
		for (Entity e : model.entities()) {
			int[] s = (cam != null) ? cam.worldToScreen(e.center().x(), e.center().y())
					: new int[] { (int) e.center().x(), (int) e.center().y() };
			if (Category.of(e) == Category.FRANK && e instanceof Frank f) {
				g.setColor(g.getColor(255, 255, 80, 80));
				g.drawString(f.name() + " vies:" + f.lives(), s[0] - 20, s[1] - cellPx / 2 - 4);
				g.setColor(g.getColor(220, 255, 255, 0));
			} else if (Category.of(e) == Category.ENEMY || Category.of(e) == Category.APPLE) {
				g.drawString(e.name(), s[0] - 20, s[1] - cellPx / 2 - 4);
			}
		}
	}
}