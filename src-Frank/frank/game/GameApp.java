package frank.game;

import java.awt.Dimension;
import java.util.Scanner;

import engine.ai.Brain;
import engine.core.Controller;
import engine.core.Ticker;
import engine.debug.Debug;
import engine.entities.BasicStunt;
import engine.gal.aut.GALBot;
import engine.graphics.Camera;
import frank.ai.FrankAutomata;
import frank.ai.FrankCategories;
import frank.bots.FrankBot;
import frank.entities.*;
import frank.graphics.FrankPainter;
import frank.graphics.FrankSprites;
import frank.graphics.FrankView;
import oop.graphics.Canvas;
import oop.tasks.Runnable;
import oop.tasks.Runtime;
import java.util.LinkedHashMap;
import java.util.Map;

import engine.gal.aut.Automaton;
import engine.gal.aut.GALBot;
import gal.ast.GalCompiler;

public class GameApp {

	private static final int MOTION_TPS = 30, AI_TPS = 8, SPAWN_TPS = 2, RENDER_MS = 33;
	private static final int VIEW_COLS = 15, VIEW_ROWS = 11;
	private static final double FRANK_SPEED = 1.0, ENEMY_SPEED = 0.6;

	private static volatile boolean running = true;

	public static void main(String[] args) {
		long pid = ProcessHandle.current().pid();

		System.out.println("Process ID: " + pid);
		System.out.println("Press Enter to continue...");

		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();

		System.out.println("Continuing...");

		FruityFrankMap map = new FruityFrankMap();
		FruityFrank game = new FruityFrank(map.cols(), map.rows());
		FruityFrankModel model = new FruityFrankModel(game, map);

		Map<String, Automaton> galAutomata = loadGalAutomata();

		spawnEnemySpawner(model, game, map, galAutomata);

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
			spawnEnemy(model, game, c.col(), c.row(), types[ei % types.length], galAutomata);
			ei++;
		}

		int cellPx = (int) Math.round(game.getCmPerCell() * game.getPixelPerCm());
		int pxW = VIEW_COLS * cellPx, pxH = VIEW_ROWS * cellPx + 24;

		FrankSprites sprites = new FrankSprites();
		FrankView view = new FrankView(game, model, sprites);
		Camera cam = new Camera(game.getWidth_cm(), game.getHeight_cm(), game.torusOnX(), game.torusOnY(), pxW,
				pxH - 24, game.getPixelPerCm());
		view.setCamera(cam);

		FrankPainter painter = new FrankPainter(view, map, model, cellPx, game.getCmPerCell());
		Brain brain = new Brain(model);
		Debug debug = new Debug();
		debug.setPixelPerCm(game.getPixelPerCm()); // fallback mapping when no camera
		debug.setShowTickStats(true);
		debug.setShowPaintStats(true);
		debug.setShowFps(true);
		debug.setShowHitboxes(true);
		debug.setShowStates(true);
		// debug.setShowEntityNames(true);
		painter.setDebug(debug); // also forwards to the view

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

	private static void spawnEnemy(FruityFrankModel model, FruityFrank game, int x, int y, Enemy.EnemyType type,
			Map<String, Automaton> galAutomata) {
		Enemy enemy = new Enemy("Enemy_" + type + "@" + x + "," + y, game, x, y, type);
		enemy.setStunt(new BasicStunt(enemy));

		Automaton automaton = (type == Enemy.EnemyType.PURPLE) ? galAutomata.get("Fleeing")
				: galAutomata.get("Tracker");

		enemy.setBot(new GALBot(enemy, automaton, ENEMY_SPEED, new FrankCategories()));
		model.add(enemy);
	}

	private static void spawnEnemySpawner(FruityFrankModel model, FruityFrank game, FruityFrankMap map,
			Map<String, Automaton> galAutomata) {

		Automaton automaton = galAutomata.get("EnemySpawner");

		if (automaton == null) {
			System.err.println("Automate EnemySpawner introuvable");
			return;
		}

		FruityFrankMap.Cell sp = map.enemySpawns().get(0);
		EnemySpawnerEntity spawner = new EnemySpawnerEntity(game, sp.col(), sp.row());

		spawner.setStunt(new BasicStunt(spawner));

		GALBot bot = new GALBot(spawner, automaton, 0.0, new FrankCategories());

		final int[] spawnIndex = { 0 };

		Enemy.EnemyType[] spawnTypes = { Enemy.EnemyType.RED, Enemy.EnemyType.GREEN, Enemy.EnemyType.PURPLE,
				Enemy.EnemyType.ORANGE };

		bot.setFactory((category, at, creator) -> {
			Enemy.EnemyType type = spawnTypes[spawnIndex[0] % spawnTypes.length];
			spawnIndex[0]++;

			Enemy enemy = new Enemy("Enemy_SPAWNED_" + type + "@" + at.x() + "," + at.y(), game, at.x(), at.y(), type);

			enemy.setStunt(new BasicStunt(enemy));

			Automaton enemyAutomaton = switch (type) {
			case PURPLE -> galAutomata.get("EnemyRandom");
			case RED -> galAutomata.get("Tracker");
			case ORANGE -> galAutomata.get("Tracker");
			case GREEN -> galAutomata.get("Fleeing");
			default -> throw new IllegalArgumentException();
			};

			if (enemyAutomaton != null) {
				GALBot enemyBot = new GALBot(enemy, enemyAutomaton, ENEMY_SPEED, new FrankCategories());
				enemy.setBot(enemyBot);
			} else {
				System.err.println("Automate introuvable pour ennemi spawn : " + type);
			}

			return enemy;
		});

		spawner.setBot(bot);
		model.add(spawner);

		System.out.println("EnemySpawnerEntity créée avec l'automate EnemySpawner");
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

	private static Map<String, Automaton> loadGalAutomata() {
		Map<String, Automaton> automata = new LinkedHashMap<>();

		String[] files = { "gal_fsm/Tracking.gal", "gal_fsm/Flee.gal", "gal_fsm/EnemyRandom.gal",
				"gal_fsm/FruitFall.gal", "gal_fsm/EnemySpawner.gal" };

		for (String file : files) {
			try {
				automata.putAll(GalCompiler.fromFile(file));
			} catch (Exception e) {
				System.err.println("GAL ignoré: " + file + " -> " + e.getMessage());
			}
		}

		System.out.println("Automates GAL chargés: " + automata.keySet());
		return automata;
	}
}