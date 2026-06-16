package franck;

import java.util.ArrayList;
import java.util.List;

import engine.core.Game;
import engine.core.Model;
import engine.entities.BasicStunt;
import engine.entities.Entity;
import engine.geometry.Grid;
import engine.geometry.ISU;
import franck.entities.*;

public class FruityFrankModel extends Model {

	private final GameState state = new GameState();
	private final Rules rules = new Rules();
	private final FruityFrankMap map;
	private final Game game;
	private Frank player;
	private boolean debugMode = false;
	private int tick = 0;

	private static final double CHERRY_SPEED = 4.0;
	private static final int FALL_DELAY_TICKS = 30; // ~1 s à 30 TPS
	private static final int FALL_STEP_TICKS = 3; // vitesse de chute : 1 case / 3 ticks

	public FruityFrankModel(Game game, FruityFrankMap map) {
		super(game);
		this.game = game;
		this.map = map;
	}

	public void setPlayer(Frank p) {
		this.player = p;
	}

	public Frank player() {
		return player;
	}

	public GameState state() {
		return state;
	}

	public FruityFrankMap map() {
		return map;
	}

	public boolean debugMode() {
		return debugMode;
	}

	public void toggleDebug() {
		debugMode = !debugMode;
	}

	// ============================================================
	// UPDATE
	// ============================================================

	@Override
	public void update() {
		super.update();
		tick++;
		digGrass();
		updateGrassFade();
		pushApples();
		collectUnderFrank();
		updateAppleFall();
		removeBadCherryProjectiles();
	}

	// ============================================================
	// CREUSAGE DU GAZON
	// ============================================================

	/**
	 * Frank horizontal -> détruit seulement le gros gazon (9 cm). Frank vertical ->
	 * détruit le gros ET le petit gazon (1 cm). Ennemis CREUSEURS (canDig) ->
	 * détruisent le gros gazon. Ennemis non creuseurs -> ne détruisent rien (ils
	 * restent dans les tunnels).
	 */
	private void digGrass() {
		// FRANK : horizontal → gros gazon seul ; vertical → gros + petit gazon
		if (player != null && entities().contains(player)) {
			ISU.Vector v = player.getLinearSpeed();
			boolean vertical = v != null && v.y() != 0 && v.x() == 0;
			for (Grid.Cell c : new ArrayList<>(player.getHitbox().occupiedCells()))
				digCell(c.position(), true, vertical);
		}

		// ENNEMIS CREUSEURS uniquement (RED, ORANGE) : même logique que Frank
		for (Entity e : new ArrayList<>(entities())) {
			if (!(e instanceof Enemy en) || !en.canDig())
				continue;

			ISU.Vector ev = en.getLinearSpeed();
			boolean enemyVertical = ev != null && ev.y() != 0 && ev.x() == 0;

			for (Grid.Cell c : new ArrayList<>(en.getHitbox().occupiedCells()))
				digCell(c.position(), true, enemyVertical);
		}
	}

	private void digCell(Grid.Position pos, boolean digHigh, boolean digLow) {
		for (Entity e : new ArrayList<>(grid().cellAt(pos).entities())) {
			if (digHigh && e instanceof GrassHigh gh)
				gh.startFade();
			if (digLow && e instanceof GrassLow)
				remove(e);
		}
	}

	private void updateGrassFade() {
		List<Entity> done = new ArrayList<>();
		for (Entity e : new ArrayList<>(entities())) {
			if (e instanceof GrassHigh gh && gh.isFading()) {
				gh.tickFade(); // décrémente le timer (7 → 6 → 5 ...)
				if (gh.fadeDone())
					done.add(gh); // arrivé à 0 → à retirer
			}
		}
		for (Entity e : done)
			remove(e); // suppression effective
	}

	// ============================================================
	// PUSH DES POMMES (glissement continu, basé sur positions réelles)
	// ============================================================

