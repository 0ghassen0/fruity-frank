package frank.graphics;

import engine.entities.Entity;
import engine.graphics.Camera;
import engine.graphics.Painter;
import frank.entities.Frank;
import frank.game.Category;
import frank.game.FruityFrankMap;
import frank.game.FruityFrankModel;
import oop.graphics.Canvas;
import oop.graphics.Color;
import oop.graphics.Font;
import oop.graphics.Graphics;
import oop.graphics.BufferedImage;

public class FrankPainter extends Painter {

	private final FrankView view;
	private final FruityFrankMap map;
	private final FruityFrankModel model;
	private final int cellPx;
	private final double cmPerCell;

	public FrankPainter(FrankView view, FruityFrankMap map, FruityFrankModel model, int cellPx, double cmPerCell) {
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
		g.drawString("FRUITS " + model.state().fruitsRemaining(), 90, 16);
		
		Frank frank = model.player();
		if (frank != null) {
			g.setColor(g.getColor(255, 255, 120, 0));
			for (int i = 0; i < model.state().lives(); i++)
			    g.fillOval(canvas.getWidth() - 24 - i * 16, 6, 12, 12);
		}
		
		
		paintStartMenu(canvas, g);

		if (!model.state().isStartMenu()) {
		    paintEndMenu(canvas, g);
		    paintCountdown(canvas, g);
		}
		
		if (model.debugMode())
			debugOverlay(g);
	}
	private void drawLabel(Graphics g, String text, int x, int y, Color textColor) {
	    Font f = g.getFont();
	    int w   = f.getWidth(text);   // largeur réelle du texte
	    int asc = f.getAscent();      // au-dessus de la ligne de base
	    int dsc = f.getDescent();     // en dessous
	    int pad = 2;

	    // fond noir opaque 
	    g.setColor(g.getColor(255, 0, 0, 0));
	    g.fillRect(x - pad, y - asc - pad, w + 2 * pad, asc + dsc + 2 * pad);

	    // texte
	    g.setColor(textColor);
	    g.drawString(text, x, y);
	}
	private void debugOverlay(Graphics g) {
	    Camera cam = view.camera();
	    Color frankColor = g.getColor(255, 255, 80, 80);
	    Color otherColor = g.getColor(220, 255, 255, 0);

	    for (Entity e : model.entities()) {
	        int[] s = (cam != null) ? cam.worldToScreen(e.center().x(), e.center().y())
	                : new int[] { (int) e.center().x(), (int) e.center().y() };
	        int lx = s[0] - 20;
	        int ly = s[1] - cellPx / 2 - 4;

	        if (Category.of(e) == Category.FRANK && e instanceof Frank f) {
	            drawLabel(g, f.name() + " vies:" + model.state().lives(), lx, ly, frankColor);
	        } else if (Category.of(e) == Category.ENEMY || Category.of(e) == Category.APPLE) {
	            drawLabel(g, e.name(), lx, ly, otherColor);
	        }
	    }
	}
	
	
	private void paintStartMenu(Canvas canvas, Graphics g) {
	    if (!model.state().isStartMenu())
	        return;

	    BufferedImage img = view.sprites().startMenu();

	    if (img == null)
	        return;

	    int originalW = img.getWidth();
	    int originalH = img.getHeight();

	    int w = Math.min(canvas.getWidth() - 20, originalW);
	    int h = originalH * w / originalW;

	    int x = (canvas.getWidth() - w) / 2;
	    int y = (canvas.getHeight() - h) / 2;

	    g.drawImage(img, x, y, w, h);
	}
	
	private void paintCountdown(Canvas canvas, Graphics g) {
	    if (!model.state().isCountingDown())
	        return;

	    int n = model.state().countdownNumber();
	    BufferedImage img = view.sprites().countdownDigit(n);

	    if (img == null)
	        return;

	    int w = 90;
	    int h = 110;

	    int x = (canvas.getWidth() - w) / 2;
	    int y = (canvas.getHeight() - h) / 2;

	    g.drawImage(img, x, y, w, h);
	}
	
	private void paintEndMenu(Canvas canvas, Graphics g) {
	    if (!model.state().isEnded())
	        return;

	    if (!model.state().canShowEndMenu())
	        return;

	    BufferedImage img = model.state().isVictory()
	            ? view.sprites().victoryMenu()
	            : view.sprites().gameOverMenu();
	    
	    
	    if (img == null)
	        return;

	    int originalW = img.getWidth();
	    int originalH = img.getHeight();

	    int w = Math.min(canvas.getWidth() - 20, originalW);
	    int h = originalH * w / originalW;

	    int x = (canvas.getWidth() - w) / 2;
	    int y = (canvas.getHeight() - h) / 2;

	    g.drawImage(img, x, y, w, h);
	}
}