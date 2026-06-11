package engine.view;

import engine.core.Model;
import engine.entity.Entity;
import oop.graphics.Canvas;
import oop.graphics.Graphics;

public class View implements Canvas.PaintListener {

	// constants
    private final Model model;
    private final double vpW_cm;
    private final double vpH_cm;
    private final double elasticity = 0.3;

    // fields
    private Canvas canvas;
    private double vpX;
    private double vpY;

    public View(Model model, double vpW_cm, double vpH_cm) {
        assert model != null;
        assert vpW_cm > 0;
        assert vpH_cm > 0;
        this.model = model;
        this.vpW_cm = vpW_cm;
        this.vpH_cm = vpH_cm;
        this.vpX = 0.0;
        this.vpY = 0.0;
    }

    public void setCanvas(Canvas canvas) {
        assert canvas != null;
        this.canvas = canvas;
        canvas.set(this);
    }

    public Canvas canvas() {
        return this.canvas;
    }

    public int toPixelX(double x_cm) {
        return (int) Math.round((x_cm - vpX) * model.isu().game().pixelPerCm());
    }

    public int toPixelY(double y_cm) {
        return (int) Math.round((y_cm - vpY) * model.isu().game().pixelPerCm());
    }

    public int toPixel(double cm) {
        return (int) Math.round(cm * model.isu().game().pixelPerCm());
    }

    @Override
    public void visible(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void paint(Canvas canvas, Graphics g) {
        this.canvas = canvas;

        if (canvas == null) {
            return;
        }

        centerOnPlayer();
        paintBackground(canvas, g);
        paintEntities(g);
        paintMinimap(canvas, g);
    }

    @Override
    public void revoked(Canvas canvas) {
        // no-op
    }

    private void centerOnPlayer() {
        for (Entity e : model.entities()) {
            if (e instanceof game.entities.PacMan && e.center() != null) {
                double px = e.center().x();
                double py = e.center().y();
                double targetX = px - this.vpW_cm / 2.0;
                double targetY = py - this.vpH_cm / 2.0;
                this.vpX += (targetX - this.vpX) * elasticity;
                this.vpY += (targetY - this.vpY) * elasticity;
                return;
            }
        }
    }

    // paint abstraite
    private void paintBackground(Canvas canvas, Graphics g) {
        g.setColor(Graphics.Colors.black);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void paintEntities(Graphics g) {
        for (Entity e : model.entities()) {
            if (e.avatar() != null) {
                e.avatar().paint(g);
            }
        }
    }

    private void paintMinimap(Canvas canvas, Graphics g) {
        int mapW = canvas.getWidth() / 5;
        int mapH = canvas.getHeight() / 5;
        int mapX = canvas.getWidth() - mapW - 5;
        int mapY = 5;

        double worldW = model.isu().game().widthCm();
        double worldH = model.isu().game().heightCm();

        g.setColor(Graphics.Colors.darkGray);
        g.fillRect(mapX, mapY, mapW, mapH);

        for (Entity e : model.entities()) {
            if (e.center() == null || e.size() == null) {
                continue;
            }

            int ex = mapX + (int) (e.center().x() / worldW * mapW);
            int ey = mapY + (int) (e.center().y() / worldH * mapH);
            int er = Math.max(2, (int) (e.size().x() / worldW * mapW));

            if (e instanceof game.entities.PacMan) {
                g.setColor(Graphics.Colors.yellow);
            } else if (e instanceof game.entities.Ghost) {
                g.setColor(Graphics.Colors.red);
            } else if (e instanceof game.entities.Obstacle) {
                g.setColor(Graphics.Colors.gray);
            } else {
                g.setColor(Graphics.Colors.white);
            }

            g.fillOval(ex - er, ey - er, er * 2, er * 2);
        }
    }
}