	private void pushApples() {
		if (player == null || !entities().contains(player))
			return;

		double cell = game.getCmPerCell();
		int dir = player.facingDx();
		boolean wantsH = (dir != 0) && (player.facingDy() == 0);

		for (Entity e : new ArrayList<>(entities())) {
			if (!(e instanceof Apple apple))
				continue;
			if (apple.isFalling())
				continue;
			if (apple.stunt() != null)
				apple.stunt().drive(0, 0);

			if (!wantsH)
				continue;

			Grid.Position fp = player.position();
			Grid.Position ap = apple.position();

			// Frank doit être EXACTEMENT une case derrière la pomme, même ligne
			boolean sameRow = fp.y() == ap.y();
			boolean frankBehind = sameRow && (ap.x() - fp.x() == dir);
			if (!frankBehind)
				continue;

			// Frank doit être bien centré dans sa case avant de pousser
			// (évite de pousser quand il est à cheval entre deux cases)
			double fxCenterCm = (fp.x() + 0.5) * cell;
			double fxRealCm = player.center().x();
			if (Math.abs(fxRealCm - fxCenterCm) > 0.35 * cell)
				continue;

			// La case devant la pomme doit être libre
			Grid.Position beyond = grid().new Position(ap.x() + dir, ap.y());
			if (!canReceiveApple(beyond))
				continue;

			// Écrase le petit gazon de la case que la pomme quitte
			for (Entity o : new ArrayList<>(grid().cellAt(ap).entities()))
				if (o instanceof GrassLow gl)
					remove(gl);

			// Déplace la pomme proprement d'une case (alignée sur la grille)
			apple.setPosition(beyond);
		}
	}
	// ============================================================
	// RAMASSAGE
	// ============================================================

	private void collectUnderFrank() {
		if (player == null || !entities().contains(player))
			return;
		Grid.Cell cell = grid().cellAt(player.position());
		List<Entity> got = new ArrayList<>();
		for (Entity e : cell.entities())
			if (e instanceof Fruit || e instanceof Cherry)
				got.add(e);
		for (Entity e : got) {
			remove(e);
			if (e instanceof Fruit f)
				state.addScore(pointsForFruit(f));
			if (e instanceof Cherry) {
				player.pickCherry();
				state.addScore(50);
			}
		}
	}

	// ============================================================
	// CHUTE DES POMMES (délai ~1 s puis chute progressive)
	// ============================================================

	private void updateAppleFall() {
		for (Entity e : new ArrayList<>(entities())) {
			if (!(e instanceof Apple apple))
				continue;

			Grid.Position pos = apple.position();
			Grid.Position south = grid().new Position(pos.x(), pos.y() + 1);

			// SUPPORT = ce qui RETIENT la pomme.
			// Le GrassLow (1cm) ne retient PAS : la pomme l'écrase en tombant.
			boolean supported = map.isWall(south.x(), south.y()) || cellHas(south, GrassHigh.class) // gros gazon =
																									// support
					|| cellHas(south, Apple.class) // autre pomme = support
					|| cellHas(south, Fruit.class); // fruit = support
			// NOTE : GrassLow n'est PAS dans la liste → la pomme tombe à travers/dessus

			if (supported) {
				apple.disarm();
				apple.setFalling(false);
				continue;
			}

			// Pas de support : armer le délai d'~1s
			if (!apple.isArmed()) {
				apple.arm(FALL_DELAY_TICKS);
				continue;
			}

			apple.tickDelay();
			if (!apple.delayDone())
				continue;

			apple.setFalling(true);
			if (tick % FALL_STEP_TICKS != 0)
				continue; // ralentit la chute

			// La pomme tombe dans la case du dessous
			Grid.Cell below = grid().cellAt(south);
			boolean killed = false;
			for (Entity occ : new ArrayList<>(below.entities())) {
				if (occ instanceof Frank f) {
					f.loseLife();
					state.killedByEnemy();
					if (f.isDead())
						state.setGameOver();
					remove(f);
					killed = true;
				}
				if (occ instanceof Enemy en) {
					remove(en);
					state.addScore(500);
					killed = true;
				}
				// Le petit gazon est ÉCRASÉ par la pomme qui tombe dessus
				if (occ instanceof GrassLow gl)
					remove(gl);
			}
			if (killed) {
				remove(apple);
				continue;
			}

			apple.setPosition(south);
		}
	}
	// ============================================================
	// PROJECTILES CERISE
	// ============================================================

