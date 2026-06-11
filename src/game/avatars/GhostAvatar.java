package game.avatars;

import engine.view.Avatar;
import engine.view.View;
import oop.graphics.Graphics;
import game.entities.Ghost;

public class GhostAvatar extends Avatar {

    public GhostAvatar(View view, Ghost entity) {
        super(view, entity);
    }

    @Override
    public void paint(Graphics g) {
        int px = view.toPixelX(entity.center().x());
        int py = view.toPixelY(entity.center().y());
        int pr = view.toPixel(entity.size().x() / 2.0);

        g.setColor(Graphics.Colors.cyan);
        g.fillOval(px - pr, py - pr, pr*2, pr*2);
        g.fillRect(px - pr, py, pr*2, pr);
    }
}