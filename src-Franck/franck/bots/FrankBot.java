package franck.bots;

import engine.entities.Bot;
import engine.entities.Entity;
import engine.entities.Stunt;
import franck.FruityFrankModel;
import franck.entities.Frank;
import oop.graphics.VirtualKeyCodes;

public class FrankBot extends Bot {

    private final double speed;

    public FrankBot(Entity entity, double speed) {
        super(entity);
        this.speed = speed;
    }

    @Override
    public void pressed(int keyCode, char keyChar) {
        if (entity == null) return;

        if (keyChar == 'd' || keyChar == 'D') {
            if (entity.model() instanceof FruityFrankModel m) m.toggleDebug();
            return;
        }

        if (keyCode == VirtualKeyCodes.VK_SPACE) {
            if (entity instanceof Frank frank)
                if (frank.hasCherry())
                    if (entity.model() instanceof FruityFrankModel m)
                        m.throwCherryInternal(frank);
            return;
        }

        Stunt stunt = stunt();
        if (stunt == null) return;

        double dvx = 0, dvy = 0;
        switch (keyCode) {
            case VirtualKeyCodes.VK_RIGHT -> dvx =  speed;
            case VirtualKeyCodes.VK_LEFT  -> dvx = -speed;
            case VirtualKeyCodes.VK_UP    -> dvy = -speed;
            case VirtualKeyCodes.VK_DOWN  -> dvy =  speed;
            default -> { return; }
        }

        if (entity instanceof Frank frank)
            frank.setFacing(dvx > 0 ? 1 : dvx < 0 ? -1 : 0,
                            dvy > 0 ? 1 : dvy < 0 ? -1 : 0);

        stunt.drive(dvx, dvy);
    }

    @Override
    public void released(int keyCode, char keyChar) {
        Stunt stunt = stunt();
        if (stunt == null) return;
        switch (keyCode) {
            case VirtualKeyCodes.VK_RIGHT, VirtualKeyCodes.VK_LEFT,
                 VirtualKeyCodes.VK_UP,    VirtualKeyCodes.VK_DOWN -> stunt.drive(0, 0);
        }
    }
}