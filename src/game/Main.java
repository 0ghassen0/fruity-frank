package game;

import java.awt.Dimension;

import engine.brain.AttackBot;
import engine.brain.BasicStunt;
import engine.core.Model;
import engine.view.Controller;
import engine.view.Painter;
import engine.view.Ticker;
import engine.view.View;
import game.avatars.GhostAvatar;
import game.avatars.PacManAvatar;
import game.bots.PlayerBot;
import game.entities.Ghost;
import game.entities.PacMan;
import oop.graphics.Canvas;
import oop.tasks.Runtime;
import oop.tasks.Task;

public class Main implements oop.tasks.Runnable {

    public static void main(String[] args) {
        Runtime.boot(new Dimension(800, 600), new Main());
    }

    @Override
    public void run() throws Exception {
        Task task = Runtime.task();
        Canvas canvas = (Canvas) task.find("canvas");

        PacManGame game = new PacManGame(20, 20);
        Model model = new Model(game);

        PacMan pacman = new PacMan();
        pacman.bind(game.grid(), game.isu());
        pacman.setSize(game.isu().new Dimension(
            game.cmPerCell() / 2.0,
            game.cmPerCell() / 2.0
        ));
        pacman.setPosition(game.grid().new Position(10, 10));
        model.add(pacman);

        Ghost ghost = new Ghost();
        ghost.bind(game.grid(), game.isu());
        ghost.setSize(game.isu().new Dimension(
            game.cmPerCell() / 2.0,
            game.cmPerCell() / 2.0
        ));
        ghost.setPosition(game.grid().new Position(15, 10));
        model.add(ghost);

        BasicStunt playerStunt = new BasicStunt(model, pacman);
        PlayerBot playerBot = new PlayerBot(playerStunt);
        BasicStunt ghostStunt = new BasicStunt(model, ghost);
        new AttackBot(ghostStunt, pacman);

        double vpW = canvas.getWidth() / (double) game.pixelPerCm();
        double vpH = canvas.getHeight() / (double) game.pixelPerCm();

        View view = new View(model, vpW, vpH);
        view.setCanvas(canvas);

        new PacManAvatar(view, pacman);
        new GhostAvatar(view, ghost);
        
        System.out.println("size=" + pacman.size().x());
        System.out.println("pixelPerCm=" + game.pixelPerCm());

        canvas.set((Canvas.KeyListener) new Controller(canvas, pacman, task));


        Ticker ticker = new Ticker(model, view, task);
        ticker.start();

        Painter painter = new Painter(view);
        painter.start();
    }
}