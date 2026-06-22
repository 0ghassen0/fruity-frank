package frank.game;

import engine.core.Controller;
import engine.entities.Bot;
import oop.graphics.Canvas;

public class FrankController extends Controller {

    private final FruityFrankModel model;
    private Runnable restartAction;

    public FrankController(Bot bot, FruityFrankModel model) {
        super(bot);
        this.model = model;
    }

    public void setRestartAction(Runnable restartAction) {
        this.restartAction = restartAction;
    }

    @Override
    public void pressed(Canvas canvas, int bno, int x, int y) {
        if (model.state().isEnded() && model.state().canShowEndMenu()) {
            if (insideRestartButton(canvas, x, y)) {
                if (restartAction != null)
                    restartAction.run();
                return;
            }

            if (insideExitButton(canvas, x, y)) {
                System.exit(0);
                return;
            }

            return;
        }

        super.pressed(canvas, bno, x, y);
    }

    @Override
    public void pressed(Canvas canvas, int keyCode, char keyChar) {
        if (model.state().isEnded())
            return;

        super.pressed(canvas, keyCode, keyChar);
    }

    private boolean insideRestartButton(Canvas canvas, int mx, int my) {
        int originalW = 713;
        int originalH = 276;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.12 * menuW);
        int y = menuY + (int) (0.45 * menuH);
        int w = (int) (0.72 * menuW);
        int h = (int) (0.22 * menuH);

        return inside(mx, my, x, y, w, h);
    }

    private boolean insideExitButton(Canvas canvas, int mx, int my) {
        int originalW = 713;
        int originalH = 276;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.25 * menuW);
        int y = menuY + (int) (0.68 * menuH);
        int w = (int) (0.55 * menuW);
        int h = (int) (0.22 * menuH);

        return inside(mx, my, x, y, w, h);
    }
    
    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w
            && my >= y && my <= y + h;
    }
}