	private void removeBadCherryProjectiles() {
		List<Entity> dead = new ArrayList<>();
		for (Entity e : entities()) {
			if (!(e instanceof CherryProjectile cp))
				continue;

			// La case juste devant le projectile, dans sa direction de tir
			Grid.Position next = grid().new Position(cp.position().x() + cp.facingDx(),
					cp.position().y() + cp.facingDy());

			// Mur → la cerise disparaît
			if (map.isWall(next.x(), next.y())) {
				dead.add(cp);
				continue;
			}

			// Tout obstacle dans la case suivante → la cerise disparaît
			for (Entity o : grid().cellAt(next).entities()) {
				if (o instanceof GrassHigh || o instanceof GrassLow || o instanceof Fruit || o instanceof Apple
						|| o instanceof Cherry) {
					dead.add(cp);
					break;
				}
			}
		}
		for (Entity e : dead)
			remove(e);
	}

	public void throwCherryInternal(Frank frank) {
		frank.useCherry();
		int sx = frank.position().x() + frank.facingDx();
		int sy = frank.position().y() + frank.facingDy();
		Grid.Position p = grid().new Position(sx, sy);
		if (map.isWall(p.x(), p.y()))
			return; // ← seule condition : pas tirer dans un mur

		CherryProjectile proj = new CherryProjectile(game, sx, sy, frank.facingDx(), frank.facingDy());
		proj.setStunt(new BasicStunt(proj));
		proj.stunt().drive(frank.facingDx() * CHERRY_SPEED, frank.facingDy() * CHERRY_SPEED);
		add(proj);
	}
	// ============================================================
	// PHYSIQUE
	// ============================================================

	@Override
	public boolean canOccupy(Entity e, Grid.Position target) {

		if (map.isWall(target.x(), target.y()))
			return false;

		if (e instanceof Frank frank) {
			Apple apple = null;
			for (Entity o : grid().cellAt(target).entities())
				if (o instanceof Apple a) {
					apple = a;
					break;
				}

			if (apple != null) {
				int dx = target.x() - frank.position().x();
				int dy = target.y() - frank.position().y();
				if (dy != 0)
					return false;
				Grid.Position beyond = grid().new Position(target.x() + dx, target.y());
				boolean ok = canReceiveApple(beyond);
				System.out.println("[PUSH] Frank veut pousser pomme, case derrière libre ? " + ok + " (beyond="
						+ beyond.x() + "," + beyond.y() + ")");
				return ok;
			}
		}

		if (e instanceof Enemy en) {
			if (en.canDig())
				return true;
			for (Entity o : grid().cellAt(target).entities()) {
				if (o instanceof GrassHigh)
					return false;
			}
			return true;
		}

		if (e instanceof CherryProjectile) {
			for (Entity o : grid().cellAt(target).entities())
				if (o instanceof GrassHigh)
					return false;
			return true;
		}

		return true;
	}

	private boolean canReceiveApple(Grid.Position pos) {
		if (map.isWall(pos.x(), pos.y()))
			return false;
		for (Entity e : grid().cellAt(pos).entities()) {
			if (e instanceof Apple)
				return false;
			if (e instanceof Fruit)
				return false;
			if (e instanceof Cherry)
				return false;
			if (e instanceof Enemy)
				return false;
		}
		return true;
	}

	// ============================================================
	// DIVERS
	// ============================================================

	@Override
	protected void onCollision(Entity a, Entity b) {
		rules.resolve(this, state, a, b);
	}

	private boolean cellHas(Grid.Position pos, Class<?> type) {
		for (Entity e : grid().cellAt(pos).entities())
			if (type.isInstance(e))
				return true;
		return false;
	}

	private int pointsForFruit(Fruit f) {
		return switch (f.type()) {
		case BANANA -> 150;
		case ORANGE -> 180;
		case STRAWBERRY -> 250;
		case PLUM -> 200;
		case GRAPES -> 120;
		};
	}
}