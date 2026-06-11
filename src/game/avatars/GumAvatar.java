package game.avatars;

import engine.view.Avatar;
import engine.view.View;
import oop.graphics.Graphics;
import game.entities.Gum;

public class GumAvatar extends Avatar {

    public GumAvatar(View view, Gum entity) {
        super(view, entity);
    }

    @Override
    public void paint(Graphics g) {
        int px = view.toPixelX(entity.center().x());
        int py = view.toPixelY(entity.center().y());
        int pr = view.toPixel(entity.size().x() / 4.0);

        g.setColor(Graphics.Colors.white);
        g.fillOval(px - pr, py - pr, pr*2, pr*2);
    }
}