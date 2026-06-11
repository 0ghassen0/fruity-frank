package game.avatars;

import engine.view.Avatar;
import engine.view.View;
import oop.graphics.Graphics;
import game.entities.PacMan;

public class PacManAvatar extends Avatar {

    public PacManAvatar(View view, PacMan entity) {
        super(view, entity);
    }

    @Override
    public void paint(Graphics g) {
    	
        int px = view.toPixelX(entity.center().x());
        int py = view.toPixelY(entity.center().y());
        int pr = view.toPixel(entity.size().x());

        Object saved = g.getTransform();
        g.translate(px, py);
        g.rotate(Math.toRadians(entity.orientation()));

        g.setColor(Graphics.Colors.yellow);
        g.fillOval(-pr, -pr, pr*2, pr*2);

        g.setTransform(saved);
    }
}