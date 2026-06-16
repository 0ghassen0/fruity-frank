package franck;

import java.awt.Dimension;

import engine.ai.Brain;
import engine.core.Controller;
import engine.core.Ticker;
import engine.entities.BasicStunt;
import engine.gal.aut.GALBot;
import engine.graphics.Camera;
import franck.ai.FranckAutomata;
import franck.ai.FranckCategories;
import franck.bots.FrankBot;
import franck.entities.*;
import franck.graphics.FranckPainter;
import franck.graphics.FranckSprites;
import franck.graphics.FranckView;
import oop.graphics.Canvas;
import oop.tasks.Runnable;
import oop.tasks.Runtime;

public class GameApp {

	private static final int MOTION_TPS = 30, AI_TPS = 8, SPAWN_TPS = 2, RENDER_MS = 33;
	private static final int VIEW_COLS = 15, VIEW_ROWS = 11;
	private static final double FRANK_SPEED = 1.0, ENEMY_SPEED = 0.6;

	private static volatile boolean running = true;

	public static void main(String[] args) {
		FruityFrankMap map = new FruityFrankMap();
		FruityFrank game = new FruityFrank(map.cols(), map.rows());
		FruityFrankModel model = new FruityFrankModel(game, map);

		for (FruityFrankMap.Cell c : map.grassCells()) {
			model.add(new GrassLow(game, c.col(), c.row()));
			model.add(new GrassHigh(game, c.col(), c.row()));
		}

		Fruit.FruitType[] ft = Fruit.FruitType.values();
		int ti = 0;
		for (FruityFrankMap.Cell c : map.fruitCells())
			model.add(new Fruit(game, c.col(), c.row(), ft[ti++ % ft.length]));

		for (FruityFrankMap.Cell c : map.cherryCells())
			model.add(new Cherry(game, c.col(), c.row()));

		for (FruityFrankMap.Cell c : map.appleCells()) {
			Apple apple = new Apple(game, c.col(), c.row());
			apple.setStunt(new BasicStunt(apple)); // pour le push glissant ; toujours pas de bot
			model.add(apple);
		}

		FrankBot frankBot = spawnFrank(model, game, map);
		Controller controller = new Controller(frankBot);

		Enemy.EnemyType[] types = { Enemy.EnemyType.RED, // creuse + poursuit
				Enemy.EnemyType.GREEN, // poursuit dans tunnels
				Enemy.EnemyType.PURPLE, // fuit
				Enemy.EnemyType.ORANGE // creuse + poursuit
		};
		int ei = 0;
		for (FruityFrankMap.Cell c : map.enemySpawns()) {
			spawnEnemy(model, game, c.col(), c.row(), types[ei % types.length]);
			ei++;
		}

		int cellPx = (int) Math.round(game.getCmPerCell() * game.getPixelPerCm());
		int pxW = VIEW_COLS * cellPx, pxH = VIEW_ROWS * cellPx + 24;

		FranckSprites sprites = new FranckSprites();
		FranckView view = new FranckView(game, model, sprites);
		Camera cam = new Camera(game.getWidth_cm(), game.getHeight_cm(), game.torusOnX(), game.torusOnY(), pxW,
				pxH - 24, game.getPixelPerCm());
		view.setCamera(cam);

		FranckPainter painter = new FranckPainter(view, map, model, cellPx, game.getCmPerCell());
		Brain brain = new Brain(model);

		Runtime.boot(new Dimension(pxW, pxH), new Runnable() {
			@Override
			public void run() {
				Canvas canvas = (Canvas) Runtime.task().find("canvas");
				canvas.set(painter);
				canvas.set((Canvas.KeyListener) controller);
				canvas.set((Canvas.MouseListener) controller);
				new Ticker(MOTION_TPS).start(model);
				new Ticker(AI_TPS).start(brain);
				new Ticker(SPAWN_TPS).start(new Spawner(model, game, map, controller));
				Runtime.post(new RenderLoop(canvas), RENDER_MS);
			}
		}, true);
	}

	private static FrankBot spawnFrank(FruityFrankModel model, FruityFrank game, FruityFrankMap map) {
		FruityFrankMap.Cell sp = map.frankSpawn();
		Frank frank = new Frank("Frank", game, sp.col(), sp.row());
		frank.setStunt(new BasicStunt(frank));
		FrankBot bot = new FrankBot(frank, FRANK_SPEED);
		frank.setBot(bot);
		model.add(frank);
		model.setPlayer(frank);
		return bot;
	}

	private static void spawnEnemy(FruityFrankModel model, FruityFrank game, int x, int y, Enemy.EnemyType type) {
		Enemy enemy = new Enemy("Enemy_" + type + "@" + x + "," + y, game, x, y, type);
		enemy.setStunt(new BasicStunt(enemy));
		var automaton = (type == Enemy.EnemyType.PURPLE) ? FranckAutomata.enemyFlee() : FranckAutomata.enemyChase();
		enemy.setBot(new GALBot(enemy, automaton, ENEMY_SPEED, new FranckCategories()));
		model.add(enemy);
	}

	private static class Spawner implements Ticker.TickListener {
		private final FruityFrankModel model;
		private final FruityFrank game;
		private final FruityFrankMap map;
		private final Controller controller;

		Spawner(FruityFrankModel m, FruityFrank g, FruityFrankMap mp, Controller c) {
			model = m;
			game = g;
			map = mp;
			controller = c;
		}

		@Override
		public void onTick(double dt) {
			if (!model.state().isGameOver() && model.state().frankDied()) {
				model.state().clearDeath();
				controller.setPlayerBot(spawnFrank(model, game, map));
			}
		}
	}

	private static class RenderLoop implements Runnable {
		private final Canvas canvas;

		RenderLoop(Canvas c) {
			canvas = c;
		}

		@Override
		public void run() {
			if (!running)
				return;
			canvas.repaint();
			Runtime.post(this, RENDER_MS);
		}
	}
}