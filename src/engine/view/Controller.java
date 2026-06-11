package engine.view;

import engine.brain.BasicStunt;
import engine.entity.Entity;
import game.bots.PlayerBot;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;
import oop.tasks.Runtime;
import oop.tasks.Task;

public class Controller implements Canvas.KeyListener, Canvas.MouseListener {

    private final Entity player;
    private final Task task;

    // FSM states
    private enum RotState { NO_ROT, LEFT_ROT, RIGHT_ROT, LEFT_ROT_RP, RIGHT_ROT_LP }
    private RotState rotState = RotState.NO_ROT;

    // Rotator
    private boolean turnLeft  = false;
    private boolean turnRight = false;
    private static final int ROT_ANGLE = 5;
    private static final int ROT_DELAY = 16;

    public Controller(Canvas canvas, Entity player, Task task) {
        assert canvas != null;
        assert player != null;
        assert task != null;
        this.player = player;
        this.task = task;
        canvas.set((Canvas.KeyListener) this);
        canvas.set((Canvas.MouseListener) this);
    }

    // =========================================================
    // KeyListener
    // =========================================================

    @Override
    public void pressed(Canvas canvas, int keyCode, char keyChar) {
        if (!(player.stunt() instanceof BasicStunt)) return;
        if (!(((BasicStunt) player.stunt()).bot() instanceof PlayerBot)) return;
        
        PlayerBot bot = (PlayerBot) ((BasicStunt) player.stunt()).bot();
        
        switch (keyCode) {
            case VirtualKeyCodes.VK_RIGHT: bot.command(0);   break;
            case VirtualKeyCodes.VK_DOWN:  bot.command(90);  break;
            case VirtualKeyCodes.VK_LEFT:  bot.command(180); break;
            case VirtualKeyCodes.VK_UP:    bot.command(270); break;
        }
    }
    @Override
    public void released(Canvas canvas, int keyCode, char keyChar) {}

    @Override
    public void typed(Canvas canvas, char keyChar) {}

    // =========================================================
    // MouseListener — FSM
    // =========================================================

    @Override
    public void moved(Canvas canvas, int x, int y) {}

    @Override
    public void pressed(Canvas canvas, int bno, int x, int y) {
        switch (rotState) {
            case NO_ROT:
                if (bno == 1) {
                    rotState = RotState.LEFT_ROT;
                    startRotation(true);
                } else if (bno == 3) {
                    rotState = RotState.RIGHT_ROT;
                    startRotation(false);
                }
                break;
            case LEFT_ROT:
                if (bno == 3) rotState = RotState.LEFT_ROT_RP;
                break;
            case RIGHT_ROT:
                if (bno == 1) rotState = RotState.RIGHT_ROT_LP;
                break;
            default:
                break;
        }
    }

    @Override
    public void released(Canvas canvas, int bno, int x, int y) {
        switch (rotState) {
            case LEFT_ROT:
                if (bno == 1) { rotState = RotState.NO_ROT;    stopRotation(); }
                break;
            case RIGHT_ROT:
                if (bno == 3) { rotState = RotState.NO_ROT;    stopRotation(); }
                break;
            case LEFT_ROT_RP:
                if (bno == 1) { rotState = RotState.RIGHT_ROT; startRotation(false); }
                if (bno == 3) { rotState = RotState.LEFT_ROT; }
                break;
            case RIGHT_ROT_LP:
                if (bno == 3) { rotState = RotState.LEFT_ROT;  startRotation(true); }
                if (bno == 1) { rotState = RotState.RIGHT_ROT; }
                break;
            default:
                break;
        }
    }

    // =========================================================
    // Rotation helpers
    // =========================================================

    private void startRotation(boolean left) {
        turnLeft  = left;
        turnRight = !left;
        task.post(new Rotator());
    }

    private void stopRotation() {
        turnLeft  = false;
        turnRight = false;
    }

    private class Rotator implements oop.tasks.Runnable {
        @Override
        public void run() throws Exception {
            if (turnLeft) {
                player.turn(-ROT_ANGLE);
                task.post(this, ROT_DELAY);
            } else if (turnRight) {
                player.turn(ROT_ANGLE);
                task.post(this, ROT_DELAY);
            }
            // sinon stop — ne re-poste pas
        }
    }
}