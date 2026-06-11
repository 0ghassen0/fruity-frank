package game.avatars;

import engine.view.Avatar;
import engine.view.View;
import oop.graphics.Graphics;
import game.entities.Obstacle;

public class ObstacleAvatar extends Avatar {

    public ObstacleAvatar(View view, Obstacle entity) {
        super(view, entity);
    }

    @Override
    public void paint(Graphics g) {
        int px = view.toPixelX(entity.center().x());
        int py = view.toPixelY(entity.center().y());
        int pw = view.toPixel(entity.size().x());
        int ph = view.toPixel(entity.size().y());

        g.setColor(Graphics.Colors.gray);
        g.fillRect(px - pw/2, py - ph/2, pw, ph);
    }
}