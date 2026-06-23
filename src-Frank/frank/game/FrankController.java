package frank.game;

import engine.core.Controller;
import engine.entities.Bot;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;

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
        if (model.state().isStartMenu()) {
            if (insideStartButton(canvas, x, y)) {
                model.state().startGame();
                return;
            }

            if (insideStartExitButton(canvas, x, y)) {
                System.exit(0);
                return;
            }

            return;
        }

        if (model.state().isPaused()) {
            if (insidePauseResumeButton(canvas, x, y)) {
                model.state().resumeGame();
                return;
            }

            if (insidePauseRestartButton(canvas, x, y)) {
                if (restartAction != null)
                    restartAction.run();
                return;
            }

            if (insidePauseExitButton(canvas, x, y)) {
                System.exit(0);
                return;
            }

            return;
        }

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
        if (keyCode == VirtualKeyCodes.VK_ESCAPE) {
            model.state().togglePause();
            return;
        }

        if (model.state().isPaused())
            return;

        if (model.state().isEnded())
            return;

        if (model.state().isStartMenu())
            return;

        super.pressed(canvas, keyCode, keyChar);
    }

    private void toggleDebugOverlay() {
        boolean show = !model.debugMode();
        GameApp.debug.setShowTickStats(show);
        GameApp.debug.setShowPaintStats(show);
        GameApp.debug.setShowFps(show);
        GameApp.debug.setShowHitboxes(show);
        GameApp.debug.setShowStates(show);
        GameApp.painter.setDebug(show ? GameApp.debug : null);
        model.toggleDebug();
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
    
    
    private boolean insideStartButton(Canvas canvas, int mx, int my) {
        int originalW = 819;
        int originalH = 330;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.18 * menuW);
        int y = menuY + (int) (0.43 * menuH);
        int w = (int) (0.65 * menuW);
        int h = (int) (0.24 * menuH);

        return inside(mx, my, x, y, w, h);
    }

    private boolean insideStartExitButton(Canvas canvas, int mx, int my) {
        int originalW = 819;
        int originalH = 330;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.27 * menuW);
        int y = menuY + (int) (0.68 * menuH);
        int w = (int) (0.55 * menuW);
        int h = (int) (0.23 * menuH);

        return inside(mx, my, x, y, w, h);
    }
    
    private boolean insidePauseResumeButton(Canvas canvas, int mx, int my) {
        int originalW = 819;
        int originalH = 330;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.30 * menuW);
        int y = menuY + (int) (0.34 * menuH);
        int w = (int) (0.42 * menuW);
        int h = (int) (0.18 * menuH);

        return inside(mx, my, x, y, w, h);
    }

    private boolean insidePauseRestartButton(Canvas canvas, int mx, int my) {
        int originalW = 819;
        int originalH = 330;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.17 * menuW);
        int y = menuY + (int) (0.54 * menuH);
        int w = (int) (0.67 * menuW);
        int h = (int) (0.18 * menuH);

        return inside(mx, my, x, y, w, h);
    }

    private boolean insidePauseExitButton(Canvas canvas, int mx, int my) {
        int originalW = 819;
        int originalH = 330;

        int menuW = Math.min(canvas.getWidth() - 20, originalW);
        int menuH = originalH * menuW / originalW;

        int menuX = (canvas.getWidth() - menuW) / 2;
        int menuY = (canvas.getHeight() - menuH) / 2;

        int x = menuX + (int) (0.28 * menuW);
        int y = menuY + (int) (0.74 * menuH);
        int w = (int) (0.53 * menuW);
        int h = (int) (0.17 * menuH);

        return inside(mx, my, x, y, w, h);
    }

}